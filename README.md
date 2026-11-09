# NetRetriever

> **NetRetriever** is a Java-based download manager developed as an architectural sandbox to explore, demonstrate, and test software engineering principles (**SOLID**, **GRASP**) and design patterns (**GoF Creational, Structural & Behavioral Patterns**).

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

### 4. GoF Structural Design Patterns

All structural patterns are implemented under [`io.github.blueberry44477.net_retriever.patterns.structural`](src/main/java/io/github/blueberry44477/net_retriever/patterns/structural/):

| Pattern | Key Classes | Description |
| :--- | :--- | :--- |
| **Adapter (Object Adapter)** | `FileDownloader` *(Target)*<br>`LegacyFtpClient` *(Adaptee)*<br>`LegacyFtpDownloaderAdapter` *(Adapter)* | Implements **Object Adapter** using composition/aggregation (`private final Adaptee adaptee`) rather than class inheritance. Converts incompatible third-party legacy APIs (FTP control/passive streaming) into the standard domain `FileDownloader` contract (`fetchMetadata`, `download`, `supports`). |
| **Bridge** | `DownloadOperation` *(Abstraction)*<br>`SimpleDownloadOperation`<br>`ResumableDownloadOperation`<br>`SplitChunkDownloadOperation` *(RefinedAbstractions)*<br>`DownloadTransport` *(Implementor)*<br>`HttpDownloadTransport`<br>`FtpDownloadTransport`<br>`CloudStorageTransport` *(ConcreteImplementors)* | Decouples download workflow execution logic (sequential streaming, byte-range resuming, parallel multi-chunk partitioning) from transport protocol implementations (HTTP/2, FTP, S3). Avoids $M \times N$ class proliferation and allows abstractions and implementors to vary independently at runtime. |
| **Composite** | `DownloadItem` *(Component)*<br>`FileDownloadItem`<br>`MirroredFileDownloadItem` *(Leaves)*<br>`DownloadGroup`<br>`PriorityDownloadGroup` *(Composites)* | Composes download tasks into hierarchical part-whole tree structures. Allows clients to treat individual file downloads and multi-level composite download batches/folders uniformly (progress aggregation, cascading start/pause/cancel, tree printing). |
| **Flyweight** | `ProtocolPolicyFlyweight` *(Flyweight)*<br>`SharedProtocolPolicy` *(ConcreteFlyweight)*<br>`ProtocolPolicyFactory` *(FlyweightFactory)*<br>`DownloadChunkContext` *(Context)* | Shares immutable intrinsic connection and protocol state (`protocol`, `host`, `port`, `bufferSize`, `authScheme`) across thousands of fine-grained download chunks (`chunkId`, `byteOffset`, `length`, `destination`), drastically reducing memory overhead and eliminating GC thrashing. |
| **Decorator** | `FileDownloader` *(Component)*<br>`BaseFileDownloader` *(ConcreteComponent)*<br>`FileDownloaderDecorator` *(Decorator)*<br>`RateLimitingDownloaderDecorator`<br>`ChecksumVerifyingDownloaderDecorator`<br>`RetryDownloaderDecorator`<br>`LoggingMetricsDownloaderDecorator` *(ConcreteDecorators)* | Dynamically attaches orthogonal cross-cutting responsibilities (bandwidth rate-limiting, checksum integrity validation, automatic retry with backoff, telemetry duration metrics) to `FileDownloader` instances transparently without subclass explosion. Supports arbitrary composition pipelines. |
| **Proxy** | `FileDownloader` *(Subject)*<br>`BaseFileDownloader` *(RealSubject)*<br>`AccessControlDownloaderProxy` *(ProtectionProxy)*<br>`CachingDownloaderProxy` *(CachingProxy)*<br>`LazyInitDownloaderProxy` *(VirtualProxy)* | Provides a surrogate/placeholder to control, guard, or optimize access to `FileDownloader`. Enforces domain security whitelists and HTTPS requirements (Protection Proxy), avoids duplicate remote metadata lookups via thread-safe memoization (Caching Proxy), and defers expensive resource allocation until first invocation (Virtual Proxy). |

