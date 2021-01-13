package com.campsite.api;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.campsite.api.dao.ReservationDao;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = CampsiteApiApplication.class, webEnvironment = WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureMockMvc
public class CampsiteApiApplicationTests2 {
	private static final String TEST_NAME="kk";
	private static final String TEST_EMAIL="kk@gmail.com";
	private static final String TEST_DAYS="3";
	private static final String TEST_MAX_DAYS="3";
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
	
   @Autowired
   private MockMvc mvc;
	   
   @Autowired
   ReservationDao dao;
   
   @Autowired
   JdbcTemplate jdbcTemplate;
   
   @Autowired
   ObjectMapper objectMapper;

	@Test
	void testManyReservationsAllShouldFailNEW() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		
		
		int total=50;
		int success=0;
		int error=0;
		for (int i=0; i<50; i++) {
			LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
			MvcResult mvcResult = null;

		
			try {
				
				mvcResult = mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON)
						.param("name", TEST_NAME)
						.param("email", TEST_EMAIL)
						.param("startDate", dtf.format(startDate))
						.param("numDays", TEST_DAYS)
						)
				.andExpect(status().isOk())
				.andReturn();
				if (mvcResult.getResponse().getContentAsString().contains("Success")) {
					success++;
				}else {
					error++;
				}
			}catch(Exception e) {
				System.out.println("Error");
				error++;
			}
		}


		List<Map<String, Object>> listOfMap = jdbcTemplate.queryForList("select * from reservation", new Object[] {});
		assertEquals(1, listOfMap.size());
		assertEquals(1, success);		
		assertEquals(total-1, error);
	}	

}
