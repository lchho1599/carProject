package com.rentdb.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.notification.domain.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

	List<NotificationLog> findByLeadIdOrderByIdAsc(Long leadId);

}
