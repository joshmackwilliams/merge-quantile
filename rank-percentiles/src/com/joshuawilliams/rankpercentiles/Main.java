package com.joshuawilliams.rankpercentiles;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// Test RankPercentilesStream using an unevenly distributed data set
public class Main {
	public static void main(String[] args) {
		int n_datapoints = 1000000; // Number of data points to generate
		double percent_large = 0.25; // Percentage of data points that should be large
		double percentile = 0.75; // Percentile to try to find
		double[] datapoints = new double[n_datapoints];
		Random random = ThreadLocalRandom.current();
		for(int i = 0; i < n_datapoints; i++) {
			datapoints[i] = random.nextDouble();
			if(random.nextDouble() < percent_large) {
				datapoints[i] *= 1000000;
			}
		}
		shuffleArray(datapoints);
		
		// Try unsorted data points
		RankPercentilesStream stream = new RankPercentilesStream(1024);
		for(int i = 0; i < n_datapoints; i++) {
			stream.stream(datapoints[i]);
		}
		double unsorted_prediction = stream.find_percentile(percentile);
		
		Arrays.sort(datapoints); // Try sorted datapoints
		
		stream = new RankPercentilesStream(1024);
		for(int i = 0; i < n_datapoints; i++) {
			stream.stream(datapoints[i]);
		}
		double sorted_prediction = stream.find_percentile(percentile);
		double true_value = datapoints[(int) Math.floor(datapoints.length * percentile)];
		
		reverseArray(datapoints);
		stream = new RankPercentilesStream(1024);
		for(int i = 0; i < n_datapoints; i++) {
			stream.stream(datapoints[i]);
		}
		double reversed_prediction = stream.find_percentile(percentile);
		
		Arrays.sort(datapoints);
		
		double unsorted_error_percent = calculate_error_percent(datapoints, percentile, unsorted_prediction);
		double sorted_error_percent = calculate_error_percent(datapoints, percentile, sorted_prediction);
		double reversed_error_percent = calculate_error_percent(datapoints, percentile, reversed_prediction);
		System.out.println("True value: " + true_value);
		System.out.println("Unsorted prediction: " + unsorted_prediction);
		System.out.println("Sorted prediction: " + sorted_prediction);
		System.out.println("Reversed prediction: " + reversed_prediction);
		System.out.println("Unsorted error: " + unsorted_error_percent);
		System.out.println("Sorted error: " + sorted_error_percent);
		System.out.println("Reversed error: " + reversed_error_percent);
	}
	
	private static double calculate_error_percent(double[] data, double percentile, double datapoint) {
		int n_data_below = 0;
		while(n_data_below < data.length && data[n_data_below] < datapoint) {
			n_data_below++;
		}
		double percent_below = ((double)n_data_below) / ((double)data.length);
		return Math.abs(percentile - percent_below);
	}
	
	private static void shuffleArray(double[] ar) {
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			double a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
	
	private static void reverseArray(double[] ar) {
		for(int i = 0; i < ar.length/2; i++) {
			double swap = ar[ar.length - i - 1];
			ar[ar.length - i - 1] = ar[i];
			ar[i] = swap;
		}
	}
}
