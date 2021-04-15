package com.test.exercise.model;

import java.math.BigDecimal;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class Quote {

	/**
	 * Unique code of the instrument.
	 * Must be 12 symbols String
	 */
	@Column(name = "isin", nullable = false) 
	private String isin;
	
	// bid must be less than ask
	@Column(name = "bid", updatable = false, scale = 2, precision = 10)
	private BigDecimal bid;
	@Column(name = "ask", updatable = false, scale = 2, precision = 10)
	private BigDecimal ask;
	private BigDecimal elvl;
	
	public Quote() {}
	
	public Quote(Quote q) {
		isin = q.getIsin();
		bid = q.getBid();
		ask = q.getAsk();
		elvl = q.getElvl();
	}
	
	public Quote(String isin, BigDecimal bid, BigDecimal ask) {
		this.isin = isin;
		this.bid = bid;
		this.ask = ask;
	}
	
	public String getIsin() {
		return isin;
	}
	public BigDecimal getBid() {
		return bid;
	}
	public BigDecimal getAsk() {
		return ask;
	}
	
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getElvl() {
		return elvl;
	}

	public void setElvl(BigDecimal elvl) {
		this.elvl = elvl;
	}

	@Override
	public String toString() {
		return "Quote [isin=" + isin + ", bid=" + bid + ", ask=" + ask + ", elvl=" + elvl + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ask == null) ? 0 : ask.hashCode());
		result = prime * result + ((bid == null) ? 0 : bid.hashCode());
		result = prime * result + ((isin == null) ? 0 : isin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quote other = (Quote) obj;
		if (ask == null) {
			if (other.ask != null)
				return false;
		} else if (!ask.equals(other.ask))
			return false;
		if (bid == null) {
			if (other.bid != null)
				return false;
		} else if (!bid.equals(other.bid))
			return false;
		if (isin == null) {
			if (other.isin != null)
				return false;
		} else if (!isin.equals(other.isin))
			return false;
		return true;
	}
}