package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.Payment;
import com.nestfinder.nestfinderbackend.payload.request.PaymentRequest;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import com.nestfinder.nestfinderbackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/charge")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createCharge(@Valid @RequestBody PaymentRequest paymentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Ensure the payment request is for the authenticated user
        if (!paymentRequest.getUserId().equals(userId)) {
            return new ResponseEntity<>(new MessageResponse("Error: You can only make payments for yourself!"), HttpStatus.FORBIDDEN);
        }

        Optional<Payment> payment = paymentService.createCharge(
                paymentRequest.getBookingId(),
                userId,
                paymentRequest.getAmount(),
                paymentRequest.getCurrency(),
                paymentRequest.getStripeToken()
        );

        if (payment.isPresent()) {
            return new ResponseEntity<>(payment.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(new MessageResponse("Error: Payment failed or invalid booking/user."), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
