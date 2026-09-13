package com.rentdb.lead.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rentdb.lead.domain.LeadNote;

public interface LeadNoteRepository extends JpaRepository<LeadNote, Long> {

	@EntityGraph(attributePaths = "admin")
	List<LeadNote> findByLeadIdOrderByCreatedAtDescIdDesc(Long leadId);

}
