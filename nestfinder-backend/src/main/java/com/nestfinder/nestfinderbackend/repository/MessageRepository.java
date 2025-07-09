package com.nestfinder.nestfinderbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nestfinder.nestfinderbackend.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Count messages by userId
    long countByUserId(Long userId);
}
