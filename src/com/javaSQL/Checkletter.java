package com.javaSQL;
import java.util.Scanner;

public class Checkletter {

	public static void main(String[] args) {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter the Letter");
		char ch=sc.next().charAt(0);
		if(ch>='a'  && ch<='z') {
			System.out.println("lowercase");
			
		}else {
			System.out.println("UPPERCASE");
		}
		// TODO Auto-generated method stub

	}

}
