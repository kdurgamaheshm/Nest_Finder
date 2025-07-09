package com.nestfinder.nestfinderbackend.repository;

import com.nestfinder.nestfinderbackend.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // Fetch all images for a given house ID
    List<Image> findByHouseId(Long houseId);
}
