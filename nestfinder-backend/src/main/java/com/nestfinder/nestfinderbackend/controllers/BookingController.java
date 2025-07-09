package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.Booking;
import com.nestfinder.nestfinderbackend.payload.request.BookingRequest;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import com.nestfinder.nestfinderbackend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId(); // Get user ID from authenticated user

        if (!bookingRequest.getUserId().equals(userId)) {
            return new ResponseEntity<>(new MessageResponse("Error: You can only book for yourself!"),
                    HttpStatus.FORBIDDEN);
        }

        // Step 1: Prepare the booking (validate dates, availability, etc.)
        Optional<Booking> preparedBookingOptional = bookingService.prepareBooking(
                bookingRequest.getHouseId(),
                userId,
                bookingRequest.getStartDate(),
                bookingRequest.getEndDate());

        if (preparedBookingOptional.isEmpty()) {
            return new ResponseEntity<>(
                    new MessageResponse("Error: House not available for the selected dates or invalid house/user."),
                    HttpStatus.BAD_REQUEST);
        }

        Booking preparedBooking = preparedBookingOptional.get();

        // Step 2: Confirm booking and process payment
        Optional<Booking> finalBookingOptional = bookingService.confirmBookingAndProcessPayment(
                preparedBooking,
                bookingRequest.getStripeToken());

        if (finalBookingOptional.isPresent()) {
            return new ResponseEntity<>(finalBookingOptional.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new MessageResponse("Error: Payment failed or booking could not be confirmed."),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        // Implement logic to check if the user is authorized to delete this booking
        // For simplicity, allowing any USER/OWNER/ADMIN to delete for now.
        // In a real application, you'd check if the current user is the booker, owner
        // of the house, or an admin.
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
