#!/bin/bash

#example: ./build_charts.sh 10 10

ID=1
NUM_OF_TRS=$1 # max number of transactions per second 
INCREMENT=$2 # the increment of transactions in each experiment
NUM_OF_CLIENTS=$3 # number of clients for multi-client part
# single client

rm -f /tmp/response_time*
rm -f /tmp/workload*
rm -f /tmp/client*
rm -f /tmp/transaction*

./performance_analysis.sh $ID $NUM_OF_TRS $INCREMENT false 20 transaction_single_rm.txt
tr '\n' ', ' < /tmp/"response_time_$ID".txt | cat > /tmp/response_time.txt
tr '\n' ', ' < /tmp/"workload_$ID".txt | cat > /tmp/workload.txt
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt

sleep 2s

./performance_analysis.sh $ID $NUM_OF_TRS $INCREMENT false 20 transaction_multiple_rm.txt
echo "" >> /tmp/response_time.txt
echo "" >> /tmp/workload.txt
tr '\n' ', ' < /tmp/"response_time_$ID".txt | cat >> /tmp/response_time.txt
tr '\n' ', ' < /tmp/"workload_$ID".txt | cat >> /tmp/workload.txt

python3 build_figures.py -w /tmp/workload.txt -t /tmp/response_time.txt -l "one ResourceManager,three ResourceManagers" -n 'Single Client' -o "single_client.pdf"

rm -f /tmp/response_time*
rm -f /tmp/workload*
rm -f /tmp/client*
rm -f /tmp/transaction*

sleep 3.s

# multi-client

# start clients in loop
for CLIENT in $(seq 1 $NUM_OF_CLIENTS); do
	./performance_analysis.sh $CLIENT $NUM_OF_TRS $INCREMENT true 70 transaction_multiple_rm.txt &
	sleep 2s
done
wait

#CLIENT=1

for LINE in $(seq 1 $((NUM_OF_TRS / INCREMENT)));
do	
	NUM=0
	# for each file
	for ID in $(seq 1 $NUM_OF_CLIENTS);
	do
		TMP_NUM=`sed -n "$LINE"p < /tmp/"response_time_$ID.txt"`
		NUM=$((NUM + TMP_NUM))	
	done
	echo "$((NUM / NUM_OF_CLIENTS))" >> /tmp/response_time_tmp.txt
done

tr '\n' ', ' < /tmp/response_time_tmp.txt | cat > /tmp/response_time.txt

WORKLOAD=`seq 1 $INCREMENT $NUM_OF_TRS`
echo "$WORKLOAD" > /tmp/workload_tmp.txt
tr '\n' ', ' < /tmp/"workload_tmp".txt | cat > /tmp/workload.txt

python3 build_figures.py -w /tmp/workload.txt -t /tmp/response_time.txt -l "avg. of $NUM_OF_CLIENTS clients" -n "Multi-Client" -o "multi_client.pdf"

rm -f /tmp/response_time*
rm -f /tmp/workload*
rm -f /tmp/client*
rm -f /tmp/transaction*
