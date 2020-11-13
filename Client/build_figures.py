import matplotlib
matplotlib.use('Agg')
import argparse

from matplotlib import pyplot as plt
from ast import literal_eval


def main():
	# Parse arguments
	argparse_root = argparse.ArgumentParser(description = "Performance Analysis")
	argparse_root.add_argument(
		"-l",
		"--labels",
		help="List of label names, separated by comma",
		required=True
	)
	
	argparse_root.add_argument(
		"-w",
		"--workload",
		help="Path to the workload file",
		required=True
	)

	argparse_root.add_argument(
		"-t",
		"--time",
		help="Path to the response_time file",
		required=True
	)
	
	argparse_root.add_argument(
		"-n",
		"--name",
		help="Plot title",
		required=True
	)
	
	argparse_root.add_argument(
		"-o",
		"--output",
		help="Output file name",
		required=True
	)
	
	args = argparse_root.parse_args()

	with open(args.time) as f:
		response_time = [literal_eval(line) for line in f]

	with open(args.workload) as f:
		workload = [literal_eval(line) for line in f]

	labels = args.labels.split(",")

	for tr_load, time, label in zip(workload, response_time, labels):
		plt.plot(list(tr_load), list(time), label=label, marker='.')
		#print(list(tr_load), list(time))

	plt.ylabel('response time (ms)')
	plt.xlabel('workload')
	
	# Set a title
	plt.title(args.name)
	# show a legend on the plot
	plt.legend()

	plt.savefig(args.output)

if __name__ == "__main__":
	main()
