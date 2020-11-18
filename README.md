# merge-quantile
An algorithm to find quantiles of streamed data by dividing data points into ranks

The big advantage of this algorithm is that the whole data set doesn't need to fit in memory. In fact, the memory complexity is O(log(n)). Streaming a data point or finding a quantile has complexity O(log(n)) as well. 

## Algorithm Description: 

Every data point stored by this algorithm has a "rank". 

The algorithm has a parameter called rank_size, which defaults to 1024. For the remainder of this discussion we will assume that it is set to 1024 for simplicity. 

Initially, data points have a rank of 0. Once 2048 rank 0 data points have been collected, they merge into 1024 data points of rank 1. 

Ranks are merged by taking the lowest two rank 0 points and averaging them to find a rank 1 point. The rank 0 points are then removed and the process is repeated with the next-lowest pair of points. The result is that each rank 1 point represents two rank 0 points. 

This merging process is repeated when 2048 data points of any rank are collected. So when 2048 data points of rank n are collected, they are merged as described above to create 1024 data points of rank 5. The result of this merging is that a datapoint of rank "n" is made from "2^n" datapoints of rank 0. 

So for example, once 5,120 (=1024+4096) data points have been collected, we will have 1024 rank 0 datapoints, no rank 1 datapoints, and 1024 rank 2 datapoints. 

In order to find a particular quantile, datapoints of all ranks are considered. Beginning with the lowest datapoint and proceeding in sorted order, the algorithm counts the number of equivalent rank 0 datapoints that it has passed. Once the correct fraction has been passed, the algorithm reports the most recent datapoint as the quantile. 

For example, let's say we are trying to find the median of the above mentioned set, which was fed 5,120 datapoints. We need to pass 2,560 (5120/2=2560) of the datapoints before reaching the median. So the algorithm will step through the datapoints, lowest to highest by value. Each rank 2 datapoint represents 2^2 = 4 rank 0 datapoints and so passing a rank 2 datapoint counts as passing 4 rank 0 datapoints. Each rank 0 datapoint counts as only 1 datapoint. So, we could get to 2,560 in many ways. If the lowest 640 datapoints happened to be rank 2, we would pass those and be done (640\*4 = 2560). If we passed, say, 512 rank 0 datapoints and 512 rank 2 datapoints, we would also be done (512\*4 + 512 = 2560). 
