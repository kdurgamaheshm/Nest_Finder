package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.model.Wishlist;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.repository.WishlistRepository;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @PostMapping("/{houseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addHouseToWishlist(@PathVariable Long houseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<House> houseOptional = houseRepository.findById(houseId);

        if (userOptional.isEmpty() || houseOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: User or House not found!"), HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();
        House house = houseOptional.get();

        if (wishlistRepository.findByUserIdAndHouseId(userId, houseId).isPresent()) {
            return new ResponseEntity<>(new MessageResponse("Error: House already in wishlist!"), HttpStatus.CONFLICT);
        }

        Wishlist wishlist = new Wishlist(user, house);
        wishlistRepository.save(wishlist);

        return new ResponseEntity<>(new MessageResponse("House added to wishlist successfully!"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<Wishlist> getUserWishlist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        return wishlistRepository.findByUserId(userId);
    }

    @DeleteMapping("/{houseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeHouseFromWishlist(@PathVariable Long houseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<Wishlist> wishlistOptional = wishlistRepository.findByUserIdAndHouseId(userId, houseId);

        if (wishlistOptional.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: House not found in wishlist!"),
                    HttpStatus.NOT_FOUND);
        }

        wishlistRepository.delete(wishlistOptional.get());
        return new ResponseEntity<>(new MessageResponse("House removed from wishlist successfully!"),
                HttpStatus.NO_CONTENT);
    }
}
