# comp512-project

Distributed Systems project for COMP 512, McGill, Fall 2020.

## Project Group #3

* `Stacey Beard`:
* `Melissa Dunn`:
* `Anton Gladyr`: 260892882

# Usage

To run the RMI resource manager:

```
cd Server/
./run_server.sh [<rmi_name>] # starts a single ResourceManager
./run_servers.sh # convenience script for starting multiple resource managers
```

To run the RMI client:

```
cd Client
./run_client.sh [<server_hostname> [<server_rmi_name>]]
```
