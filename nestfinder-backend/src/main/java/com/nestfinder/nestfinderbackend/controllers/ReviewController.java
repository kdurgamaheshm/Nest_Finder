package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.Review;
import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.ReviewRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<House> houseOptional = houseRepository.findById(review.getHouse().getId());

        if (userOptional.isEmpty() || houseOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: User or House not found!"), HttpStatus.NOT_FOUND);
        }

        review.setUser(userOptional.get());
        review.setHouse(houseOptional.get());
        review.setReviewDate(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    @GetMapping("/house/{houseId}")
    public List<Review> getReviewsByHouseId(@PathVariable Long houseId) {
        return reviewRepository.findByHouseId(houseId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public List<Review> getReviewsByUserId(@PathVariable Long userId) {
        // In a real application, you'd add logic to ensure a user can only view their
        // own reviews
        // or if they are an admin/owner viewing others' reviews.
        return reviewRepository.findByUserId(userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        if (reviewOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: Review not found!"), HttpStatus.NOT_FOUND);
        }

        Review review = reviewOptional.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (review.getUser().getId().equals(userDetails.getId()) || isAdmin) {
            reviewRepository.deleteById(id);
            return new ResponseEntity<>(new MessageResponse("Review deleted successfully!"), HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(new MessageResponse("Error: You are not authorized to delete this review!"),
                    HttpStatus.FORBIDDEN);
        }
    }
}
