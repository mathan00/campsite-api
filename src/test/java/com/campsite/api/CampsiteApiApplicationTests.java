package com.campsite.api;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.campsite.api.controller.ReservationController;
import com.campsite.api.dao.ReservationDao;
import com.campsite.api.model.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = CampsiteApiApplication.class, webEnvironment = WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureMockMvc
class CampsiteApiApplicationTests {
	private static final String TEST_NAME = "kk";
	private static final String TEST_EMAIL = "kk@gmail.com";
	private static final String TEST_DAYS = "3";
	private static final String TEST_MAX_DAYS = "3";

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");

	@Autowired
	private MockMvc mvc;

	@Autowired
	ReservationDao dao;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@BeforeTestMethod
	void before() {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		
	}

	@Test
	void testCreateFirstReservation() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		

		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", TEST_NAME)
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ReservationId", any(String.class))).andExpect(jsonPath("$.Status", is("Success")));
	}

	@Test
	void testCreateTwoDuplicateReservation() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		

		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", TEST_NAME)
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ReservationId", any(String.class))).andExpect(jsonPath("$.Status", is("Success")));
		
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", TEST_NAME)
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Error"))).andExpect(jsonPath("$.ErrorMsg", is("Cannot book for this time as the the Following days have already been booked!")));

	}

	@Test
	void testCreateTwoOverlappingReservation() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		

		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", TEST_NAME)
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ReservationId", any(String.class))).andExpect(jsonPath("$.Status", is("Success")));
		
		LocalDate startDate2 = LocalDate.of(2021, Month.JANUARY, 14);
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", TEST_NAME)
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate2)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Error"))).andExpect(jsonPath("$.ErrorMsg", is("Cannot book for this time as the the Following days have already been booked!")));

	}
	
	@Test
	void testModifyReservationWithinOldDateRange() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		
		jdbcTemplate.update(
				"insert into reservation (reservation_id, email, name, start_date, num_days) values (1, 'TT1', 'TT1@gmail.com', '2021-01-13', 3)");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (1, '2021-01-13')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (2, '2021-01-14')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (3, '2021-01-15')");

		
		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");

		String newNumDays = "2";
		mvc.perform(post("/api/modify").contentType(MediaType.APPLICATION_JSON).param("id", "1")
				.param("name", TEST_NAME).param("email", TEST_EMAIL).param("startDate", dtf.format(startDate))
				.param("numDays", newNumDays)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Success")));

	}

	@Test
	void testModifyReservationOutsideOldDateRange() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		
		jdbcTemplate.update(
				"insert into reservation (reservation_id, email, name, start_date, num_days) values (1, 'TT1', 'TT1@gmail.com', '2021-01-13', 3)");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (1, '2021-01-13')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (2, '2021-01-14')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (3, '2021-01-15')");

		
		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 16);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");

		String newNumDays = "3";
		mvc.perform(post("/api/modify").contentType(MediaType.APPLICATION_JSON).param("id", "1")
				.param("name", TEST_NAME).param("email", TEST_EMAIL).param("startDate", dtf.format(startDate))
				.param("numDays", newNumDays)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Success")));
		
	}

	
	@Test
	void testModifyReservationOverlappingWithOldDateRange() throws Exception {
		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		
		jdbcTemplate.update(
				"insert into reservation (reservation_id, email, name, start_date, num_days) values (1, 'TT1', 'TT1@gmail.com', '2021-01-13', 3)");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (1, '2021-01-13')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (2, '2021-01-14')");
		jdbcTemplate.update("insert into reserved_days (reservation_day_id, booked_day) values (3, '2021-01-15')");

		
		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 14);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");

		String newNumDays = "3";
		mvc.perform(post("/api/modify").contentType(MediaType.APPLICATION_JSON).param("id", "1")
				.param("name", TEST_NAME).param("email", TEST_EMAIL).param("startDate", dtf.format(startDate))
				.param("numDays", newNumDays)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Success")));

	}

	@Test
	void testCancelReservation() throws Exception {

		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");	
		
		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 10);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		mvc.perform(post("/api/reserve").contentType(MediaType.APPLICATION_JSON).param("name", "TEST_CANCEL")
				.param("email", TEST_EMAIL).param("startDate", dtf.format(startDate)).param("numDays", TEST_DAYS))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ReservationId", any(String.class))).andExpect(jsonPath("$.Status", is("Success")));
		Long id = jdbcTemplate.queryForObject("select reservation_id from reservation where start_date='2021-01-10'", Long.class);
		mvc.perform(get("/api/cancel").contentType(MediaType.APPLICATION_JSON).param("id", id!=null?id.toString():""))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.Status", is("Success")));
		
	}


	@Test
	void testGetAvailability() throws Exception {

		jdbcTemplate.update("delete from reservation");		
		jdbcTemplate.update("delete from reserved_days");		

		LocalDate ld = LocalDate.now();
		LocalDate startDate = ld.plusDays(1);
		LocalDate lastDate = ld.plusMonths(1);
		mvc.perform(get("/api/availability").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.AvailableDates", hasItem(startDate.format(dtf))))
				.andExpect(jsonPath("$.AvailableDates", hasItem(lastDate.format(dtf))));

	}

}
