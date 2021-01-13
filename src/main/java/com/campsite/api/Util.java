package com.campsite.api;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class Util {

	public static Date getSqlDateFromLocalDate(LocalDate localDate) {
		return new java.sql.Date(localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
	}

	public static Map<String, String> createMapWithErrorStatusAndMsg(String errorMsg){
		Map<String, String> m = new HashMap<>();
		m.put("Status", "Error");
		m.put("ErrorMsg", errorMsg);
		return m;
	}
	
	public static Map<String, String> createSuccessMap(){
		Map<String, String> m = new HashMap<>();
		m.put("Status", "Success");
		return m;
	}
	
}
