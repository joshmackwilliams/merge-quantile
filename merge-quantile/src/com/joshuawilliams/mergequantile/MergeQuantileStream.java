package com.joshuawilliams.mergequantile;

import java.util.Arrays; // Needed for sorting new data before adding to the ranks

/*
 * MergeQuantileStream - Find median and other percentiles of streamed data with high accuracy. 
 * 
 * The public API of this class is quite simple. To add a data point to the set, simply call 
 * stream(data). To find a particular percentile (a number n such that some percentage of the
 * data is less than n), just call find_percentile(percentage expressed as a double between 0 and 1)
 * 
 * The big advantage of this algorithm is that the whole data set doesn't need to fit in memory. 
 * In fact, the memory complexity is O(log(n)). Streaming a data point or finding a percentile 
 * has complexity O(log(n)) as well. 
 * 
 * Algorithm Description: 
 * 
 * This algorithm works by sorting the data into "ranks", each of a specified size. Each rank has 
 * twice the significance of the last. So, rank 3 will hold the equivalent of (rank_size) * 2^3 
 * data points, while rank 1 will hold (rank_size) * 2^1 data points. 
 * 
 * New data points are added into a temporary array. Once rank_size data points have been gathered, 
 * they transform into rank 0. If we don't have a rank 0 right now, we can simply store it and 
 * start building the next one. However, if we do have a rank 0, we turn the two of them into a rank 1
 * by merging adjacent pairs of data points. So the lowest two points from both sets will become one 
 * point in the new set and so on. 
 * 
 * In this way, each point in a given rank is the equivalent of two data points in a rank below it. 
 * 
 * Then we look for a rank 1. If one already exists, we merge it with the new rank 1 and continue up. 
 * We do this until we find an empty space. The result is something like incrementing a binary number, 
 * with bits combining and carrying into the next place. 
 * 
 * To find a percentile, we simply walk through all of the ranks, counting how many rank 0 data points 
 * have been passed until we find the right number. Then we can return the next data point. 
 */

public class MergeQuantileStream {
	private int rank_size; // Granularity of the data retained. Higher values = more accurate, but more memory used
	private double[][] ranks = new double[1][]; // Holds previously built arrays in order of rank
		// Note that each point in rank i represents 2^i real datapoints
	private double[] building; // The data set we're currently building. Has size x
	private int data_built = 0; // How many data points are in building
	
	public MergeQuantileStream(int rank_size) {
		this.rank_size = rank_size;
		building = new double[rank_size];
	}
	
	// Add a data point to the stream
	public void stream(double data) {
		building[data_built] = data;
		data_built++;
		if(data_built == rank_size) {
			Arrays.sort(building);
			ingest_data(building, 0); // Ingest the current array at rank 0
			building = new double[rank_size];
			data_built = 0;
		}
	}
	
	// Incorporates all the data. Basically walk through the data, tracking how many data points have been 
	// passed, until we reach the percentile we are looking for. 
	public double find_quantile(double percentile) {
		Arrays.sort(building, 0, data_built);
		int[] rank_indices = new int[ranks.length];
		int building_index = 0;
		long data_passed = 0;
		long total_data = data_built;
		for(int i = 0; i < ranks.length; i++) {
			if(ranks[i] != null) {
				total_data += (rank_size * (1 << i));
			}
		}
		long needed_data = (long) (total_data * percentile);
		double last_value = 0;
		while(data_passed <= needed_data) {
			// Find the minimum data point, then pass it
			int minimum_rank = -1;
			double minimum_value = Double.MAX_VALUE;
			for(int i = 0; i < ranks.length; i++) {
				if(ranks[i] == null || rank_indices[i] >= ranks[i].length) { // Skip finished ranks
					continue;
				}
				double value = ranks[i][rank_indices[i]];
				if(value < minimum_value) {
					minimum_value = value;
					minimum_rank = i;
				}
			}
			if(building_index < data_built && building[building_index] < minimum_value) {
				last_value = building[building_index];
				data_passed += 1;
				building_index += 1;
			} else {
				last_value = ranks[minimum_rank][rank_indices[minimum_rank]];
				data_passed += (1 << minimum_rank);
				rank_indices[minimum_rank] += 1;
			}
		}
		return last_value;
	}
	
	// Given a sorted array of a given rank, add the data to the stream
	private void ingest_data(double[] data, int rank) {
		if(rank >= ranks.length) { // We don't have space for this rank
			double[][] new_ranks = new double[rank + 1][];
			for(int i = 0; i < ranks.length; i++) {
				new_ranks[i] = ranks[i]; // Copy old data over
			}
			ranks = new_ranks;
			ranks[rank] = data;
		} else if(ranks[rank] == null) { // We have space but don't have this rank
			ranks[rank] = data;
		} else {
			double[] old_data = ranks[rank];
			ranks[rank] = null;
			ingest_data(merge_ranks(data, old_data), rank + 1);
		}
	}
	
	// Merge two arrays by averaging pairs of adjacent points
	private double[] merge_ranks(double[] input1, double[] input2) {
		double[] output = new double[rank_size];
		int i1 = 0;
		int i2 = 0;
		for(int io = 0; io < rank_size; io++) {
			double data1;
			double data2;
			if(pull_from_first(input1, input2, i1, i2)) {
				data1 = input1[i1];
				i1++;
			} else {
				data1 = input2[i2];
				i2++;
			}
			if(pull_from_first(input1, input2, i1, i2)) {
				data2 = input1[i1];
				i1++;
			} else {
				data2 = input2[i2];
				i2++;
			}
			output[io] = (data1 + data2) / 2;
		}
		return output;
	}
	
	// Helper method to deal with edge cases
	// Returns true if the next data point should be pulled from in1
	private boolean pull_from_first(double[] in1, double[] in2, int i1, int i2) { 
		if(i1 >= in1.length) {
			return false;
		} else if(i2 >= in2.length) {
			return true;
		} else {
			return in1[i1] < in2[i2];
		}
	}
}