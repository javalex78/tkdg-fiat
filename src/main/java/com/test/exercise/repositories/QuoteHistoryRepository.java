package com.test.exercise.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.test.exercise.model.QuoteHistory;

public interface QuoteHistoryRepository extends JpaRepository<QuoteHistory, Long> {

	@Query(value = "SELECT qh FROM QuoteHistory qh WHERE qh.timeCreated=(SELECT MAX(qhs.timeCreated) FROM QuoteHistory qhs WHERE qhs.quote.isin=qh.quote.isin) ORDER BY qh.timeCreated")
	List<QuoteHistory> findAllLastQuotes();
}
