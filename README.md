# Distributed Travel Reservation

_This project is an adaption of the project of CSE 593 of the University of Washington._

The goal this project is to develop a component-based distributed information system using some of the fundamental components and algorithms for distribution, coordination, scalability, fault-tolerance, etc. The ultimate goal is a cohesive multi-client, multi-server implementation of a Travel Reservation system.

The system allows customers to reserve flights, cars and rooms for their vacation. You can find more information about the client interface and explore the functionality in the _UserGuide.pdf_ and _GettingStarted.pdf_.

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
