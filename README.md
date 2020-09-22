# merge-quantile
An algorithm to find quantiles of streamed data by dividing data points into ranks

The big advantage of this algorithm is that the whole data set doesn't need to fit in memory. In fact, the memory complexity is O(log(n)). Streaming a data point or finding a quantile has complexity O(log(n)) as well. 

## Algorithm Description: 

This algorithm works by sorting the data into "ranks", each of a specified size. Each rank has twice the significance of the last. So, rank 3 will hold the equivalent of (rank_size) * 2^3 data points, while rank 1 will hold (rank_size) * 2^1 data points. 

New data points are added into a temporary array. Once rank_size data points have been gathered, they transform into rank 0. If we don't have a rank 0 right now, we can simply store it and start building the next one. However, if we do have a rank 0, we turn the two of them into a rank 1 by merging adjacent pairs of data points. So the lowest two points from both sets will become one point in the new set and so on. 

In this way, each point in a given rank is the equivalent of two data points in a rank below it. 

Then we look for a rank 1. If one already exists, we merge it with the new rank 1 and continue up. We do this until we find an empty space. The result is something like incrementing a binary number, with bits combining and carrying into the next place. 

To find a quantile, we simply walk through all of the ranks, counting how many rank 0 data points have been passed until we find the right number. Then we can return the next data point. 
