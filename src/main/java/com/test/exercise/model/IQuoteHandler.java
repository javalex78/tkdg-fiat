package com.test.exercise.model;

import java.math.BigDecimal;
import java.util.List;

public interface IQuoteHandler {
	
	/**
	 * Handle new quote
	 * @param quote - new quote
	 * @return list of errors. If no errors - return empty list.
	 */
	public List<String> handle(Quote quote);
	
	public BigDecimal calculateElvlForNewQuote(Quote newQuote, BigDecimal oldElvl);
	
	public BigDecimal getActualElvlByIsin(String isin);
}
