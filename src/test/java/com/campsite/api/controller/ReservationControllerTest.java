package com.campsite.api.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ReservationControllerTest {

	ReservationController controller = new ReservationController();

	
	@Test
	void reserveTestBoundaryCondition() {
		Map<String, String> retMap = controller.reserve("t1", "t1@gmail.com", "2021-01-01", 0);
		assertEquals("Error", retMap.get("Status"));
		assertTrue(retMap.get("ErrorMsg")!= null? retMap.get("ErrorMsg").contains("Num Days must been within "): false);
		
		retMap = controller.reserve("t1", "t1@gmail.com", "2021-01-01", 4);
		assertEquals("Error", retMap.get("Status"));
		assertTrue(retMap.get("ErrorMsg")!= null? retMap.get("ErrorMsg").contains("Num Days must been within "): false);		
	
	}

}
