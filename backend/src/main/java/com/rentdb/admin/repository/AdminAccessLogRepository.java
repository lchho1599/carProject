package com.rentdb.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.admin.domain.AdminAccessLog;

public interface AdminAccessLogRepository extends JpaRepository<AdminAccessLog, Long> {
}
