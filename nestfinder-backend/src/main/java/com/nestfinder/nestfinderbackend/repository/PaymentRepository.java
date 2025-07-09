package com.nestfinder.nestfinderbackend.repository;

import com.nestfinder.nestfinderbackend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByStripeChargeId(String stripeChargeId);

    // Find recent payments by userId
    List<Payment> findTop5ByUserIdOrderByPaymentDateDesc(Long userId);
}
