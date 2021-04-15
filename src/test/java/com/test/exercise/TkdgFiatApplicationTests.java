package com.test.exercise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.test.exercise.model.IQuoteHandler;
import com.test.exercise.model.Quote;
import com.test.exercise.model.QuoteHistory;
import com.test.exercise.repositories.QuoteHistoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TkdgFiatApplicationTests {
	
	@Autowired
	private QuoteController controller;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private IQuoteHandler quoteHandler;
	
	@Autowired
	private QuoteHistoryRepository repository;
	
	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
	}
	
	MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	@Test
	public void testRepositoryDeleteAll() throws Exception {
		repository.deleteAll();
		assertThat(repository.count()).isZero();
	}
	
	@Test
	public void testQuoteHistoryMethodEquals() throws Exception {
		QuoteHistory orig = generateRandomQuoteHistory();
		orig.setId(1L);
		QuoteHistory clone = new QuoteHistory(orig);
		assertThat(clone.equals(orig));
		
		QuoteHistory changed = new QuoteHistory(orig);
		changed.setId(2L);
		assertThat( !clone.equals(changed) );

		changed = new QuoteHistory(orig);
		LocalDateTime newTime = changed.getTimeCreated().plus(1, ChronoUnit.SECONDS);
		changed.setTimeCreated(newTime);
		assertThat( !clone.equals(changed) );
		
		changed = new QuoteHistory(orig);
		Quote oq = orig.getQuote();
		String newIsin = oq.getIsin().substring(1) + "!";
		Quote nq = new Quote(newIsin, oq.getBid(), oq.getAsk());
		nq.setElvl(oq.getElvl());
		changed.setQuote(nq);
		assertThat( !clone.equals(changed) );

		nq = new Quote(oq);
		BigDecimal newBid = oq.getBid().add(new BigDecimal(1));
		nq.setBid(newBid);
		changed.setQuote(nq);
		assertThat( !clone.equals(changed) );

		nq = new Quote(oq);
		BigDecimal newAsk = oq.getAsk().add(new BigDecimal(1));
		nq.setAsk(newAsk);
		changed.setQuote(nq);
		assertThat( !clone.equals(changed) );
		
		nq = new Quote(oq);
		BigDecimal newElvl = oq.getElvl().add(new BigDecimal(1));
		nq.setElvl(newElvl);
		changed.setQuote(nq);
		assertThat( !clone.equals(changed) );
	}

	@Test
	public void testSaveQuoteHistory() throws Exception {
		repository.deleteAll();
		QuoteHistory qh = generateRandomQuoteHistory();
		repository.save(qh);
		assertThat(repository.count()).isEqualTo(1L);
		QuoteHistory retrieved = repository.getOne(qh.getId());
		assertThat(qh.equals(retrieved));
		repository.deleteAll();
	}
	
	@Test
	public void testPostQuote() throws Exception {
		repository.deleteAll();
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J0\", \"bid\": \"64.42\", \"ask\": \"65.08\"}")
				.contentType(contentType)).andExpect(status().isOk());
		assertThat(repository.count()).isEqualTo(1L);
		QuoteHistory retrieved = repository.findAll().get(0);
		Quote quote = retrieved.getQuote();
		assertThat("RU000A0JX0J0".equals(quote.getIsin()));
		assertThat("64.42".equals(quote.getBid().toString()));
		assertThat("65.08".equals(quote.getAsk().toString()));
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime secondEarlier = now.minus(1, ChronoUnit.SECONDS);
		assertThat(retrieved.getTimeCreated() != null 
				&& retrieved.getTimeCreated().isBefore(now) 
				& retrieved.getTimeCreated().isAfter(secondEarlier));
	}
	
	@Test
	public void testGetQuoteByIsin() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J1\", \"bid\": \"65.44\", \"ask\": \"66.12\"}")
				.contentType(contentType)).andExpect(status().isOk());

		mockMvc.perform(get("/quote").param("isin", "RU000A0JX0J1").accept(MediaType.APPLICATION_JSON))
		.andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(
				"{\"isin\":\"RU000A0JX0J1\",\"bid\":65.44,\"ask\":66.12}"));
	}
	
	@Test
	public void testElvlByIsin() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J2\", \"bid\": \"20.06\", \"ask\": \"21.12\"}")
				.contentType(contentType)).andExpect(status().isOk());

		mockMvc.perform(get("/elvl").param("isin", "RU000A0JX0J2").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string("20.06"));
	}
	
	
	@Test
	public void testGetAllElvls() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J3\", \"bid\": \"65.44\", \"ask\": \"66.12\"}")
		.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J4\", \"bid\": \"20.06\", \"ask\": \"21.12\"}")
				.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J5\", \"bid\": \"22.17\", \"ask\": \"23.01\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J6\", \"bid\": \"64.42\", \"ask\": \"65.08\"}")
		.contentType(contentType)).andExpect(status().isOk());

		mockMvc.perform(get("/elvls").accept(MediaType.APPLICATION_JSON)).andDo(print())
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(
					"{\"RU000A0JX0J3\":65.44,\"RU000A0JX0J4\":20.06,\"RU000A0JX0J5\":22.17,\"RU000A0JX0J6\":64.42}"));
	}
	
	@Test
	public void testGetAllElvlsForLastQuotesOnly() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_DOWN_UP\", \"bid\": \"20.06\", \"ask\": \"21.12\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_UP_DOWN\", \"bid\": \"64.42\", \"ask\": \"65.08\"}")
				.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU_DOWN_DOWN\", \"bid\": \"15.44\", \"ask\": \"16.12\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU0000_UP_UP\", \"bid\": \"22.17\", \"ask\": \"23.01\"}")
				.contentType(contentType)).andExpect(status().isOk());
		
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {}

		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_DOWN_UP\", \"bid\": \"19.86\", \"ask\": \"20.82\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_UP_DOWN\", \"bid\": \"66.44\", \"ask\": \"67.18\"}")
				.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU_DOWN_DOWN\", \"bid\": \"14.28\", \"ask\": \"15.12\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU0000_UP_UP\", \"bid\": \"23.87\", \"ask\": \"25.11\"}")
				.contentType(contentType)).andExpect(status().isOk());
		
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {}
		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_DOWN_UP\", \"bid\": \"21.56\", \"ask\": \"22.92\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU00_UP_DOWN\", \"bid\": \"65.74\", \"ask\": \"66.46\"}")
				.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU_DOWN_DOWN\", \"bid\": \"12.65\", \"ask\": \"13.72\"}")
				.contentType(contentType)).andExpect(status().isOk());
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU0000_UP_UP\", \"bid\": \"25.13\", \"ask\": \"27.47\"}")
				.contentType(contentType)).andExpect(status().isOk());

		mockMvc.perform(get("/elvls").accept(MediaType.APPLICATION_JSON)).andDo(print())
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().json(
					"{\"RU00_DOWN_UP\":21.56,\"RU00_UP_DOWN\":66.44,\"RU_DOWN_DOWN\":13.72,\"RU0000_UP_UP\":25.13}"));
	}
	
	@Test
	public void testPostQuoteInvalidIsin() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU_ISIN_MORE_THAN_12_SYMBOLS\", \"bid\": \"20.06\", \"ask\": \"21.12\"}")
				.contentType(contentType)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(containsString("ERROR: ISIN must be 12 symbols!")));
		
		mockMvc.perform(get("/quote").param("isin", "RU_ISIN_MORE_THAN_12_SYMBOLS").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string(""));
	}
	
	@Test
	public void testPostQuoteInvalidBidMoreThanAsk() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0A0\", \"bid\": \"26.01\", \"ask\": \"24.12\"}")
				.contentType(contentType)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(containsString("ERROR: Bid must be less than Ask!")));
		
		mockMvc.perform(get("/quote").param("isin", "RU000A0JX0A0").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string(""));
	}
	
	@Test
	public void testGetElvlForQuoteBidNull() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0A1\", \"ask\": \"26.01\"}")
				.contentType(contentType)).andExpect(status().isOk());		
		mockMvc.perform(get("/elvl").param("isin", "RU000A0JX0A1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string("26.01"));
	}
	
	@Test
	public void testPostQuoteInvalidAskNull() throws Exception {
		mockMvc.perform(post("/newquote").content("{\"isin\": \"RU000A0JX0J7\", \"bid\": \"26.01\"}")
				.contentType(contentType)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(containsString("ERROR: Ask can't be NULL!")));
		mockMvc.perform(get("/elvl").param("isin", "RU000A0JX0J7").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().string(""));
	}
	
	@Test
	public void testInitProper() throws Exception {
		System.out.println("Fill DB with generated quotes..");
		
		Map<String, Quote> qm = new HashMap<>();
		for(int i = 0; i < 3; i++) {
			QuoteHistory qh = generateRandomQuoteHistory();
			repository.save(qh);
			qm.put(qh.getQuote().getIsin(), qh.getQuote());
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {}
		}
		
		for(int i = 0; i < 3; i++) {
			for(Quote quote: qm.values()) {
				Quote newQuote = generateNewQuotePriceGoLower(quote, new BigDecimal("0.40"));
				QuoteHistory qh = new QuoteHistory(newQuote);
				repository.save(qh);
				qm.put(newQuote.getIsin(), newQuote);
				try {
					Thread.currentThread().sleep(200);
				} catch (InterruptedException e) {}
			}
		}
		
		for(int i = 0; i < 3; i++) {
			for(Quote quote: qm.values()) {
				Quote newQuote = generateNewQuotePriceGoHigher(quote, new BigDecimal("0.50"));				
				QuoteHistory qh = new QuoteHistory(newQuote);
				repository.save(qh);
				qm.put(newQuote.getIsin(), newQuote);
				try {
					Thread.currentThread().sleep(200);
				} catch (InterruptedException e) {}
			}
		}
		
		
	}
	
	private String generateIsin() {
		String start = "RU000";
		String sample = "QWERTYUIOPASDFGHJKLZXCVBNM0123456789";
		String end = "";
		Random random = new Random();
		for(int i = 0; i < 7; i++) {
			int idx = random.nextInt(sample.length());
			end += sample.charAt(idx);
		}
		return start + end;
	}
	
	private Quote generateRandomQuote() {
		String isin = generateIsin();
		Random random = new Random();
		int r = random.nextInt(350000);
		r = (r < 350) ? 350 : r;   // минимальная цена у нас будет 3,50
		BigDecimal bid = new BigDecimal(r);
		bid = bid.movePointLeft(2);
		BigDecimal ask = bid.add(new BigDecimal("0.72"));
		Quote quote = new Quote(isin, bid, ask);
		
		quote.setElvl( quoteHandler.calculateElvlForNewQuote( quote, quoteHandler.getActualElvlByIsin(quote.getIsin()) ) );
		return quote;
	}
	
	private QuoteHistory generateRandomQuoteHistory() {
		Quote quote = generateRandomQuote();
		QuoteHistory qh = new QuoteHistory(quote);
		return qh;
	}
	
	private Quote generateNewQuotePriceGoLower(Quote quote, BigDecimal subtrahend) {
		String isin = quote.getIsin();
		Quote newQuote = new Quote(isin, quote.getBid().subtract(subtrahend), quote.getAsk().subtract(subtrahend));				
		newQuote.setElvl( quoteHandler.calculateElvlForNewQuote( newQuote, quoteHandler.getActualElvlByIsin(isin) ) );
		return newQuote;
	}
	
	private Quote generateNewQuotePriceGoHigher(Quote quote, BigDecimal augend) {
		String isin = quote.getIsin();
		Quote newQuote = new Quote(isin, quote.getBid().add(augend), quote.getAsk().add(augend));				
		newQuote.setElvl( quoteHandler.calculateElvlForNewQuote( newQuote, quoteHandler.getActualElvlByIsin(isin) ) );
		return newQuote;
	}
	
	@BeforeEach
	private void cleanState() {
		controller.cleanState();
		controller.init();
	}
	
	@AfterEach
	private void cleanDB() {
		repository.deleteAll();
	}
}
