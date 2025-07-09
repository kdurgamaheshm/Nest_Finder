package com.nestfinder.nestfinderbackend.repository;

import com.nestfinder.nestfinderbackend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a specific house
    List<Review> findByHouseId(Long houseId);

    // Get all reviews submitted by a specific user
    List<Review> findByUserId(Long userId);
}
