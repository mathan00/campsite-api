package com.campsite.api.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campsite.api.Util;
import com.campsite.api.dao.ReservationDaoImpl;
import com.campsite.api.service.ReservationService;

@RestController
public class ReservationController {
	final Logger logger = LoggerFactory.getLogger(ReservationDaoImpl.class);

	@Autowired
	ReservationService service;
	
	@RequestMapping(path = "/api/availability", method = RequestMethod.GET)
	public Map<String, List<LocalDate>> checkAvailability() {
		List<LocalDate> list = service.checkAvailability();
		System.out.println("Available Dates: "+list);
		Map<String, List<LocalDate>> retMap = new HashMap<>();
		retMap.put("AvailableDates", list);
		return retMap;
	}
	
	@RequestMapping(path = "/api/reserve", method = RequestMethod.POST)
	public Map<String, String> reserve(@RequestParam String name, @RequestParam String email, 
			@RequestParam String startDate,@RequestParam Integer numDays) {
		logger.info("reserve called with Name:{}, Email:{}, StartDate: {}, NumDays: {}", 
					name, email, startDate, numDays);

		//If null or invalid int is provided then default value is used
		int numDaysInt = 0;
		try {
			numDaysInt = numDays!= null? numDays.intValue() : ReservationService.DEFAULT_DAYS_TO_BOOK;			
		}catch (Exception e) {
			numDaysInt = ReservationService.DEFAULT_DAYS_TO_BOOK;
		}

		if (numDaysInt > ReservationService.MAX_DAYS_TO_BOOK || numDaysInt < ReservationService.MIN_DAYS_TO_BOOK) {
			return Util.createMapWithErrorStatusAndMsg(String.format("Num Days must been within %s and %s", ReservationService.MIN_DAYS_TO_BOOK, ReservationService.MAX_DAYS_TO_BOOK));
		}
		
		LocalDate startDateLD = null;
		try {
			startDateLD = LocalDate.parse(startDate, ReservationService.DTF_uuuu_MM_dd);
		}catch (DateTimeParseException e) {
			return Util.createMapWithErrorStatusAndMsg(String.format("Start Date provided is Invalid. Provided Start Date:%s",
					startDateLD));
		}
		String reservationId = null;
		try {
			reservationId = service.addReservation(name, email, startDateLD, numDaysInt);			
		}catch(Exception e) {
			return Util.createMapWithErrorStatusAndMsg(String.format(e.getMessage()));		
		}
		Map<String, String> retMap = new HashMap<>();
		retMap.put("Status", "Success");
		retMap.put("ReservationId", reservationId);
		return retMap;
	}

	@RequestMapping(path = "/api/cancel", method = RequestMethod.GET)
	public Map<String, String> cancelReservation(@RequestParam(name = "id", required = true) String reservationId) {
		Map<String, String> rMap = new HashMap<>();
		try {
			service.cancelReservation(reservationId);					
		}catch(Exception e) {
			rMap.put("Status", "Error while Cancelling");
			rMap.put("ERROR_MSG", e.getMessage());
			return rMap;

		}
		rMap.put("Status", "Success");
		return rMap;
	}
	
	@RequestMapping(path = "/api/modify", method = RequestMethod.POST)
	public Map<String, String> modifyReservation(@RequestParam String id,
			@RequestParam String name, @RequestParam String email, 
			@RequestParam String startDate,@RequestParam Integer numDays
			) {
		logger.info("modify called with id:{}, Name:{}, Email:{}, StartDate:{}, NumDays:{} ",
				id, name, email, startDate, numDays);
		//If null or invalid int is provided then default value is used
		int numDaysInt = 0;
		try {
			numDaysInt = numDays!= null? numDays.intValue() : ReservationService.DEFAULT_DAYS_TO_BOOK;			
		}catch (Exception e) {
			numDaysInt = ReservationService.DEFAULT_DAYS_TO_BOOK;
		}
	
		if (numDaysInt > ReservationService.MAX_DAYS_TO_BOOK && numDaysInt < ReservationService.MIN_DAYS_TO_BOOK) {
			return Util.createMapWithErrorStatusAndMsg(String.format("Num Days must been within %s and %s", ReservationService.MIN_DAYS_TO_BOOK, ReservationService.MAX_DAYS_TO_BOOK));
		}
		
		LocalDate startDateLD = null;
		try {
			startDateLD = LocalDate.parse(startDate, ReservationService.DTF_uuuu_MM_dd);
		}catch (DateTimeParseException e) {
			return Util.createMapWithErrorStatusAndMsg(String.format("Start Date provided is Invalid. Provided Start Date:%s",
					startDateLD));
		}
		try {
			service.modifyReservation(id, name, email, startDateLD, numDays);			
			return Util.createSuccessMap();
		}catch(Exception e) {
			//in any of the error scenarios
			return Util.createMapWithErrorStatusAndMsg(e.getMessage());
		}
	}

}
