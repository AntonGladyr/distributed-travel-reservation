#!/bin/bash

ID=1

rm -f /tmp/response_time.txt
rm -f /tmp/workload.txt

./performance_analysis.sh $ID 10 false transaction_single_rm.txt
tr '\n' ' ' < /tmp/"response_time_$ID".txt | cat > /tmp/response_time.txt
tr '\n' ' ' < /tmp/"workload_$ID".txt | cat > /tmp/workload.txt
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt

./performance_analysis.sh $ID 10 false transaction_multiple_rm.txt
echo "" >> /tmp/response_time.txt
echo "" >> /tmp/workload.txt
tr '\n' ' ' < /tmp/"response_time_$ID".txt | cat >> /tmp/response_time.txt
tr '\n' ' ' < /tmp/"workload_$ID".txt | cat >> /tmp/workload.txt
rm -f /tmp/"response_time_$ID".txt
rm -f /tmp/"workload_$ID".txt



#python3 build_figures.py

#rm -f /tmp/response_time.txt
#rm -f /tmp/workload.txt

#TODO: multi-client
