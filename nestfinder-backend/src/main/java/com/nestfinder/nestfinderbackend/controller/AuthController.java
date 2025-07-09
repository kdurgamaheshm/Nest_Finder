package com.nestfinder.nestfinderbackend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nestfinder.nestfinderbackend.payload.request.LoginRequest;
import com.nestfinder.nestfinderbackend.payload.request.SignupRequest;
import com.nestfinder.nestfinderbackend.payload.response.JwtResponse;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.repository.RoleRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.security.jwt.JwtUtils;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin( maxAge = 3600)
@RestController
// @RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

//    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

//    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        // This part will need to be expanded to handle roles (USER, OWNER, ADMIN)
        // For now, a basic user creation is shown.
        // User user = new User(signUpRequest.getUsername(),
        // signUpRequest.getEmail(),
        // encoder.encode(signUpRequest.getPassword()));

        // Set roles based on the request or default to USER
        // Set<Role> roles = new HashSet<>();
        // if (signUpRequest.getRole() == null) {
        // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(userRole);
        // } else {
        // signUpRequest.getRole().forEach(role -> {
        // switch (role) {
        // case "admin":
        // Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(adminRole);
        // break;
        // case "owner":
        // Role ownerRole = roleRepository.findByName(ERole.ROLE_OWNER)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(ownerRole);
        // break;
        // default:
        // Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        // .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        // roles.add(userRole);
        // }
        // });
        // }
        // user.setRoles(roles);
        // userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
