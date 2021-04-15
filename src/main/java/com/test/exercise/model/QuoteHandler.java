package com.test.exercise.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.test.exercise.repositories.QuoteHistoryRepository;

@Component
public class QuoteHandler implements IQuoteHandler {
	
	@Value("#{restController.actualQuotes}")
	private final Map<String, Quote> actualQuotes;

	private final QuoteHistoryRepository repository;
	
	public QuoteHandler(Map<String, Quote> actualQuotes, QuoteHistoryRepository repository) {
		this.actualQuotes = actualQuotes;
		this.repository = repository;
	}

	@Override
	public List<String> handle(Quote newQuote) {
		List<String> errorsList = new ArrayList<>();
		
		BigDecimal oldElvl = getActualElvlByIsin(newQuote.getIsin());
		BigDecimal newElvl = calculateElvlForNewQuote(newQuote, oldElvl);
		newQuote.setElvl(newElvl);
		
		actualQuotes.put(newQuote.getIsin(), newQuote);
		QuoteHistory qh = new QuoteHistory(newQuote);
		repository.save(qh);
		errorsList.add("Success: " + newQuote);
		return errorsList;
	}
	
	public BigDecimal getActualElvlByIsin(String isin) {
		Quote actualQuote = actualQuotes.get(isin);
		return (actualQuote != null && actualQuote.getElvl() != null) ? actualQuote.getElvl() : null;
	}

	/**
	 * Rules, how elvl is calculated:
	 * 1. If bid > elvl, then elvl = bid<br/>
	 * 2. If ask < elvl, then elvl = ask<br/>
	 * 3. If elvl for that stock is absent, then elvl = bid<br/>
	 * 4. If bid is absent, then elvl = ask<br/> 
	 */
	public BigDecimal calculateElvlForNewQuote(Quote newQuote, BigDecimal oldElvl) {
		if(oldElvl != null) {
			BigDecimal elvl = oldElvl;
			if(newQuote.getBid().compareTo(oldElvl) == 1) { // If bid > elvl, then elvl = bid
				elvl = newQuote.getBid();
			} else if(oldElvl.compareTo(newQuote.getAsk()) == 1) { // If ask < elvl, then elvl = ask			
				elvl = newQuote.getAsk();
			}
			return elvl;
		} else {  // считаем что Elvl нет, рассчитываем по bid/ask
			if(newQuote.getBid() != null) {
				return newQuote.getBid();
			} else {
				return newQuote.getAsk();
			}
		}
	}

}