---

### 5. GoF Behavioral Design Patterns

All behavioral patterns are implemented under [`io.github.blueberry44477.net_retriever.patterns.behavioral`](src/main/java/io/github/blueberry44477/net_retriever/patterns/behavioral/):

| Pattern | Key Classes | Description |
| :--- | :--- | :--- |
| **Chain of Responsibility** | `DownloadRequestHandler` *(AbstractHandler)*<br>`UriValidationHandler`<br>`AuthenticationHandler`<br>`ContentSecurityHandler`<br>`DiskSpaceValidationHandler`<br>`BandwidthQuotaHandler` *(ConcreteHandlers)*<br>`DownloadRequestPipeline` *(PipelineBuilder)* | Organizes pre-flight download verification into a decoupled, sequential pipeline of specialized handlers. Each handler evaluates one aspect (URI protocol syntax, bearer tokens, blacklist/extension safety, disk capacity, bandwidth quotas) and passes to its successor or short-circuits with a `DownloadValidationException`. |
| **Command** | `DownloadCommand` *(Command)*<br>`DownloadTaskReceiver` *(Receiver)*<br>`StartDownloadCommand`<br>`PauseDownloadCommand`<br>`ResumeDownloadCommand`<br>`CancelDownloadCommand` *(ConcreteCommands)*<br>`BatchDownloadCommand` *(MacroCommand)*<br>`DownloadCommandInvoker` *(Invoker)* | Encapsulates task lifecycle actions as first-class objects, completely decoupling trigger senders from the receiver. Supports multi-level Undo/Redo stacks, transactional all-or-nothing batch/macro rollbacks, asynchronous execution queues, and an audit execution history log. |
| **Iterator** | `TaskIterator` *(Iterator)*<br>`TaskCollection` *(Aggregate)*<br>`DownloadTaskCollection` *(ConcreteAggregate)*<br>`SequentialTaskIterator`<br>`StatusFilteredTaskIterator`<br>`PriorityTaskIterator` *(ConcreteIterators)* | Provides sequential access to aggregated download tasks without exposing the underlying internal collection. Supports concurrent traversal strategies: standard FIFO insertion order, status-filtered subsets (`DOWNLOADING`, `PAUSED`, `ERROR`), and priority sorting (descending file size or custom comparator) without mutating the underlying collection. |
| **Mediator** | `DownloadSessionMediator` *(Mediator)*<br>`DownloadCoordinatorMediator` *(ConcreteMediator)*<br>`DownloadColleague` *(Colleague)*<br>`DownloaderColleague`<br>`StorageColleague`<br>`BandwidthColleague`<br>`NotificationColleague` *(ConcreteColleagues)* | Encapsulates and coordinates complex interactions among decoupled subsystems (network streaming engine, disk storage guardian, bandwidth QoS manager, notification alerts). Eliminates direct $M \times N$ colleague dependencies, orchestrating automated actions like emergency downloader pausing on low disk space and stream throttling during network congestion. |
| **Memento** | `TaskMemento` *(NarrowMemento)*<br>`DownloadTaskMemento` *(WideMemento)*<br>`DownloadTaskOriginator` *(Originator)*<br>`DownloadCheckpointCaretaker` *(Caretaker)* | Captures and externalizes the complete internal execution state of download tasks without violating encapsulation. Provides a narrow interface to Caretakers for checkpoint stack management (Undo/Redo rollbacks and named milestone recovery) while granting wide restoration access only to the Originator. |
| **Observer** | `DownloadObservable` *(Subject)*<br>`DownloadObserver` *(Observer)*<br>`DownloadProgressEvent` *(EventPayload)*<br>`DownloadTaskSubject` *(ConcreteSubject)*<br>`ConsoleProgressObserver`<br>`SpeedTelemetryObserver`<br>`AuditLoggingObserver`<br>`DownloadCompletionWebhookObserver` *(ConcreteObservers)* | Defines a one-to-many subscription dependency where subject state changes automatically notify and update diverse decoupled observers (real-time ASCII progress bar, throughput & ETA calculations, audit compliance logging, and completion/error HTTP webhooks). |
| **State** | `DownloadState` *(State)*<br>`DownloadTaskStateContext` *(Context)*<br>`CreatedDownloadState`<br>`DownloadingDownloadState`<br>`PausedDownloadState`<br>`RetryingDownloadState`<br>`CompletedDownloadState`<br>`ErrorDownloadState`<br>`CanceledDownloadState` *(ConcreteStates)* | Encapsulates lifecycle states as first-class polymorphic objects, eliminating conditional `switch`/`if-else` branching in task entities. Strictly guards against invalid state transitions and manages automatic resilience and fault-tolerant reconnection through the transient `RETRYING` state. |
| **Strategy** | `ChunkPartitioningStrategy` *(Strategy)*<br>`DownloadPartitionContext` *(Context)*<br>`DownloadChunkRange` *(ValueObject)*<br>`FixedSizeChunkingStrategy`<br>`EqualWorkerChunkingStrategy`<br>`AdaptiveDynamicChunkingStrategy`<br>`BandwidthProportionalChunkingStrategy` *(ConcreteStrategies)* | Encapsulates interchangeable payload byte-range chunking algorithms for multi-threaded downloads. Eliminates worker idle time by dynamically adapting partitions based on file size tiers, buffer sizes, and heterogeneous mirror/worker bandwidth capacities while guaranteeing zero-gap, non-overlapping coverage. |
| **Template Method** | `AbstractDownloadWorkflow` *(AbstractClass)*<br>`HttpDownloadWorkflow`<br>`ResumableDownloadWorkflow`<br>`BitTorrentDownloadWorkflow`<br>`CloudStorageDownloadWorkflow` *(ConcreteWorkflows)*<br>`DownloadExecutionResult` *(ResultRecord)* | Defines the invariant sequential execution skeleton (`executeDownload`) for network retrievals, enforcing the Hollywood Principle. Defers primitive steps (`connect`, `fetchContentLength`, `transferPayload`, `disconnect`) to protocol-specific subclasses and provides hooks for partial file resumption, contiguous sparse storage allocation, and checksum validation. |
| **Visitor** | `DownloadElement` *(Element)*<br>`SimpleDownloadElement`<br>`SegmentedDownloadElement`<br>`MirroredDownloadElement`<br>`BatchDownloadElement` *(ConcreteElements)*<br>`DownloadVisitor` *(Visitor)*<br>`DownloadMetricsVisitor`<br>`StorageVerificationVisitor`<br>`JsonExportVisitor`<br>`BandwidthAllocationVisitor` *(ConcreteVisitors)* | Defines new operations over complex composite download task structures without modifying element classes via double dispatch (`element.accept(visitor) -> visitor.visit(this)`). Supports decoupled telemetry calculation, disk space/security audits, formatted JSON serialization, and dynamic QoS bandwidth allocation. |

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
│   │   │   └── patterns/
│   │   │       ├── creational/         # GoF Creational Patterns Sandbox
│   │   │       │   ├── singleton/
│   │   │       │   ├── prototype/
│   │   │       │   ├── factory_method/
│   │   │       │   ├── abstract_factory/
│   │   │       │   ├── builder/
│   │   │       │   ├── object_pool/
│   │   │       │   └── CreationalPatternsDemo.java
│   │   │       ├── structural/         # GoF Structural Patterns Sandbox
│   │   │       │   ├── adapter/        # Object Adapter (LegacyFtpClient & LegacyFtpDownloaderAdapter)
│   │   │       │   ├── bridge/         # Bridge Pattern (Abstractions & Transports)
│   │   │       │   ├── composite/      # Composite Pattern (DownloadItem, Leaves & Groups)
│   │   │       │   ├── flyweight/      # Flyweight Pattern (Policy Flyweight, Factory & ChunkContext)
│   │   │       │   ├── decorator/      # Decorator Pattern (RateLimiting, Checksum, Retry, Metrics)
│   │   │       │   ├── proxy/          # Proxy Pattern (AccessControl, Caching, LazyInit)
│   │   │       │   └── StructuralPatternsDemo.java
│   │   │       └── behavioral/         # GoF Behavioral Patterns Sandbox
│   │   │           ├── chain_of_responsibility/ # Pre-flight validation pipeline handlers
│   │   │           │   └── chain_of_responsibility.md # Architecture documentation
│   │   │           ├── command/        # Task operations, invoker, undo/redo, batch macro
│   │   │           │   └── command.md  # Architecture documentation
│   │   │           ├── iterator/       # Task aggregate collection & specialized iterators
│   │   │           │   └── iterator.md # Architecture documentation
│   │   │           ├── mediator/       # Decoupled component coordination & event dispatch
│   │   │           │   └── mediator.md # Architecture documentation
│   │   │           ├── memento/        # Task checkpointing, undo/redo & state rollback
│   │   │           │   └── memento.md  # Architecture documentation
│   │   │           ├── observer/       # Reactive progress notifications & telemetry observers
│   │   │           │   └── observer.md # Architecture documentation
│   │   │           ├── state/          # Task lifecycle states, transitions & retry resilience
│   │   │           │   └── state.md    # Architecture documentation
│   │   │           ├── strategy/       # Interchangeable chunk partitioning algorithms
│   │   │           │   └── strategy.md # Architecture documentation
│   │   │           ├── template_method/# Invariant download execution skeleton & protocol hooks
│   │   │           │   └── template_method.md # Architecture documentation
│   │   │           ├── visitor/        # Decoupled operations over hierarchical task structures
│   │   │           │   └── visitor.md  # Architecture documentation
│   │   │           └── BehavioralPatternsDemo.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/changelog/           # Liquibase database migrations
│   └── test/
│       └── java/io/github/blueberry44477/net_retriever/patterns/
│           ├── creational/
│           │   └── CreationalPatternsTest.java # JUnit 5 test suite (16 tests)
│           ├── structural/
│           │   └── StructuralPatternsTest.java # JUnit 5 test suite (41 tests)
│           └── behavioral/
│               └── BehavioralPatternsTest.java # JUnit 5 test suite (77 tests)
```

---

## 🚀 Getting Started

### Prerequisites
* **Java:** JDK 21 or higher (OpenJDK 25 compatible)
* **Maven:** Maven Wrapper (`./mvnw`) included

### Running the Creational Patterns Demonstration
```bash
./mvnw clean compile
java -cp target/classes io.github.blueberry44477.net_retriever.patterns.creational.CreationalPatternsDemo
```

### Running the Structural Patterns Demonstration
To run the interactive CLI demonstration showcasing Adapter, Bridge, Composite, Flyweight, Decorator, and Proxy patterns:
```bash
./mvnw clean compile
java -cp target/classes io.github.blueberry44477.net_retriever.patterns.structural.StructuralPatternsDemo
```

### Running the Behavioral Patterns Demonstration
To run the interactive CLI demonstration showcasing Chain of Responsibility, Command, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, and Visitor patterns:
```bash
./mvnw clean compile
java -cp target/classes io.github.blueberry44477.net_retriever.patterns.behavioral.BehavioralPatternsDemo
```

### Running Unit Tests
Dedicated JUnit 5 suites test all creational, structural, and behavioral patterns (134 tests in total):

```bash
# Run creational pattern tests
./mvnw test -Dtest=CreationalPatternsTest

# Run structural pattern tests
./mvnw test -Dtest=StructuralPatternsTest

# Run behavioral pattern tests (77 tests)
./mvnw test -Dtest=BehavioralPatternsTest

# Run all pattern tests (134 tests)
./mvnw test -Dtest=CreationalPatternsTest,StructuralPatternsTest,BehavioralPatternsTest
```

### Generating Javadoc Documentation
All classes, interfaces, methods, and fields include comprehensive Javadoc explaining their architectural role:

```bash
./mvnw javadoc:javadoc
```
The generated documentation is located at `target/reports/apidocs/index.html`.
