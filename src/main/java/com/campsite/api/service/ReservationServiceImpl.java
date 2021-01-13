package com.campsite.api.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.campsite.api.dao.ReservationDao;
import com.campsite.api.dao.ReservationDaoImpl;
import com.campsite.api.model.Reservation;

@Component
public class ReservationServiceImpl implements ReservationService {
	final Logger logger = LoggerFactory.getLogger(ReservationDaoImpl.class);

	
	@Autowired
	ReservationDao dao;
	
	@Transactional
	@Override
	public String addReservation(String name, String email, LocalDate startDate, int numDays) {
		logger.info("addReservation Callled with name:{}, email:{}, startDate:{}, numDays:{}", name,
				email, startDate, numDays);
		if (numDays < ReservationService.MIN_DAYS_TO_BOOK && numDays > ReservationService.MAX_DAYS_TO_BOOK) {
			throw new RuntimeException("Reservation duration exceeds min and Max number of days");
		}
		if (startDate == null) {
			throw new RuntimeException("Reservation startDate cannot be null");
		}
		
		/**
		 * Even though currently the max days user can reserve is only three, the approach can handle any number of duration
		 */
		// I need to go back max number of days to book because earliest book that could have started but wouldn't have start date entry in 
		// database will be current day - MAX Days to Book.
		LocalDate nStartDate = startDate.minusDays(ReservationService.MAX_DAYS_TO_BOOK);
		LocalDate endDate = startDate.plusDays(numDays);
		List<Reservation> reservations = dao.bookedDates(nStartDate, endDate);
		Stream<List<LocalDate>> localDateListStream = reservations.stream().map(r->{
			List<LocalDate> localDates = new ArrayList<>();
			LocalDate sDate = r.getStartDate();
			for (int i=0; i < r.getNumDays() ; i++) {
				localDates.add(sDate.plusDays(i));			
			}
			return localDates;
		});
		Stream<LocalDate> localDateStream = localDateListStream.flatMap(ld -> ld.stream());
		Set<LocalDate> uniqueLocalDate = localDateStream.filter(l -> l.isAfter(startDate.minusDays(1))).collect(Collectors.toSet());
		logger.info("Found already booked dates: {} within the startDate:{} and endDate:{}", uniqueLocalDate, startDate, endDate);
		if (!uniqueLocalDate.isEmpty()) {
			throw new RuntimeException(String.format("Cannot book for this time as the the Following days have already been booked!", uniqueLocalDate));
		}
		
		Reservation r1 = new Reservation();
		r1.setName(name);
		r1.setEmail(email);
		r1.setStartDate(startDate);
		r1.setNumDays(numDays);
		dao.create(r1);
		
		return r1.getReservationId() != null? r1.getReservationId().toString(): "";
	}

