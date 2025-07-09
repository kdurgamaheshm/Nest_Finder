package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.EHouseStatus;
import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.Image;
import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.payload.request.HouseSearchRequest;
import com.nestfinder.nestfinderbackend.repository.HouseRepository;
import com.nestfinder.nestfinderbackend.repository.ImageRepository;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import com.nestfinder.nestfinderbackend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin( maxAge = 3600)
@RestController
@RequestMapping("/api/houses")
public class HouseController {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping
    public List<House> getAllHouses(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String houseStatus) { // Added houseStatus parameter

        return houseRepository.findAll((Specification<House>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (city != null && !city.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + city.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (houseType != null && !houseType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("houseType"), com.nestfinder.nestfinderbackend.model.EHouseType.valueOf(houseType.toUpperCase())));
            }
            if (houseStatus != null && !houseStatus.isEmpty()) { // Filter by houseStatus
                predicates.add(criteriaBuilder.equal(root.get("houseStatus"), EHouseStatus.valueOf(houseStatus.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<House> createHouse(@RequestPart("house") House house,
                                             @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> owner = userRepository.findById(userDetails.getId());

        if (owner.isPresent()) {
            house.setOwner(owner.get());
            if (house.getHouseStatus() == null) { // Set default status if not provided
                house.setHouseStatus(EHouseStatus.AVAILABLE);
            }
            House savedHouse = houseRepository.save(house);

            if (images != null) {
                for (MultipartFile file : images) {
                    String fileName = fileStorageService.storeFile(file);
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/")
                            .path(fileName)
                            .toUriString();
                    Image image = new Image(fileName, fileDownloadUri, savedHouse);
                    imageRepository.save(image);
                    savedHouse.addImage(image); // Add image to house's collection
                }
                houseRepository.save(savedHouse); // Save house again to update image collection
            }
            return new ResponseEntity<>(savedHouse, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<House> getHouseById(@PathVariable Long id) {
        Optional<House> house = houseRepository.findById(id);
        return house.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<House> updateHouse(@PathVariable Long id,
                                             @RequestPart("house") House houseDetails,
                                             @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Optional<House> optionalHouse = houseRepository.findById(id);
        if (optionalHouse.isPresent()) {
            House house = optionalHouse.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (house.getOwner().getId().equals(userDetails.getId()) || isAdmin) {
                house.setTitle(houseDetails.getTitle());
                house.setDescription(houseDetails.getDescription());
                house.setAddress(houseDetails.getAddress());
                house.setPrice(houseDetails.getPrice());
                house.setHouseType(houseDetails.getHouseType());
                house.setHouseStatus(houseDetails.getHouseStatus()); // Update house status

                // Handle new image uploads
                if (images != null) {
                    for (MultipartFile file : images) {
                        String fileName = fileStorageService.storeFile(file);
                        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/files/")
                                .path(fileName)
                                .toUriString();
                        Image image = new Image(fileName, fileDownloadUri, house);
                        imageRepository.save(image);
                        house.addImage(image);
                    }
                }
                House updatedHouse = houseRepository.save(house);
                return ResponseEntity.ok(updatedHouse);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHouse(@PathVariable Long id) {
        Optional<House> optionalHouse = houseRepository.findById(id);
        if (optionalHouse.isPresent()) {
            House house = optionalHouse.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (house.getOwner().getId().equals(userDetails.getId()) || isAdmin) {
                houseRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<House> updateHouseStatus(@PathVariable Long id, @RequestParam EHouseStatus status) {
        Optional<House> optionalHouse = houseRepository.findById(id);
        if (optionalHouse.isPresent()) {
            House house = optionalHouse.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (house.getOwner().getId().equals(userDetails.getId()) || isAdmin) {
                house.setHouseStatus(status);
                House updatedHouse = houseRepository.save(house);
                return ResponseEntity.ok(updatedHouse);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
