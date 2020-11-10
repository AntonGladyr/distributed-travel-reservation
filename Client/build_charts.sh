#!/bin/bash

#example: ./build_charts.sh 10 10

ID=1
NUM_OF_TRS=$1 # max number of transactions per second 
NUM_OF_CLIENTS=$2 # number of clients for multi-client part
# single client

rm -f /tmp/response_time.txt
rm -f /tmp/workload.txt

./performance_analysis.sh $ID $NUM_OF_TRS false transaction_single_rm.txt
tr '\n' ', ' < /tmp/"response_time_$ID".txt | cat > /tmp/response_time.txt
tr '\n' ', ' < /tmp/"workload_$ID".txt | cat > /tmp/workload.txt
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt

sleep 1.75s

./performance_analysis.sh $ID $NUM_OF_TRS false transaction_multiple_rm.txt
echo "" >> /tmp/response_time.txt
echo "" >> /tmp/workload.txt
tr '\n' ', ' < /tmp/"response_time_$ID".txt | cat >> /tmp/response_time.txt
tr '\n' ', ' < /tmp/"workload_$ID".txt | cat >> /tmp/workload.txt
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt

python3 build_figures.py -w /tmp/workload.txt -t /tmp/response_time.txt -l "one ResourceManager,three ResourceManagers" -n 'Single Client' -o "single_client.pdf"

rm -f /tmp/response_time.txt
rm -f /tmp/workload.txt

# multi-client

# start clients in loop
for CLIENT in $(seq 1 $((NUM_OF_CLIENTS - 1)))
do
	./performance_analysis.sh $CLIENT $NUM_OF_TRS true transaction_single_rm.txt &
done

./performance_analysis.sh $NUM_OF_CLIENTS $NUM_OF_TRS true transaction_single_rm.txt &
CLIENT_PID=`echo $!`
wait $CLIENT_PID

tr '\n' ', ' < /tmp/"response_time_$NUM_OF_CLIENTS".txt | cat > /tmp/response_time.txt
tr '\n' ', ' < /tmp/"workload_$NUM_OF_CLIENTS".txt | cat > /tmp/workload.txt
#rm -f /tmp/"response_time_$ID".txt
#rm -f /tmp/"workload_$ID".txt

python3 build_figures.py -w /tmp/workload.txt -t /tmp/response_time.txt -l "client $NUM_OF_CLIENTS" -n 'Multi-Client' -o "multi_client.pdf"
