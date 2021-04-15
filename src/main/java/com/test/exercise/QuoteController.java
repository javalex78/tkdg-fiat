package com.test.exercise;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.exercise.model.IQuoteHandler;
import com.test.exercise.model.IQuoteValidator;
import com.test.exercise.model.Quote;
import com.test.exercise.model.QuoteHandler;
import com.test.exercise.model.QuoteHistory;
import com.test.exercise.repositories.QuoteHistoryRepository;

@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@RestController(value = "restController")
public class QuoteController {

	private final QuoteHistoryRepository repository;

	private final Map<String, Quote> actualQuotes = new ConcurrentHashMap<>();

	@Autowired
	private IQuoteValidator quoteValidator;

	@Autowired
	private IQuoteHandler quoteHandler;

	public QuoteController(QuoteHistoryRepository repository) {
		this.repository = repository;
	}

	@PostMapping("/newquote")
	List<String> newQuote(@RequestBody Quote newQuote) {
		List<String> errorsList = quoteValidator.validate(newQuote);
		if(errorsList.size() > 0) {
			return errorsList;
		}
		
		errorsList = quoteHandler.handle(newQuote);
		return errorsList;
	}
	
	@GetMapping("/elvls")
	Map<String, BigDecimal> allElvls() {
		final Map<String, BigDecimal> result = new HashMap<>();
		actualQuotes.forEach((isin, quote) -> result.put(isin, quote.getElvl()));
		return result;
	}
	
	/**
	 * Calculate <i>elvl</i> (so called 'energy level') for given quote.
	 * <i>Elvl</i> representing the best price for this instrument
	 * and calculated by the following rules:<br/>
	 * 1. If bid > elvl, then elvl = bid<br/>
	 * 2. If ask < elvl, then elvl = ask<br/>
	 * 3. If elvl for that stock is absent, then elvl = bid<br/>
	 * 4. If bid is absent, then elvl = ask<br/>
	 * 
	 * @param isin - isin of quote for which we calculate elvl
	 * @return elvl, calculate for given quote.
	 */
	@GetMapping("/elvl")
	BigDecimal getActualQuoteElvlByIsin(@RequestParam(name = "isin") String isin) {
		Quote actualQuote = actualQuotes.get(isin);
		return (actualQuote != null) ? actualQuote.getElvl() : null;
	}
	
	@GetMapping("/quotes")
	List<QuoteHistory> all() {
		return repository.findAll(Sort.by("timeCreated"));
	}

	@GetMapping("/quote")
	Quote getActualQuoteByIsin(@RequestParam(name = "isin") String isin) {
		return actualQuotes.get(isin);
	}

	public Map<String, Quote> getActualQuotes() {
		return actualQuotes;
	}

	@PostConstruct
	void init() {
		System.out.println("Loading last quotes from DB..");
		List<QuoteHistory> quotesHistory = repository.findAllLastQuotes();
		quotesHistory.stream().map(x -> x.getQuote()).forEach(x -> actualQuotes.put(x.getIsin(), x));
	}
	
	void cleanState() {
		actualQuotes.clear();
	}
	
}
