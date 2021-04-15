package com.test.exercise.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class QuoteHistory {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Embedded 
	private Quote quote;

	@Column(name = "date_time", columnDefinition = "TIMESTAMP")
	private LocalDateTime timeCreated;
	
	public QuoteHistory() {}
	
	public QuoteHistory(QuoteHistory qh) {
		id = qh.getId();
		quote = qh.getQuote();
		timeCreated = qh.getTimeCreated();
	}
	
	public QuoteHistory(Quote quote) {
		this.quote = quote;
		this.timeCreated = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Quote getQuote() {
		return quote;
	}

	public LocalDateTime getTimeCreated() {
		return timeCreated;
	}
	
	public void setTimeCreated(LocalDateTime time) {
		this.timeCreated = time;
	}

	public void setQuote(Quote quote) {
		this.quote = quote;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuoteHistory other = (QuoteHistory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (quote == null) {
			if (other.quote != null)
				return false;
		} else if (!quote.equals(other.quote))
			return false;
		if (timeCreated == null) {
			if (other.timeCreated != null)
				return false;
		} else if (!timeCreated.equals(other.timeCreated))
			return false;
		return true;
	}
	
}
