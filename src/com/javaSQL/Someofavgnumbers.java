package com.javaSQL;

import java.util.Scanner;

public class Someofavgnumbers {

    // Method to calculate sum
    public static int Calculatesum(int[] numbers) {
        int sum = 0;
        for (int num : numbers) {
            sum = sum + num;
        }
        return sum;
    }

    // Method to calculate average
    public static double Avgnumbers(int sum, int count) {
        return (double) sum / count;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Input: How many numbers
        System.out.println("Enter how many numbers:");
        int n = sc.nextInt();

        int[] numbers = new int[n];

        // Input: n numbers
        System.out.println("Enter " + n + " numbers:");
        for (int i = 0; i < n; i++) {
            numbers[i] = sc.nextInt();
        }

        // Calculate sum and average
        int sum = Calculatesum(numbers);
        double avg = Avgnumbers(sum, n);

        // Output
        System.out.println("Sum = " + sum);
        System.out.println("Average = " + avg);
    }
}
