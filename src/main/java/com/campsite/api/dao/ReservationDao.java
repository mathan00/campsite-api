package com.campsite.api.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.campsite.api.model.Reservation;


public interface ReservationDao {
	
	void create(Reservation reservation);
	Reservation get(Long reservationId);
	void update(Reservation reservation, LocalDate curStartDate, int curNumDays);
	void delete(Long reservationId);
	List<Reservation> bookedDates(LocalDate min, LocalDate max);
	

}