	/**
	 * The campsite can be booked 1 day in adavance and upto 1 month in advance.
	 * Check availaility from tomorrow to 1 month in advance
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<LocalDate> checkAvailability() {
		LocalDate currentDate = LocalDate.now();
		LocalDate min = currentDate.plusDays(1);
		LocalDate max = currentDate.plusMonths(1);
		logger.info("availability is checked with MinDate:{} and MaxDate:{} and currentDate:{}", min, max, currentDate);
		List<Reservation> bookedDatesRes = dao.bookedDates(min, max);
		logger.info("already booked reservations: {}", bookedDatesRes);
//		Set<LocalDate> bookedDates = 
				
		Stream<List<LocalDate>> localDateListStream = bookedDatesRes.stream().map(r-> {
			LocalDate l = r.getStartDate();
			List<LocalDate> dates = new ArrayList<>();
			dates.add(l);
			for (int i=0; i < r.getNumDays(); i++) {
				LocalDate l2 = l.plusDays(i);
				dates.add(l2);
			}
			return dates;
		});
		
		Stream<LocalDate> localDateStream = localDateListStream.flatMap(e-> {
			return e.stream();
		});
		
		Set<LocalDate> bookedDateSet = localDateStream.collect(Collectors.toSet());
		
		List<LocalDate> availableDates = new ArrayList<>();
		//between function is excluded the max date and as per our business requirement, 
		// end date is inclusive.
		int numberOfDays = (int) ChronoUnit.DAYS.between(min, max) + 1;
		for (int i=0; i < numberOfDays; i++) {
			LocalDate cDate = min.plusDays(i);
			if (!bookedDateSet.contains(cDate)) {
				availableDates.add(min.plusDays(i));				
			}
		}
		logger.info("Booked Dates: {}", bookedDateSet);
		logger.info("Available Dates to book: {}", availableDates);
		
		return availableDates;
	}
	
	@Transactional
	@Override
	public void cancelReservation(String id) {
		logger.info("cancelReservation called with id:{}", id);
		Long nId = null;
		try{
			nId = Long.parseLong(id);
		}catch(NumberFormatException e) {
			throw new RuntimeException(String.format("invalid id passed: %s", id));
		}
		Reservation r = dao.get(nId);
		if (r == null) {
			throw new RuntimeException(String.format("There is no Reservation found with the Reservation Id: %s", id));
		}
		dao.delete(nId);	
		logger.info("removed reservation with id:{}", id);
	}
	
	@Transactional
	public void modifyReservation(String id, String name, String email, LocalDate startDate, int numDays) {
		logger.info("modifyReservation called with id:{}, Name:{}, Email:{}, startDate:{}, numDays:{}",
				id, name, email, startDate, numDays);
		Long nId = null;
		try{
			nId = Long.parseLong(id);
		}catch(NumberFormatException e) {
			throw new RuntimeException(String.format("invalid id passed: %s", id));
		}
		Reservation currentReservation = dao.get(nId);
		if (currentReservation == null) {
			throw new RuntimeException(String.format("There is no Reservation found with the Reservation Id: %s", id));
		}else {
			//simpler case where only name and email information is updated
			if (currentReservation.getStartDate().isEqual(startDate) && currentReservation.getNumDays() == numDays) {
				logger.info("The Start Date and NumDays for old reservation and new Reservation are same for id:{}", currentReservation.getReservationId());
				if (!name.equals(currentReservation.getName()) || !email.equals(currentReservation.getEmail()) ) {
					logger.info("Updating Email and Name as existing record values are differnt from user entered for ResId:{}", currentReservation.getReservationId());
					currentReservation.setEmail(email);
					currentReservation.setName(name);
					dao.update(currentReservation, currentReservation.getStartDate(), currentReservation.getNumDays());
					logger.info("Reservation has been updated for id:{}", currentReservation.getReservationId());
					return;
				}else {
					logger.warn("Nothing to update in the reservation as all the details are same for id:{}", currentReservation.getReservationId());
					return;
				}
			}
		}

		if (numDays < ReservationService.MIN_DAYS_TO_BOOK && numDays > ReservationService.MAX_DAYS_TO_BOOK) {
			throw new RuntimeException("Reservation duration exceeds min and Max number of days");
		}
		if (startDate == null) {
			throw new RuntimeException("Reservation startDate cannot be null");
		}
		/**
		 * Even though currently the max days user can reserve is only three, the approach can handle any number of duration
		 */
		// I need to go back max number of days to book because earliest book that could have started but wouldn't have start date entry in 
		// database will be current day - MAX Days to Book.
		LocalDate nStartDate = startDate.minusDays(ReservationService.MAX_DAYS_TO_BOOK);
		LocalDate endDate = startDate.plusDays(numDays);
		List<Reservation> reservations = dao.bookedDates(nStartDate, endDate);
		//filter out the current record from the list. because at modifying to a new date will be same as finding available slots without having
		//the current record. Also have identified the case where user is only modifying the name and email without modifying exiting startDate and numDays above.		
		reservations = reservations.stream().filter(r->!r.getReservationId().equals(currentReservation.getReservationId())).collect(Collectors.toList());
		Stream<List<LocalDate>> localDateListStream = reservations.stream().map(
				r->{
			List<LocalDate> localDates = new ArrayList<>();
			LocalDate sDate = r.getStartDate();
			for (int i=0; i < r.getNumDays(); i++) {
				localDates.add(sDate.plusDays(i));			
			}
			return localDates;
		});
		Stream<LocalDate> localDateStream = localDateListStream.flatMap(ld -> ld.stream());
		Set<LocalDate> uniqueLocalDate = localDateStream.filter(l -> l.isAfter(startDate.minusDays(1))).collect(Collectors.toSet());
		logger.info("Already Booked Dates: {} founds with in the new Reservation startDate:{} and endDate:{} ", uniqueLocalDate, startDate, endDate);
		if (!uniqueLocalDate.isEmpty()) {
			throw new RuntimeException(String.format("Cannot book for this time as the the Following days have already been booked!", uniqueLocalDate));
		}
		
		LocalDate oldStartDate = currentReservation.getStartDate();
		int oldNumDays = currentReservation.getNumDays();
		currentReservation.setName(name);
		currentReservation.setEmail(email);
		currentReservation.setStartDate(startDate);
		currentReservation.setNumDays(numDays);
		dao.update(currentReservation, oldStartDate, oldNumDays);
		
				
	}

	public ReservationDao getDao() {
		return dao;
	}

	public void setDao(ReservationDao dao) {
		this.dao = dao;
	}
}
