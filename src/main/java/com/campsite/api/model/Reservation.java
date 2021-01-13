package com.campsite.api.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="reservation")
public class Reservation {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="reservation_id")
	Long reservationId;
	
	@Column(name="start_date", unique=true)
	LocalDate startDate;

	@Column(name="num_days", nullable = false)
	Integer numDays;

	@Column(nullable = false)
	String name;
	
	@Column(nullable = false)
	String email;
	
	public Reservation() {	
	}

	public Long getReservationId() {
		return reservationId;
	}

	public void setReservationId(Long reservationId) {
		this.reservationId = reservationId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public Integer getNumDays() {
		return numDays;
	}

	public void setNumDays(Integer numDays) {
		this.numDays = numDays;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Reservation [reservationId=" + reservationId + ", startDate=" + startDate + ", numDays=" + numDays
				+ ", name=" + name + ", email=" + email + "]";
	}

}
