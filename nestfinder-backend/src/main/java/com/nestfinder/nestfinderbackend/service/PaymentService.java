package com.nestfinder.nestfinderbackend.service;

import com.nestfinder.nestfinderbackend.model.*;
import com.nestfinder.nestfinderbackend.repository.BookingRepository;
import com.nestfinder.nestfinderbackend.repository.PaymentRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // Properly initialize Stripe's API key after dependency injection
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Optional<Payment> createCharge(Long bookingId, Long userId, BigDecimal amount, String currency, String token) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (bookingOptional.isEmpty() || userOptional.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOptional.get();
        User user = userOptional.get();

        try {
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe accepts amount in cents
                    .setCurrency(currency)
                    .setDescription("Payment for Booking ID: " + bookingId)
                    .setSource(token)
                    .build();

            Charge charge = Charge.create(params);

            Payment payment = new Payment(
                    booking,
                    user,
                    amount,
                    currency,
                    charge.getId(),
                    EPaymentStatus.SUCCEEDED,
                    LocalDateTime.now()
            );

            return Optional.of(paymentRepository.save(payment));

        } catch (StripeException e) {
            System.err.println("Stripe Exception: " + e.getMessage());
            Payment payment = new Payment(
                    booking,
                    user,
                    amount,
                    currency,
                    null,
                    EPaymentStatus.FAILED,
                    LocalDateTime.now()
            );
            return Optional.of(paymentRepository.save(payment));
        } catch (Exception e) {
            System.err.println("General Exception: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> updatePaymentStatus(Long paymentId, EPaymentStatus newStatus) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setStatus(newStatus);
            return Optional.of(paymentRepository.save(payment));
        }
        return Optional.empty();
    }
}
