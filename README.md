# Distributed Peer-to-Peer System

## University of Houston-Clear Lake  
**College of Science and Engineering**  
**Advanced Operating Systems**  
**Author: Umar Abdul Aziz (2310655)**  

---

## Table of Contents
- [Introduction](#introduction)
- [Data Structures and Algorithms](#data-structures-and-algorithms)
- [Problem Solving - Index Server](#problem-solving---index-server)
- [Problem Solving - Peer](#problem-solving---peer)
- [System Architecture](#system-architecture)
- [Screenshots](#screenshots)
- [Conclusion](#conclusion)
- [How to Run](#how-to-run)
- [Future Enhancements](#future-enhancements)
- [License](#license)
- [Author](#author)

---

## Introduction
Peer-to-peer (P2P) networks enable decentralized resource sharing without relying on a single central authority. This project implements a **distributed peer-to-peer content-sharing system** using **Java** and **socket programming**, designed for scalable and efficient file sharing. The system consists of an **Index Server** that maintains peer registrations and file metadata, while **individual peers** can search for, upload, and download files.

### Key Features:
- **Master Index Server:**
  - Manages user registration, authentication, and content indexing.
  - Acts as a centralized metadata store for available files.
- **Peer-to-Peer Clients:**
  - Can connect to the Index Server to register and share files.
  - Retrieve file locations and directly request downloads from other peers.
- **Multi-threaded Design:**
  - Ensures simultaneous file transfers and server requests.
- **Efficient Lookup Using HashMaps:**
  - Stores user credentials and file information for quick retrieval.
- **Scalable Architecture:**
  - Supports multiple peers and concurrent connections for efficient load distribution.

---

## Data Structures and Algorithms
### 1. HashMap for User Credentials
Used for efficiently storing and retrieving user authentication details.
```java
HashMap<String, User> users = new HashMap<>();
```
- **Key:** Username
- **Value:** User object containing email and password

### 2. HashMap for File Information
Stores metadata about shared files to facilitate fast lookups.
```java
HashMap<String, FileInfo> files = new HashMap<>();
```
- **Key:** Filename
- **Value:** FileInfo object containing the peer’s IP address and port

### Benefits of Using HashMaps
- **O(1) Lookup Time** for fast authentication and file searches.
- **Dynamic Updates** enable real-time peer activity tracking.
- **Scalability** for managing large numbers of users and files efficiently.

---

## Problem Solving - Index Server
### Overview
The **IndexServer** acts as a central directory for managing user registrations, authentication, and file lookup requests. It operates as a multi-threaded server, efficiently handling multiple simultaneous client connections.

### Key Functionalities
- **User Registration & Authentication:**
  - Ensures only authorized users can access the system.
- **File Indexing & Lookup:**
  - Maintains metadata about available files and their host peers.
- **Handling Client Requests Concurrently:**
  - Uses multi-threading to allow multiple clients to interact with the server simultaneously.

### Commands Handled by the Server
| Command | Description |
|---------|-------------|
| REGISTER | Registers a new user |
| LOGIN | Authenticates an existing user |
| SEARCH | Searches for a file among registered peers |
| LOADFILE | Adds new content to the server’s file index |

### Multi-threading Approach
Each client request is handled in a **separate thread**, preventing bottlenecks and ensuring smooth server operations.

---

## Problem Solving - Peer
### Overview
Each **Peer Client** registers with the Index Server and can **search for and download** files from other peers. Peers act as both content providers and consumers.

### Key Functionalities
- **Connect to Index Server:**
  - Registers as an active node and shares available file metadata.
- **Host a Local Server:**
  - Enables file sharing by responding to direct download requests.
- **Search and Retrieve Content:**
  - Queries the Index Server for file locations and initiates downloads from the respective peers.

### Example Workflow
1. A peer **registers/logs in** to the Index Server.
2. The peer **uploads file details** to the Index Server.
3. Another peer **searches for a file**.
4. If found, a **direct TCP connection** is established between peers for file transfer.

### File Handling
- Shared files are stored in the `ContentFile/` directory.
- Received files are saved in the `receivefiles/` directory.

---

## System Architecture
[Insert System Architecture Diagram Here]

---

## Screenshots
- **User Login & File Download**  
- **File Search & Peer Connection**  
- **Registration & File Upload**  
[Insert Screenshots Here]

---

## Conclusion
This project successfully implements a **scalable distributed peer-to-peer file-sharing system** using Java and socket programming. Key takeaways include:
- **Multi-threading** for efficient request handling.
- **Decentralized File Sharing** that minimizes reliance on central authorities.
- **Efficient User & File Management** using HashMaps.
- **Concurrent User Authentication** and file retrieval processes.

The system effectively demonstrates **the advantages of peer-to-peer networking** and provides hands-on experience in distributed computing.

---

## How to Run
### Prerequisites
- **Java 8+** installed on all participating nodes.
- A **networked environment** for peer-to-peer communication.

### Steps
1. **Start the Index Server:**
```sh
java IndexServer
```
2. **Start a Peer Client:**
```sh
java PeerClient
```
3. **Register/Login** and begin file sharing.

---

## Future Enhancements
- **Security Improvements:** Implement **encryption** for secure data transfers.
- **Dynamic Peer Discovery:** Enable automatic peer detection for improved scalability.
- **Optimized Load Balancing:** Introduce mechanisms to distribute file-sharing load more efficiently.
- **User-Friendly Interface:** Develop a GUI for easier interaction with the system.
---

## Author
**Umar Abdul Aziz**

