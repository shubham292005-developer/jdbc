package com.javaSQL;

import java.sql.Connection;
import java.sql.DriverManager;

public class DemoExplain {

	public static void main(String[] args) throws Exception {
		/*
		 * import package
		 * load and register 
		 * create connection 
		 * create statement
		 * execute statement process the results
		 * close
		 */
		String url="jdbc:postgresql://localhost:5432/demo";
		String uname="postgres";
		String pass="root123";
		Class.forName("org.postgresql.Driver");
		Connection con =DriverManager.getConnection( url, uname, pass);
		System.out.println("connection Established");

	}

}
