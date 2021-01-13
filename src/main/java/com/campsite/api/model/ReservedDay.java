package com.campsite.api.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="reserved_days")
public class ReservedDay {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "reservation_day_id")
	Long reservationDayId;
	
	@Column(name="booked_day", unique = true)
	LocalDate bookedDay;
	
	public ReservedDay() {
		
	}

	public Long getReservationDayId() {
		return reservationDayId;
	}

	public void setReservationDayId(Long reservationDayId) {
		this.reservationDayId = reservationDayId;
	}

	public LocalDate getBookedDay() {
		return bookedDay;
	}

	public void setBookedDay(LocalDate bookedDay) {
		this.bookedDay = bookedDay;
	}
	
}
