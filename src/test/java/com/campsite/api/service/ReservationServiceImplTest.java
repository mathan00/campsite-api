package com.campsite.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.campsite.api.dao.ReservationDao;
import com.campsite.api.model.Reservation;

class ReservationServiceImplTest {
	public static final String TEST_NAME="kk1";
	public static final String TEST_EMAIL="kk1@gmail.com";
	public static final int TEST_MAX_NUM_DAYS=3;
	
	
	ReservationServiceImpl service = null; 
	ReservationDao dao = null;
	
	@BeforeEach
	void setUp() throws Exception {
		service = new ReservationServiceImpl();
		dao = mock(ReservationDao.class);
		service.setDao(dao);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testAddReservation() {
		LocalDate startDate = LocalDate.of(2021, Month.JANUARY, 13);
		int numDays = 3;
		LocalDate minDate = startDate.minusDays(TEST_MAX_NUM_DAYS);
		LocalDate maxDate = startDate.plusDays(numDays);
		List<Reservation> reservations = new ArrayList<>();
		
		Mockito.when(dao.bookedDates(minDate, maxDate)).thenReturn(reservations);
		Reservation r1 = new Reservation();
		r1.setName(TEST_NAME);
		r1.setEmail(TEST_EMAIL);
		r1.setNumDays(TEST_MAX_NUM_DAYS);
		r1.setStartDate(startDate);
		r1.setReservationId(null);
		String id = service.addReservation(TEST_NAME, TEST_EMAIL, startDate, TEST_MAX_NUM_DAYS);
		assertEquals("", id);
	}

	@Test
	void testCheckAvailability() {
		LocalDate ld = LocalDate.now();
		LocalDate min = ld.plusDays(1);
		LocalDate max = ld.plusMonths(1);
		List<Reservation> reservations = new ArrayList<>();
		Reservation r1 = new Reservation();
		r1.setReservationId(1l);
		r1.setName(TEST_NAME);
		r1.setEmail(TEST_EMAIL);
		r1.setNumDays(TEST_MAX_NUM_DAYS);
		r1.setStartDate(min);
		reservations.add(r1);
		Mockito.when(dao.bookedDates(min, max)).thenReturn(reservations);
		List<LocalDate> result = service.checkAvailability();
		Optional<LocalDate> optionalResult = result.stream().filter(r->r.isEqual(min)).findFirst();
		Optional<LocalDate> optionalResult2 = result.stream().filter(r->r.isEqual(min.plusDays(1))).findFirst();
		Optional<LocalDate> optionalResult3 = result.stream().filter(r->r.isEqual(min.plusDays(2))).findFirst();
		boolean testResult = false;
		if (!optionalResult.isPresent() && 
				!optionalResult2.isPresent() &&
					!optionalResult3.isPresent() ) {
			testResult = true;
		}
		assertTrue(testResult, "expected booked dates were removed from available dates");
	}

	@Test
	void testCancelReservation() {
		Reservation r = new Reservation();
		r.setEmail(TEST_EMAIL);
		r.setName(TEST_NAME);
		r.setNumDays(TEST_MAX_NUM_DAYS);
		r.setStartDate(LocalDate.of(2021, Month.JANUARY, 13));
		r.setReservationId(1l);
		Mockito.when(dao.get(1l)).thenReturn(r);
		service.cancelReservation("1");
		Mockito.verify(dao, times(1)).delete(1l);
	
	}


}
