package com.zyx.httpUtil;

import java.text.ParseException;

public class test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 System.out.println(""+new java.text.SimpleDateFormat("yyyy MM-dd HH:mm:ss").format(new java.util.Date (1539273600000L)));
		 long epoch = new java.text.SimpleDateFormat ("dd/MM/yyyy HH:mm:ss").parse("12/22/2017 00:00:00").getTime();
		 System.out.println(epoch);
	}

}
