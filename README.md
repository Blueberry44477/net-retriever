# NetRetriever

> **NetRetriever** is a Java-based download manager developed as an architectural sandbox to explore, demonstrate, and test software engineering principles (**SOLID**, **GRASP**) and design patterns (**GoF Creational Patterns**).

---

## 📌 Project Overview

In real-world systems, network download managers face challenges such as protocol diversity (HTTP, FTP, BitTorrent), varied storage targets (local files, server clusters), heavy I/O throughput, concurrency control, and dynamic task configurations.

This repository uses the **Download Manager** domain to illustrate clean architecture, object-oriented design principles, and creational design patterns in a realistic, modular codebase.

---

## 🏛 Software Architecture Principles

### 1. SOLID Principles

* **S — Single Responsibility Principle (SRP)**
  * Each class has a single, well-defined responsibility:
    * `DownloadTask`: Encapsulates entity state and progress lifecycle.
    * `HttpFileDownloader`: Handles low-level network streaming via Spring `RestClient`.
    * `StoragePathResolver`: Handles target filesystem path resolution and collisions.
    * `DownloadTaskService`: Orchestrates task creation, metadata retrieval, and persistence.

* **O — Open/Closed Principle (OCP)**
  * The system is open for extension but closed for modification.
  * Adding support for new network protocols (e.g., FTP, BitTorrent) is accomplished by implementing `FileDownloader` or `ProtocolDownloader` without changing existing download logic.
  * Storage strategies can be extended by implementing `StoragePathResolver` without modifying the core service.

* **L — Liskov Substitution Principle (LSP)**
  * Concrete implementations can be substituted wherever their base abstraction is expected:
    * `LocalStoragePathResolver` and `ServerStoragePathResolver` are interchangeable through `StoragePathResolver`.
    * Any `ProtocolDownloader` subtype conforms strictly to `verifySource()` and `downloadFile()` contracts.

* **I — Interface Segregation Principle (ISP)**
  * Interfaces are focused and minimal rather than monolithic:
    * `ProgressListener`: Focused solely on progress callback events.
    * `StoragePathResolver`: Focused purely on filesystem path resolution.
    * `ProtocolEngine`, `IntegrityValidator`, and `TransferMonitor`: Separated into distinct roles within protocol suites.

* **D — Dependency Inversion Principle (DIP)**
  * High-level business logic depends on abstractions rather than low-level concrete details:
    * `DownloadTaskService` depends on `StoragePathResolver` and `FileDownloader` abstractions, injected via Spring dependency injection.

---

### 2. GRASP Principles (General Responsibility Assignment Software Patterns)

* **Information Expert**
  * Responsibilities are placed in the class that holds the necessary data:
    * `DownloadTask` computes its own percentage completion (`getProgressPercentage()`) and validates state transitions (`start()`, `pause()`, `complete()`, `fail()`).
* **Creator**
  * `DownloadTaskService` creates instances of `DownloadTask`.
  * `DownloaderProvider` creates instances of `ProtocolDownloader`.
* **Controller**
  * `DownloadTaskController` serves as the first entrypoint for handling HTTP REST requests, delegating workflow execution to application services.
* **Low Coupling & High Cohesion**
  * Classes are grouped into feature-oriented packages (`downloader`, `storage`, `downloadtask`, `patterns.creational`).
  * High cohesion inside each module; low coupling between domain layers through interfaces and DTOs.
* **Polymorphism**
  * Protocol differences and storage schemes are handled polymorphically instead of relying on conditional `if/else` or `switch` blocks.
* **Pure Fabrication**
  * Classes invented to maintain high cohesion and low coupling:
    * `StoragePathResolver`: Pure fabrication for filesystem abstractions.
    * `TaskPrototypeRegistry`: Pure fabrication for template management.
    * `DownloadBufferPool`: Pure fabrication for memory buffer lifecycle management.
* **Indirection**
  * DTO mappings (`DownloadTaskResponse`, `CreateDownloadTaskRequest`) via MapStruct decouple persistence entities from client API representations.
* **Protected Variations**
  * Abstract interfaces protect the core download manager from unstable or changing external dependencies (such as underlying transport libraries or storage APIs).

---

### 3. GoF Creational Design Patterns

All creational patterns are implemented under [`io.github.blueberry44477.net_retriever.patterns.creational`](src/main/java/io/github/blueberry44477/net_retriever/patterns/creational/):

