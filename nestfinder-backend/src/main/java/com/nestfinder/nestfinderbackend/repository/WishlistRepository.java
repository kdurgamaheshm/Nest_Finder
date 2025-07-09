package com.nestfinder.nestfinderbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nestfinder.nestfinderbackend.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndHouseId(Long userId, Long houseId);

    // Count wishlist items by userId
    long countByUserId(Long userId);
}
