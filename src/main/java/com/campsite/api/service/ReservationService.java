package com.campsite.api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface ReservationService {
	
	public static int MAX_DAYS_TO_BOOK = 3;
	public static int MIN_DAYS_TO_BOOK = 1;

	public static int DEFAULT_DAYS_TO_BOOK = 1;

	public static final DateTimeFormatter DTF_uuuu_MM_dd = DateTimeFormatter.ofPattern("uuuu-MM-dd");

	String addReservation(String name, String email, LocalDate startDate, int numDays);
	
	List<LocalDate> checkAvailability();
	
	void cancelReservation(String id);
	
	void modifyReservation(String id, String name, String email, LocalDate startDate, int numDays);

}
