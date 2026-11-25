package com.employeemanagement.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeemanagement.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findAllByOrderByPostedAtDesc();

    List<Announcement> findAllByOrderByPostedAtDesc(Pageable pageable);
}
