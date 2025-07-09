package com.nestfinder.nestfinderbackend.service;

import com.nestfinder.nestfinderbackend.model.Booking;
import com.nestfinder.nestfinderbackend.model.EHouseStatus;
import com.nestfinder.nestfinderbackend.model.EPaymentStatus;
import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.Payment;
import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.repository.BookingRepository;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService; // Inject PaymentService

    // This method will now just prepare the booking, not save it directly
    public Optional<Booking> prepareBooking(Long houseId, Long userId, LocalDate startDate, LocalDate endDate) {
        Optional<House> houseOptional = houseRepository.findById(houseId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (houseOptional.isEmpty() || userOptional.isEmpty()) {
            return Optional.empty(); // House or User not found
        }

        House house = houseOptional.get();
        User user = userOptional.get();

        // Check if the house is AVAILABLE
        if (house.getHouseStatus() != EHouseStatus.AVAILABLE) {
            return Optional.empty(); // House is not available for booking
        }

        // Check for overlapping bookings
        // In a real app, filter by houseId for efficiency
        List<Booking> existingBookings = bookingRepository
                .findByHouseIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(houseId, endDate, startDate);
        if (!existingBookings.isEmpty()) {
            return Optional.empty(); // Overlapping booking found
        }

        // Calculate total price
        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal totalPrice = house.getPrice().multiply(BigDecimal.valueOf(numberOfDays));

        Booking newBooking = new Booking(house, user, startDate, endDate, totalPrice);
        // Do NOT save here. Saving will happen after successful payment.
        return Optional.of(newBooking);
    }

    @Transactional
    public Optional<Booking> confirmBookingAndProcessPayment(Booking booking, String stripeToken) {
        try {
            // 1. Process Payment
            Optional<Payment> paymentOptional = paymentService.createCharge(
                    booking.getId(), // Booking ID might not be set yet if it's a new booking
                    booking.getUser().getId(),
                    booking.getTotalPrice(),
                    "USD", // Assuming USD for now, can be dynamic
                    stripeToken);

            if (paymentOptional.isEmpty() || paymentOptional.get().getStatus() != EPaymentStatus.SUCCEEDED) {
                // Payment failed
                return Optional.empty();
            }

            // 2. Save Booking (if not already saved)
            Booking savedBooking = bookingRepository.save(booking);

            // 3. Update House Status
            House house = savedBooking.getHouse();
            house.setHouseStatus(EHouseStatus.BOOKED);
            houseRepository.save(house);

            return Optional.of(savedBooking);

        } catch (Exception e) {
            System.err.println("Error confirming booking and processing payment: " + e.getMessage());
            // Rollback or handle transaction failure
            return Optional.empty();
        }
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Transactional
    public void deleteBooking(Long id) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            bookingRepository.deleteById(id);

            // Update payment status to REFUNDED (simplified)
            Optional<Payment> paymentOptional = paymentService.getPaymentById(booking.getId()); // Assuming bookingId is
                                                                                                // paymentId
            paymentOptional
                    .ifPresent(payment -> paymentService.updatePaymentStatus(payment.getId(), EPaymentStatus.REFUNDED));

            // After deleting a booking, check if the house can be set back to AVAILABLE
            // This is a simplified logic. In a real app, you'd check if there are other
            // active bookings for this house.
            House house = booking.getHouse();
            List<Booking> remainingBookingsForHouse = bookingRepository.findByHouseId(house.getId());
            boolean hasOtherActiveBookings = remainingBookingsForHouse.stream()
                    .anyMatch(b -> b.getEndDate().isAfter(LocalDate.now()));

            if (!hasOtherActiveBookings) {
                house.setHouseStatus(EHouseStatus.AVAILABLE);
                houseRepository.save(house);
            }
        }
    }
}
