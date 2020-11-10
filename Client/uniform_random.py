import sys
import numpy as np

def main():
	s = np.random.uniform(sys.argv[1], sys.argv[2], sys.argv[3])
	print(list(s))

if __name__ == "__main__":
	main()
