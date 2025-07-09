package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/houses")
    public List<com.nestfinder.nestfinderbackend.model.House> getAllHouses() {
        return houseRepository.findAll();
    }

    @GetMapping("/users/count")
    public long getUserCount() {
        return userRepository.count();
    }

    @GetMapping("/houses/count")
    public long getHouseCount() {
        return houseRepository.count();
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(enabled);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("User status updated successfully!"));
        } else {
            return new ResponseEntity<>(new MessageResponse("User not found!"), HttpStatus.NOT_FOUND);
        }
    }
}
