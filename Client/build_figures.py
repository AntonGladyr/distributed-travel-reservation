import matplotlib
matplotlib.use('Agg')

from matplotlib import pyplot as plt
from ast import literal_eval

with open('response_time.txt') as f:
	response_time = [literal_eval(line) for line in f]

with open('workload.txt') as f:
	workload = [literal_eval(line) for line in f]

plt.plot(workload, response_time)
plt.ylabel('response time (ms)')
plt.xlabel('transactions per second')

plt.savefig('test.pdf')
