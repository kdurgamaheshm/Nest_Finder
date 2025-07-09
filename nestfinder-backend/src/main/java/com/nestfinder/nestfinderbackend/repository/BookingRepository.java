package com.nestfinder.nestfinderbackend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nestfinder.nestfinderbackend.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find overlapping bookings for a given house and date range
    List<Booking> findByHouseIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long houseId,
            LocalDate endDate,
            LocalDate startDate);

    // Find all bookings by houseId (used to check remaining bookings after a
    // deletion)
    List<Booking> findByHouseId(Long houseId);

    // Count bookings by userId
    long countByUserId(Long userId);

    // Find recent bookings by userId
    List<Booking> findTop5ByUserIdOrderByStartDateDesc(Long userId);
}