| Pattern | Key Classes | Description |
| :--- | :--- | :--- |
| **Singleton** | `DownloadManagerConfig` | Centralized, thread-safe global configuration using double-checked locking with `volatile`. Features protection against Reflection API instantiation and Java serialization attacks (`readResolve`). |
| **Prototype** | `DownloadTaskPrototype`<br>`StandardDownloadTaskPrototype`<br>`PriorityDownloadTaskPrototype`<br>`BackgroundDownloadTaskPrototype`<br>`TaskPrototypeRegistry` | Enables cloning pre-configured task templates. Employs deep copying (`customHeaders`) to prevent state leakage between clones and provides a registry for template retrieval. |
| **Factory Method** | `DownloaderProvider`<br>`HttpDownloaderProvider`<br>`FtpDownloaderProvider`<br>`TorrentDownloaderProvider`<br>`ProtocolDownloader` | Defines an abstract creator hook (`createDownloader()`) while delegating concrete downloader instantiation (`HttpProtocolDownloader`, `FtpProtocolDownloader`, `TorrentProtocolDownloader`) to specialized provider subclasses. Includes template method `processDownload()`. |
| **Abstract Factory** | `ProtocolComponentFactory`<br>`HttpComponentFactory`<br>`FtpComponentFactory`<br>`TorrentComponentFactory` | Produces families of compatible, protocol-specific components: `ProtocolEngine` (network transfer), `IntegrityValidator` (checksums), and `TransferMonitor` (speed/peer telemetry). Guarantees components from different protocols are never mixed. |
| **Builder** | `DownloadJob`<br>`DownloadJobBuilder`<br>`DownloadJobDirector` | Fluent step-by-step assembly of complex, immutable `DownloadJob` instances with validation on `build()`. `DownloadJobDirector` provides standardized recipes (e.g., `constructFastMultiThreadJob`, `constructBackgroundJob`, `constructSecureVerifiedJob`). |
| **Object Pool** | `DownloadBuffer`<br>`DownloadBufferPool` | Thread-safe object pool managing reusable byte buffers (`DownloadBuffer`). Eliminates Garbage Collection (GC) thrashing caused by frequent memory allocations during multi-threaded chunk downloads. Features `acquireBuffer()` and `releaseBuffer()` with state reset. |

---

## 📁 Project Structure

```
net-retriever/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/io/github/blueberry44477/net_retriever/
│   │   │   ├── NetRetrieverApplication.java
│   │   │   ├── configuration/          # Spring Boot configurations
│   │   │   ├── downloader/             # Network downloader abstractions & HTTP implementation
│   │   │   ├── downloadtask/           # Core DownloadTask entity, service, controller, repository
│   │   │   ├── storage/                # Storage path resolvers (local and server modes)
│   │   │   └── patterns/creational/    # GoF Creational Patterns Sandbox
│   │   │       ├── singleton/          # Singleton pattern
│   │   │       ├── prototype/          # Prototype pattern & registry
│   │   │       ├── factory_method/     # Factory Method pattern
│   │   │       ├── abstract_factory/   # Abstract Factory pattern & product families
│   │   │       ├── builder/            # Builder pattern & Director
│   │   │       ├── object_pool/        # Object Pool pattern
│   │   │       └── CreationalPatternsDemo.java # CLI demonstration runner
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/changelog/           # Liquibase database migrations
│   └── test/
│       └── java/io/github/blueberry44477/net_retriever/patterns/creational/
│           └── CreationalPatternsTest.java # JUnit 5 test suite (16 tests)
```

---

## 🚀 Getting Started

### Prerequisites
* **Java:** JDK 21 or higher
* **Maven:** Maven Wrapper (`./mvnw`) included

### Running the Creational Patterns Demonstration
To run the interactive CLI demonstration showcasing all 6 creational patterns in action:

```bash
# Compile the project
./mvnw clean compile

# Execute the demo runner
java -cp target/classes io.github.blueberry44477.net_retriever.patterns.creational.CreationalPatternsDemo
```

### Running Unit Tests
A dedicated JUnit 5 suite tests identity preservation, cloning isolation, factory instantiation, builder validation, and pool exhaustion:

```bash
./mvnw test -Dtest=CreationalPatternsTest
```

### Generating Javadoc Documentation
All classes and interfaces include Javadoc explaining their architectural role:

```bash
./mvnw javadoc:javadoc
```
The generated documentation is located at `target/reports/apidocs/index.html`.
