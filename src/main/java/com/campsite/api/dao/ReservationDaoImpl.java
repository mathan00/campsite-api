package com.campsite.api.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.campsite.api.model.Reservation;
import com.campsite.api.model.ReservedDay;

@Component
public class ReservationDaoImpl implements ReservationDao {

	final Logger logger = LoggerFactory.getLogger(ReservationDaoImpl.class);
	
	@Autowired
	EntityManager em;
	
	@Value("${multi-spring-boot-app-enabled:true}")
	boolean multiSpringBootAppRunning;
	
	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void create(Reservation reservation) {

		//if multiple spring boot app instance is running, at database level, integrity will be maintained.
		if (multiSpringBootAppRunning) {
			LocalDate ld = reservation.getStartDate();
			for (int i=0; i<reservation.getNumDays(); i++) {
				ReservedDay rd = new ReservedDay();
				rd.setBookedDay(ld.plusDays(i));			
				em.persist(rd);
			}
		}
		em.persist(reservation);
		
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Reservation get(Long reservationId) {
		return em.find(Reservation.class, reservationId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void update(Reservation reservation, LocalDate curStartDate, int curNumDays) {
		if (multiSpringBootAppRunning) {
			Set<LocalDate> newUniqueDates = new HashSet<>();
			for (int i=0; i< reservation.getNumDays(); i++) {
				newUniqueDates.add(reservation.getStartDate().plusDays(i));
			}
			Set<LocalDate> currentUniqueDates = new HashSet<>();
			for (int j=0; j<curNumDays; j++) {
				LocalDate n1 = curStartDate.plusDays(j);
				currentUniqueDates.add(n1);
				if (newUniqueDates.contains(n1)){
				}else {
					//remove it
					Query query = em.createQuery("select r from ReservedDay r where r.bookedDay = :d");
					query = query.setParameter("d", n1);
					List<ReservedDay> list = query.getResultList();
					if (list != null && list.get(0)!= null) {
						em.remove(list.get(0));
					}					
				}
			}
			newUniqueDates.removeAll(currentUniqueDates);
			newUniqueDates.stream().forEach(c->{
				//add new dates that were not in previous date rang
				ReservedDay rd = new ReservedDay();
				rd.setBookedDay(c);			
				em.persist(rd);
			});
		}
		em.merge(reservation);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(Long reservationId) {
		Reservation r = em.find(Reservation.class, reservationId);
		if (multiSpringBootAppRunning) {
			LocalDate ld = r.getStartDate();
			for (int i=0; i< r.getNumDays(); i++) {
				Query query = em.createQuery("select r from ReservedDay r where r.bookedDay = :d");
				query = query.setParameter("d", ld.plusDays(i));
				List<ReservedDay> list = query.getResultList();
				if (list != null && list.get(0)!= null) {
					em.remove(list.get(0));
				}
			}
		}
		em.remove(r);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<Reservation> bookedDates(LocalDate min, LocalDate max) {	
		logger.info("bookedDates with Params min:{} and max:{}", min, max);
//		System.out.println(String.format("ReservationDao bookedDates start:%s and end:%s",  min, max));
		Query query = em.createQuery("select r from Reservation r where r.startDate between :d1 and :d2");
		query = query.setParameter("d1", min);
		query = query.setParameter("d2", max);
		List<Reservation> list = query.getResultList();
		logger.info("already Booked Dates size:{}", list != null? list.size(): 0);
		return list;
	}	

}
