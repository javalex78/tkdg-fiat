package com.test.exercise.model;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class QuoteValidator implements IQuoteValidator {
	
	@Value("#{restController.actualQuotes}")
	private final Map<String, Quote> actualQuotes;
	
	public QuoteValidator(Map<String, Quote> actualQuotes) {
		this.actualQuotes = actualQuotes;
	}

	public List<String> validate(Quote quote) {
		List<String> errorList = new ArrayList<>();
		
		//Check ISIN:
		if(quote.getIsin() == null) {
			errorList.add("ERROR: ISIN is null: " + quote);
		} else if(quote.getIsin().length() != 12) {
			errorList.add("ERROR: ISIN must be 12 symbols! : " + quote);
		}
		
		//Check bid/ask
		if(quote.getAsk() != null) {
			if(quote.getAsk().scale() > 2) quote.setAsk( quote.getAsk().setScale(2, RoundingMode.HALF_UP) );
			
		}  else {
			errorList.add("ERROR: Ask can't be NULL! : " + quote);
		}
		if(quote.getBid() != null) {
			if(quote.getBid().scale() > 2) quote.setBid( quote.getBid().setScale(2, RoundingMode.HALF_UP) );
			if( quote.getAsk() != null && quote.getBid().compareTo(quote.getAsk()) != -1 ) {
				errorList.add("ERROR: Bid must be less than Ask! : " + quote);
			}
		}
		
		//Check for duplication of actual quote
		Quote actualQuote = actualQuotes.get(quote.getIsin());
		if( quote.equals(actualQuote) ) {
			errorList.add("Do nothing: " + quote + " not changed since last time");
		}
		
		return errorList;
	}

}
