package com.nestfinder.nestfinderbackend.controllers;

import java.util.Optional;
import java.util.List;

import com.nestfinder.nestfinderbackend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestfinder.nestfinderbackend.payload.request.UserProfileUpdateRequest;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.payload.response.UserDashboardResponse;
import com.nestfinder.nestfinderbackend.repository.BookingRepository;
import com.nestfinder.nestfinderbackend.repository.MessageRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.repository.WishlistRepository;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.PaymentRepository;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return new ResponseEntity<>(new MessageResponse("User not found."), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("User not found."), HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();

        // Fetch available houses (limit 10)
        List<House> availableHouses = houseRepository.findTop10ByHouseStatusOrderByIdDesc(EHouseStatus.valueOf("AVAILABLE"));

        // Fetch recent bookings (limit 5)
        List<Booking> recentBookings = bookingRepository.findTop5ByUserIdOrderByStartDateDesc(userId);

        // Fetch payments (limit 5)
        List<Payment> payments = paymentRepository.findTop5ByUserIdOrderByPaymentDateDesc(userId);

        // Count bookings, wishlist, messages
        long bookingsCount = bookingRepository.countByUserId(userId);
        long wishlistCount = wishlistRepository.countByUserId(userId);
        long messagesCount = messageRepository.countByUserId(userId);

        UserDashboardResponse dashboardResponse = new UserDashboardResponse(
                user.getUsername(),
                bookingsCount,
                wishlistCount,
                messagesCount,
                availableHouses,
                recentBookings,
                payments);

        return ResponseEntity.ok(dashboardResponse);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest userProfileUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("User not found."), HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();

        // Check if username is already taken by another user
        if (!user.getUsername().equals(userProfileUpdateRequest.getUsername())
                && userRepository.existsByUsername(userProfileUpdateRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Check if email is already in use by another user
        if (!user.getEmail().equals(userProfileUpdateRequest.getEmail())
                && userRepository.existsByEmail(userProfileUpdateRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        user.setUsername(userProfileUpdateRequest.getUsername());
        user.setEmail(userProfileUpdateRequest.getEmail());

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User profile updated successfully!"));
    }
}
