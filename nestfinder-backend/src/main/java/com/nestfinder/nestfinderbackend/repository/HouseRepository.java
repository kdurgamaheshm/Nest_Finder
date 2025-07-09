package com.nestfinder.nestfinderbackend.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nestfinder.nestfinderbackend.model.EHouseStatus;
import com.nestfinder.nestfinderbackend.model.House;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
    List<House> findAll(Specification<House> houseSpecification);

    List<House> findTop10ByHouseStatusOrderByIdDesc(EHouseStatus houseStatus);
}
