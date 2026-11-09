package io.github.blueberry44477.net_retriever.patterns.behavioral;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.AuthenticationHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.BandwidthQuotaHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.ContentSecurityHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.DiskSpaceValidationHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.DownloadRequest;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.DownloadRequestHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.DownloadRequestPipeline;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.DownloadValidationException;
import io.github.blueberry44477.net_retriever.patterns.behavioral.chain_of_responsibility.UriValidationHandler;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.BatchDownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.CancelDownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.DownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.DownloadCommandInvoker;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.DownloadTaskReceiver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.PauseDownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.ResumeDownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.command.StartDownloadCommand;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.DownloadTaskCollection;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.TaskCollection;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.TaskIterator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.BandwidthColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.DownloadCoordinatorMediator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.DownloaderColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.NotificationColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.StorageColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.DownloadCheckpointCaretaker;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.DownloadTaskOriginator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.TaskMemento;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.AuditLoggingObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.ConsoleProgressObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadCompletionWebhookObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadTaskSubject;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.SpeedTelemetryObserver;

import io.github.blueberry44477.net_retriever.patterns.behavioral.state.DownloadTaskStateContext;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.StateTransitionRecord;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.AdaptiveDynamicChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.BandwidthProportionalChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.ChunkPartitionSummary;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.DownloadChunkRange;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.DownloadPartitionContext;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.EqualWorkerChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.FixedSizeChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.AbstractDownloadWorkflow;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.BitTorrentDownloadWorkflow;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.CloudStorageDownloadWorkflow;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.DownloadExecutionResult;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.HttpDownloadWorkflow;
import io.github.blueberry44477.net_retriever.patterns.behavioral.template_method.ResumableDownloadWorkflow;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.BandwidthAllocationVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.BatchDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.DownloadMetricsVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.JsonExportVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.MirroredDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.SegmentedDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.SimpleDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.StorageVerificationVisitor;

/**
 * Demonstration runner showcasing GoF Behavioral Design Patterns:
 * 1. Chain of Responsibility Pattern (Sequential validation pipeline with pre-flight checks)
 * 2. Command Pattern (Encapsulated operations, multi-level Undo/Redo, macro batch commands, queueing)
 * 3. Iterator Pattern (Specialized sequential, status-filtered, and priority traversals of task collections)
 * 4. Mediator Pattern (Centralized coordination among downloader, storage, bandwidth QoS, and notifications)
 * 5. Memento Pattern (Non-invasive state snapshots, checkpointing, and error rollback)
 * 6. Observer Pattern (One-to-many reactive notifications: console progress, speed telemetry, audit logging, webhooks)
 * 7. State Pattern (Encapsulated lifecycle states, transition guards, transient retry resilience)
 * 8. Strategy Pattern (Interchangeable chunk partitioning: fixed-size, equal worker, adaptive dynamic, bandwidth proportional)
 * 9. Template Method Pattern (Invariant download execution skeleton with protocol-specific primitive steps & hooks)
 * 10. Visitor Pattern (Decoupled operations over hierarchical task structures via double dispatch)
 */
public class BehavioralPatternsDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("             NET-RETRIEVER: GOF BEHAVIORAL DESIGN PATTERNS DEMO                 ");
        System.out.println("================================================================================\n");

        runChainOfResponsibilityDemo();
        runCommandDemo();
        runIteratorDemo();
        runMediatorDemo();
        runMementoDemo();
        runObserverDemo();
        runStateDemo();
        runStrategyDemo();
        runTemplateMethodDemo();
        runVisitorDemo();
    }

    /**
     * Demonstrates Chain of Responsibility:
     * - Valid request traversing all handlers: URI -> Auth -> ContentSecurity -> DiskSpace -> Quota.
     * - Fail-fast rejections: invalid scheme, missing auth, malicious URL, insufficient disk, quota exceeded.
     */
    public static void runChainOfResponsibilityDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 1. CHAIN OF RESPONSIBILITY PATTERN DEMO (PRE-FLIGHT VALIDATION CHAIN) <<<");
        System.out.println("================================================================================");

        DownloadRequestHandler pipeline = DownloadRequestPipeline.createDefaultPipeline();

        // 1. Valid request passing all verification stages
        System.out.println("\n[Case 1] Processing fully compliant download request:");
        DownloadRequest validRequest = DownloadRequest.builder()
                .uri(URI.create("https://releases.ubuntu.com/noble/ubuntu-24.04-desktop-amd64.iso"))
                .destinationPath(Path.of("/downloads/ubuntu-24.04.iso"))
                .expectedBytes(5_000_000_000L) // 5 GB
                .availableDiskBytes(50_000_000_000L) // 50 GB
                .authToken("BEARER-VALID-TOKEN-XYZ")
                .userRole("USER")
                .allocatedBandwidthQuotaBytes(20_000_000_000L) // 20 GB
                .build();

        try {
            pipeline.handle(validRequest);
            System.out.printf("[Pipeline Result] Validation SUCCESS! Request validated=%b%n", validRequest.isValidated());
        } catch (DownloadValidationException e) {
            System.err.printf("[Pipeline Result] Unexpected rejection: %s%n", e.getMessage());
        }

        // 2. Request rejected by UriValidationHandler (invalid scheme)
        System.out.println("\n[Case 2] Processing request with unsupported protocol scheme (gopher://):");
        DownloadRequest badSchemeRequest = DownloadRequest.builder()
                .uri(URI.create("gopher://old.archive.org/resource.txt"))
                .destinationPath(Path.of("/downloads/file.txt"))
                .authToken("TOKEN")
                .build();

        try {
            pipeline.handle(badSchemeRequest);
        } catch (DownloadValidationException e) {
            System.out.printf("[Pipeline Result] Expected rejection at [%s]: %s%n", e.getHandlerName(), e.getMessage());
        }

        // 3. Request rejected by AuthenticationHandler (missing token)
        System.out.println("\n[Case 3] Processing request with missing authentication credentials:");
        DownloadRequest unauthenticatedRequest = DownloadRequest.builder()
                .uri(URI.create("https://secure.enterprise.org/payroll.csv"))
                .destinationPath(Path.of("/downloads/payroll.csv"))
                .authToken(null)
                .build();

        try {
            pipeline.handle(unauthenticatedRequest);
        } catch (DownloadValidationException e) {
            System.out.printf("[Pipeline Result] Expected rejection at [%s]: %s%n", e.getHandlerName(), e.getMessage());
        }

        // 4. Request rejected by ContentSecurityHandler (blacklisted malicious domain)
        System.out.println("\n[Case 4] Processing request targeting a blacklisted malware domain:");
        DownloadRequest malwareRequest = DownloadRequest.builder()
                .uri(URI.create("https://malware-dist.evil/payload.bin"))
                .destinationPath(Path.of("/downloads/payload.bin"))
                .authToken("TOKEN")
                .build();

        try {
            pipeline.handle(malwareRequest);
        } catch (DownloadValidationException e) {
            System.out.printf("[Pipeline Result] Expected rejection at [%s]: %s%n", e.getHandlerName(), e.getMessage());
        }

        // 5. Request rejected by DiskSpaceValidationHandler (insufficient storage)
        System.out.println("\n[Case 5] Processing request where file size exceeds free disk capacity:");
        DownloadRequest insufficientDiskRequest = DownloadRequest.builder()
                .uri(URI.create("https://cdn.example.org/dataset-100gb.tar"))
                .destinationPath(Path.of("/downloads/dataset.tar"))
                .expectedBytes(100_000_000_000L) // 100 GB
                .availableDiskBytes(10_000_000_000L) // 10 GB
                .authToken("TOKEN")
                .userRole("USER")
                .build();

        try {
            pipeline.handle(insufficientDiskRequest);
        } catch (DownloadValidationException e) {
            System.out.printf("[Pipeline Result] Expected rejection at [%s]: %s%n", e.getHandlerName(), e.getMessage());
        }

        // 6. Request rejected by BandwidthQuotaHandler (exceeds user quota)
        System.out.println("\n[Case 6] Processing request that exceeds remaining daily bandwidth quota:");
        DownloadRequest quotaExceededRequest = DownloadRequest.builder()
                .uri(URI.create("https://cdn.example.org/backup.iso"))
                .destinationPath(Path.of("/downloads/backup.iso"))
                .expectedBytes(15_000_000_000L) // 15 GB
                .availableDiskBytes(50_000_000_000L)
                .authToken("TOKEN")
                .userRole("USER")
                .allocatedBandwidthQuotaBytes(5_000_000_000L) // only 5 GB quota remaining
                .build();

        try {
            pipeline.handle(quotaExceededRequest);
        } catch (DownloadValidationException e) {
            System.out.printf("[Pipeline Result] Expected rejection at [%s]: %s%n", e.getHandlerName(), e.getMessage());
        }

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Command Pattern:
     * - Start, Pause, Resume commands on DownloadTaskReceiver.
     * - Invoker with multi-level Undo and Redo.
     * - Transactional BatchDownloadCommand (Composite/Macro).
     * - Enqueued execution pipeline.
     */
    public static void runCommandDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 2. COMMAND PATTERN DEMO (TASK OPERATIONS, UNDO/REDO & BATCH MACROS) <<<");
        System.out.println("================================================================================");

        DownloadTask task1 = new DownloadTask("https://releases.ubuntu.com/noble/server.iso", "server.iso", "/downloads");
        DownloadTask task2 = new DownloadTask("https://kernel.org/linux-6.10.tar.xz", "linux-6.10.tar.xz", "/downloads");

        DownloadTaskReceiver receiver1 = new DownloadTaskReceiver(task1);
        DownloadTaskReceiver receiver2 = new DownloadTaskReceiver(task2);

        DownloadCommandInvoker invoker = new DownloadCommandInvoker();

        // 1. Basic command execution: Start
        System.out.println("\n[Sub-demo A] Executing StartDownloadCommand on task1:");
        System.out.printf("Initial status: %s%n", receiver1.getStatus());
        DownloadCommand startCmd1 = new StartDownloadCommand(receiver1, 2_500_000_000L);
        try {
            invoker.executeCommand(startCmd1);
            System.out.printf("Status after execute(): %s (expected DOWNLOADING)%n", receiver1.getStatus());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 2. Undo the Start operation
        System.out.println("\n[Sub-demo B] Undoing the StartDownloadCommand:");
        try {
            invoker.undoLast();
            System.out.printf("Status after undo(): %s (expected CANCELED)%n", receiver1.getStatus());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 3. Pause & Resume cycle with Undo/Redo on task2
        System.out.println("\n[Sub-demo C] Pause / Resume lifecycle with Undo / Redo on task2:");
        DownloadCommand startCmd2 = new StartDownloadCommand(receiver2, 1_000_000_000L);
        DownloadCommand pauseCmd2 = new PauseDownloadCommand(receiver2);
        try {
            invoker.executeCommand(startCmd2);
            System.out.printf("task2 status: %s%n", receiver2.getStatus());

            invoker.executeCommand(pauseCmd2);
            System.out.printf("task2 status after Pause: %s (expected PAUSED)%n", receiver2.getStatus());

            // Undo the pause -> resumes
            invoker.undoLast();
            System.out.printf("task2 status after undo Pause: %s (expected DOWNLOADING)%n", receiver2.getStatus());

            // Redo the pause
            invoker.redoLast();
            System.out.printf("task2 status after redo Pause: %s (expected PAUSED)%n", receiver2.getStatus());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 4. Batch Macro Command (Composite Command)
        System.out.println("\n[Sub-demo D] Executing BatchDownloadCommand (Macro Command):");
        DownloadTask task3 = new DownloadTask("https://example.org/pkg1.deb", "pkg1.deb", "/downloads");
        DownloadTask task4 = new DownloadTask("https://example.org/pkg2.deb", "pkg2.deb", "/downloads");
        DownloadTaskReceiver receiver3 = new DownloadTaskReceiver(task3);
        DownloadTaskReceiver receiver4 = new DownloadTaskReceiver(task4);

        DownloadCommand batchStart = new BatchDownloadCommand("Start All Packages", List.of(
                new StartDownloadCommand(receiver3, 50_000_000L),
                new StartDownloadCommand(receiver4, 75_000_000L)
        ));

        try {
            invoker.executeCommand(batchStart);
            System.out.printf("task3 status: %s, task4 status: %s%n", receiver3.getStatus(), receiver4.getStatus());

            // Undo the batch -> both reverted
            System.out.println("[Client] Reversing entire batch command:");
            invoker.undoLast();
            System.out.printf("task3 status after batch undo: %s, task4: %s%n",
                    receiver3.getStatus(), receiver4.getStatus());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 5. Enqueued FIFO Command Execution
        System.out.println("\n[Sub-demo E] Enqueueing commands and executing via FIFO queue:");
        DownloadTask task5 = new DownloadTask("https://example.org/file.zip", "file.zip", "/downloads");
        DownloadTaskReceiver receiver5 = new DownloadTaskReceiver(task5);

        invoker.queueCommand(new StartDownloadCommand(receiver5, 10_000_000L));
        invoker.queueCommand(new PauseDownloadCommand(receiver5));
        invoker.queueCommand(new ResumeDownloadCommand(receiver5));

        System.out.printf("Queue size before drain: %d%n", invoker.getQueueSize());
        try {
            int executed = invoker.executeQueuedCommands();
            System.out.printf("Executed %d queued commands. Queue size: %d, final task status: %s%n",
                    executed, invoker.getQueueSize(), receiver5.getStatus());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 6. Invoker History Inspection
        System.out.println("\n[Sub-demo F] Invoker Execution History Log:");
        List<DownloadCommand> history = invoker.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, history.get(i).getName());
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Iterator Pattern:
     * - Sequential traversal over task aggregate collection.
     * - Status-filtered traversal (e.g. only DOWNLOADING or ERROR tasks).
     * - Priority-based traversal (descending task size) without modifying underlying collection.
     * - Rewinding and resetting iterator traversal positions.
     */
    public static void runIteratorDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 3. ITERATOR PATTERN DEMO (TASK COLLECTION TRAVERSAL & FILTERING) <<<");
        System.out.println("================================================================================");

        TaskCollection taskCollection = new DownloadTaskCollection();

        DownloadTask t1 = new DownloadTask("https://archive.org/data1.bin", "data1.bin", "/downloads");
        t1.start(150_000_000L); // DOWNLOADING, 150 MB

        DownloadTask t2 = new DownloadTask("https://archive.org/patch.zip", "patch.zip", "/downloads");
        t2.start(25_000_000L);
        t2.pause(); // PAUSED, 25 MB

        DownloadTask t3 = new DownloadTask("https://archive.org/video.mp4", "video.mp4", "/downloads");
        t3.start(1_200_000_000L); // DOWNLOADING, 1.2 GB

        DownloadTask t4 = new DownloadTask("https://archive.org/small.txt", "small.txt", "/downloads");
        // CREATED, 0 B

        DownloadTask t5 = new DownloadTask("https://archive.org/backup.tar", "backup.tar", "/downloads");
        t5.start(800_000_000L);
        t5.fail("Network timeout"); // ERROR, 800 MB

        taskCollection.addTask(t1);
        taskCollection.addTask(t2);
        taskCollection.addTask(t3);
        taskCollection.addTask(t4);
        taskCollection.addTask(t5);

        // Sub-demo A: Standard Sequential Traversal
        System.out.println("\n[Sub-demo A] Sequential traversal (insertion order):");
        TaskIterator seqIterator = taskCollection.createIterator();
        int count = 0;
        while (seqIterator.hasNext()) {
            DownloadTask task = seqIterator.next();
            System.out.printf("  Item #%d: file=\"%s\", status=%s, size=%d bytes%n",
                    ++count, task.getFileName(), task.getStatus(), task.getTotalBytes());
        }

        // Sub-demo B: Status-Filtered Traversal (DOWNLOADING only)
        System.out.println("\n[Sub-demo B] Status-filtered traversal (only DOWNLOADING tasks):");
        TaskIterator activeIterator = taskCollection.createStatusIterator(DownloadStatus.DOWNLOADING);
        while (activeIterator.hasNext()) {
            DownloadTask task = activeIterator.next();
            System.out.printf("  Active stream: file=\"%s\", size=%d bytes%n",
                    task.getFileName(), task.getTotalBytes());
        }

        // Sub-demo C: Priority-Ordered Traversal (Descending file size)
        System.out.println("\n[Sub-demo C] Priority-ordered traversal (largest files first):");
        TaskIterator priorityIterator = taskCollection.createPriorityIterator();
        int rank = 0;
        while (priorityIterator.hasNext()) {
            DownloadTask task = priorityIterator.next();
            System.out.printf("  Priority #%d: file=\"%s\", size=%d bytes%n",
                    ++rank, task.getFileName(), task.getTotalBytes());
        }

        // Sub-demo D: Resetting and re-traversing
        System.out.println("\n[Sub-demo D] Rewinding and resetting iterator:");
        seqIterator.reset();
        System.out.printf("  First item after reset: file=\"%s\"%n", seqIterator.next().getFileName());

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Mediator Pattern:
     * - Coordinated interaction among Downloader, Storage, Bandwidth, and Notification colleagues.
     * - Start and completion workflows with resource allocation and notification dispatch.
     * - Storage low warning coordination resulting in downloader pause.
     * - Bandwidth congestion coordination triggering speed throttling.
     * - Download failure coordination triggering temporary file cleanup and user alerts.
     */
    public static void runMediatorDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 4. MEDIATOR PATTERN DEMO (DECOUPLED COMPONENT COORDINATION) <<<");
        System.out.println("================================================================================");

        DownloaderColleague downloader = new DownloaderColleague();
        StorageColleague storage = new StorageColleague(20_000_000_000L, 2_000_000_000L); // 20 GB free, 2 GB warning
        BandwidthColleague bandwidth = new BandwidthColleague(5_242_880L); // 5 MB/s capacity
        NotificationColleague notifications = new NotificationColleague();

        DownloadCoordinatorMediator mediator = new DownloadCoordinatorMediator(
                downloader, storage, bandwidth, notifications
        );

        DownloadTask taskA = new DownloadTask("https://releases.ubuntu.com/ubuntu.iso", "ubuntu.iso", "/downloads");
        DownloadTask taskB = new DownloadTask("https://archive.org/movie.mkv", "movie.mkv", "/downloads");

        // Sub-demo A: Normal Download Lifecycle (Start -> Progress -> Complete)
        System.out.println("\n[Sub-demo A] Standard lifecycle coordination via Mediator:");
        downloader.startDownload(taskA, 2_500_000_000L);
        downloader.reportProgress(taskA, 1_000_000_000L);
        downloader.completeDownload(taskA);

        // Sub-demo B: Storage Low Space Warning coordination
        System.out.println("\n[Sub-demo B] Storage low capacity warning triggers immediate Downloader pause:");
        System.out.printf("Before warning: Downloader paused = %b%n", downloader.isPaused());
        storage.triggerDiskLow();
        System.out.printf("After warning:  Downloader paused = %b (coordinated by Mediator)%n", downloader.isPaused());

        // Resume downloader
        downloader.resumeActiveDownloads();

        // Sub-demo C: Bandwidth Congestion triggers QoS Throttling
        System.out.println("\n[Sub-demo C] Excessive bandwidth allocation demand triggers streaming throttle:");
        System.out.printf("Before overload: Downloader throttle speed = %d KB/s%n", downloader.getThrottleSpeedKbps());
        bandwidth.allocateBandwidth(10_000_000L); // Exceeds 5 MB/s pool
        System.out.printf("After overload:  Downloader throttle speed = %d KB/s (coordinated by Mediator)%n",
                downloader.getThrottleSpeedKbps());

        // Sub-demo D: Download Failure triggers cleanup and error notification
        System.out.println("\n[Sub-demo D] Download failure triggers temp storage reclaim and alert notification:");
        downloader.startDownload(taskB, 1_000_000_000L);
        downloader.failDownload(taskB, "Connection reset by peer");

        // Sub-demo E: Colleague Notification History Audit
        System.out.println("\n[Sub-demo E] Auditing notification history recorded by NotificationColleague:");
        List<String> history = notifications.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, history.get(i));
        }

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Memento Pattern:
     * - Capturing immutable snapshots (TaskMemento) of complex transfer state in Originator.
     * - Checkpoint stack management in Caretaker (Undo/Redo rollbacks).
     * - Named milestone checkpoints ("golden-state", "network-drop-recovery").
     * - Restoring state without violating encapsulation or exposing private fields.
     */
    public static void runMementoDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 5. MEMENTO PATTERN DEMO (TASK STATE CHECKPOINTING & ROLLBACK) <<<");
        System.out.println("================================================================================");

        DownloadTaskOriginator originator = new DownloadTaskOriginator(
                "https://releases.ubuntu.com/24.04/server.iso", "server.iso", "/downloads/server.iso"
        );
        DownloadCheckpointCaretaker caretaker = new DownloadCheckpointCaretaker();

        // 1. Initial execution and progress
        System.out.println("\n[Sub-demo A] Starting download and capturing Checkpoint 1 (25% progress):");
        originator.start(4_000_000_000L); // 4 GB
        originator.recordChunkProgress(1_000_000_000L, 0); // 1 GB, chunk 0

        TaskMemento checkpoint1 = originator.saveState("checkpoint-25%");
        caretaker.pushCheckpoint(checkpoint1);
        caretaker.saveNamedCheckpoint("milestone-quarter", checkpoint1);

        // 2. Further progress and Checkpoint 2
        System.out.println("\n[Sub-demo B] Downloading further chunks and capturing Checkpoint 2 (50% progress):");
        originator.recordChunkProgress(2_000_000_000L, 1); // 2 GB, chunk 1
        TaskMemento checkpoint2 = originator.saveState("checkpoint-50%");
        caretaker.pushCheckpoint(checkpoint2);

        // 3. Simulating corruption / failure
        System.out.println("\n[Sub-demo C] Simulating unexpected network failure:");
        originator.recordChunkProgress(2_200_000_000L, 2);
        originator.fail("Connection reset by peer at offset 2.2 GB");
        System.out.printf("Current status: %s, downloaded bytes: %d%n",
                originator.getStatus(), originator.getDownloadedBytes());

        // 4. Rollback to Checkpoint 2 (50%)
        System.out.println("\n[Sub-demo D] Rolling back to most recent healthy checkpoint via Caretaker:");
        caretaker.popCheckpoint().ifPresent(originator::restoreState);
        System.out.printf("Restored status: %s, restored bytes: %d, chunks completed: %s%n",
                originator.getStatus(), originator.getDownloadedBytes(), originator.getCompletedChunkIndices());

        // 5. Rollback further to Checkpoint 1 (25%)
        System.out.println("\n[Sub-demo E] Rolling back further to Checkpoint 1 via Caretaker:");
        caretaker.popCheckpoint().ifPresent(originator::restoreState);
        System.out.printf("Restored status: %s, restored bytes: %d, chunks completed: %s%n",
                originator.getStatus(), originator.getDownloadedBytes(), originator.getCompletedChunkIndices());

        // 6. Named checkpoint restoration
        System.out.println("\n[Sub-demo F] Restoring named checkpoint [milestone-quarter]:");
        caretaker.getNamedCheckpoint("milestone-quarter").ifPresent(originator::restoreState);
        System.out.printf("Restored named milestone: label=\"%s\", bytes=%d%n",
                originator.saveState("test").getLabel(), originator.getDownloadedBytes());

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Observer Pattern:
     * - Subject broadcasting events to multiple decoupled observers.
     * - Real-time terminal progress rendering (ConsoleProgressObserver).
     * - Dynamic throughput calculation and ETA estimation (SpeedTelemetryObserver).
     * - Audit trail recording of all lifecycle events (AuditLoggingObserver).
     * - External webhook notification on completion (DownloadCompletionWebhookObserver).
     */
    public static void runObserverDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 6. OBSERVER PATTERN DEMO (ONE-TO-MANY PROGRESS & TELEMETRY UPDATES) <<<");
        System.out.println("================================================================================");

        DownloadTaskSubject subject = new DownloadTaskSubject(
                "https://releases.ubuntu.com/noble/ubuntu.iso", "ubuntu.iso", "/downloads/ubuntu.iso"
        );

        ConsoleProgressObserver consoleObserver = new ConsoleProgressObserver();
        SpeedTelemetryObserver speedObserver = new SpeedTelemetryObserver();
        AuditLoggingObserver auditObserver = new AuditLoggingObserver();
        DownloadCompletionWebhookObserver webhookObserver = new DownloadCompletionWebhookObserver(
                URI.create("https://webhook.internal/downloads/callback")
        );

        // Sub-demo A: Registering observers
        System.out.println("\n[Sub-demo A] Registering 4 diverse observers with DownloadTaskSubject:");
        subject.addObserver(consoleObserver);
        subject.addObserver(speedObserver);
        subject.addObserver(auditObserver);
        subject.addObserver(webhookObserver);
        System.out.printf("Active observer count: %d%n", subject.getObserverCount());

        // Sub-demo B: Starting download
        System.out.println("\n[Sub-demo B] Starting download session (triggers status update to all observers):");
        subject.start(1_000_000_000L); // 1 GB

        // Sub-demo C: Streaming chunks
        System.out.println("\n[Sub-demo C] Simulating byte streaming increments with speed telemetry:");
        subject.updateProgress(250_000_000L, 25_000_000L); // 250 MB @ 25 MB/s
        subject.updateProgress(500_000_000L, 50_000_000L); // 500 MB @ 50 MB/s
        subject.updateProgress(750_000_000L, 75_000_000L); // 750 MB @ 75 MB/s
        subject.updateProgress(1_000_000_000L, 100_000_000L); // 1 GB @ 100 MB/s

        // Sub-demo D: Completing download
        System.out.println("\n[Sub-demo D] Completing download session (triggers webhook callback and status update):");
        subject.complete();
        System.out.printf("Webhook callback fired %d time(s). Peak speed: %.2f MB/s%n",
                webhookObserver.getCallbackCount(),
                speedObserver.getPeakSpeedBytesPerSec() / (1024.0 * 1024.0));

        // Sub-demo E: Audit log summary
        System.out.println("\n[Sub-demo E] Reviewing audit events recorded by AuditLoggingObserver:");
        System.out.printf("Total audit records captured: %d%n", auditObserver.getLogCount());
        for (int i = 0; i < auditObserver.getLogCount(); i++) {
            System.out.printf("  Record #%d: %s%n", i + 1, auditObserver.getAuditLog().get(i));
        }

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates State Pattern:
     * - Encapsulation of download lifecycle states (CREATED -> DOWNLOADING -> PAUSED -> RETRYING -> COMPLETED).
     * - Guarding against invalid state transitions with descriptive exceptions.
     * - Automatic resilience and recovery through the transient RETRYING state.
     * - State transition history tracking for complete lifecycle auditability.
     */
    public static void runStateDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 7. STATE PATTERN DEMO (DOWNLOAD LIFECYCLE & TRANSIENT RETRY RESILIENCE) <<<");
        System.out.println("================================================================================");

        // Sub-demo A: Normal Lifecycle (Created -> Downloading -> Paused -> Resumed -> Completed)
        System.out.println("\n[Sub-demo A] Standard lifecycle execution (Start -> Pause -> Resume -> Complete):");
        DownloadTaskStateContext context = new DownloadTaskStateContext(
                "https://releases.ubuntu.com/noble/ubuntu-24.04.iso",
                "ubuntu-24.04.iso",
                "/downloads/ubuntu-24.04.iso",
                2 // max 2 retries
        );
        System.out.printf("Initial state: %s (isTerminal=%b, canResume=%b)%n",
                context.getStateName(), context.isTerminal(), context.canResume());

        context.start(2_000_000_000L); // 2 GB
        System.out.printf("Active state: %s (Progress: %.1f%%)%n",
                context.getStateName(), context.getProgressPercentage());

        context.updateProgress(500_000_000L); // 500 MB
        System.out.printf("Progress updated: %d / %d bytes (%.1f%%)%n",
                context.getDownloadedBytes(), context.getTotalBytes(), context.getProgressPercentage());

        context.pause();
        System.out.printf("State after pause(): %s (canResume=%b)%n",
                context.getStateName(), context.canResume());

        context.resume();
        System.out.printf("State after resume(): %s%n", context.getStateName());

        context.updateProgress(2_000_000_000L);
        context.complete();
        System.out.printf("Final state: %s (isTerminal=%b, Progress: %.1f%%)%n",
                context.getStateName(), context.isTerminal(), context.getProgressPercentage());

        // Sub-demo B: Transient Failure with Automatic Retry Recovery
        System.out.println("\n[Sub-demo B] Fault tolerance: Network interruption with transient RETRYING state:");
        DownloadTaskStateContext resilientContext = new DownloadTaskStateContext(
                "https://dl.cdn.net/archive.tar.gz",
                "archive.tar.gz",
                "/tmp/archive.tar.gz",
                3
        );
        resilientContext.start(100_000_000L); // 100 MB
        resilientContext.updateProgress(30_000_000L); // 30 MB

        // Simulate network drop
        resilientContext.fail("Connection reset by peer");
        System.out.printf("State after transient failure: %s (attempts: %d/%d)%n",
                resilientContext.getStateName(),
                resilientContext.getCurrentRetryAttempt(),
                resilientContext.getMaxRetries());

        // Reconnect and finish
        resilientContext.retry();
        System.out.printf("State after retry(): %s (resumed from byte %d)%n",
                resilientContext.getStateName(), resilientContext.getDownloadedBytes());
        resilientContext.updateProgress(100_000_000L);
        resilientContext.complete();
        System.out.printf("Resilient download completed successfully! Status=%s%n", resilientContext.getStatus());

        // Sub-demo C: Exhausting Retry Budget (RETRYING -> Permanent ERROR)
        System.out.println("\n[Sub-demo C] Exhausting retry budget transitioning to permanent ERROR:");
        DownloadTaskStateContext failingContext = new DownloadTaskStateContext(
                "https://broken.host/file.bin",
                "file.bin",
                "/tmp/file.bin",
                2
        );
        failingContext.start(50_000_000L);
        failingContext.fail("HTTP 503 Service Unavailable");
        System.out.printf("After fail #1: State=%s (attempt %d)%n",
                failingContext.getStateName(), failingContext.getCurrentRetryAttempt());
        failingContext.fail("HTTP 503 Service Unavailable");
        System.out.printf("After fail #2: State=%s (attempt %d)%n",
                failingContext.getStateName(), failingContext.getCurrentRetryAttempt());
        failingContext.fail("HTTP 503 Service Unavailable");
        System.out.printf("After fail #3 (budget exhausted): State=%s, Status=%s%n",
                failingContext.getStateName(), failingContext.getStatus());

        // Sub-demo D: Invalid Transition Guards
        System.out.println("\n[Sub-demo D] Verifying state encapsulation guards against invalid operations:");
        try {
            context.pause(); // context is already COMPLETED
        } catch (IllegalStateException e) {
            System.out.printf("[Guard Triggered] Correctly rejected pause() on COMPLETED state: %s%n", e.getMessage());
        }

        // Sub-demo E: State Transition Audit History
        System.out.println("\n[Sub-demo E] Reviewing complete state transition audit history:");
        List<StateTransitionRecord> history = resilientContext.getTransitionHistory();
        System.out.printf("Total transitions recorded: %d%n", history.size());
        for (int i = 0; i < history.size(); i++) {
            StateTransitionRecord r = history.get(i);
            System.out.printf("  Transition #%d: %s -> %s | Reason: \"%s\"%n",
                    i + 1, r.fromStatus(), r.toStatus(), r.reason());
        }

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Strategy Pattern:
     * - Decoupled payload chunking algorithms (Fixed-size, Equal worker, Adaptive dynamic, Bandwidth proportional).
     * - Runtime strategy swapping without modifying client code or partitioning invariants.
     * - Solving the heterogeneous network worker bottleneck (proportional speed distribution).
     * - Analytical metrics generation (load imbalance, min/max chunk size, worker allocations).
     */
    public static void runStrategyDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 8. STRATEGY PATTERN DEMO (INTERCHANGEABLE CHUNK PARTITIONING ALGORITHMS) <<<");
        System.out.println("================================================================================");

        long payloadSize = 100_000_000L; // 100 MB
        int workerCount = 4;

        // Sub-demo A: FixedSizeChunkingStrategy (20 MB chunks)
        System.out.println("\n[Sub-demo A] Fixed-Size Chunking Strategy (20 MB fixed blocks across 4 workers):");
        DownloadPartitionContext context = new DownloadPartitionContext(new FixedSizeChunkingStrategy(20_000_000L));
        List<DownloadChunkRange> fixedChunks = context.executePartition(payloadSize, workerCount);
        for (DownloadChunkRange chunk : fixedChunks) {
            System.out.printf("  Chunk #%d: %-25s len=%10d bytes (Worker #%d)%n",
                    chunk.chunkIndex(), chunk.toHttpRangeHeader(), chunk.length(), chunk.assignedWorkerId());
        }
        ChunkPartitionSummary fixedSummary = context.getPartitionSummary(fixedChunks);
        System.out.printf("  Summary: %d chunks, Imbalance ratio: %.2f%n",
                fixedSummary.totalChunks(), fixedSummary.loadImbalanceRatio());

        // Sub-demo B: EqualWorkerChunkingStrategy (Perfect 1-byte balanced distribution)
        System.out.println("\n[Sub-demo B] Switching to Equal-Worker Chunking Strategy (100 MB / 4 workers):");
        context.setStrategy(new EqualWorkerChunkingStrategy());
        List<DownloadChunkRange> equalChunks = context.executePartition(payloadSize, workerCount);
        for (DownloadChunkRange chunk : equalChunks) {
            System.out.printf("  Chunk #%d: %-25s len=%10d bytes (Worker #%d)%n",
                    chunk.chunkIndex(), chunk.toHttpRangeHeader(), chunk.length(), chunk.assignedWorkerId());
        }
        ChunkPartitionSummary equalSummary = context.getPartitionSummary(equalChunks);
        System.out.printf("  Summary: %d chunks, Imbalance ratio: %.2f (Perfect parity)%n",
                equalSummary.totalChunks(), equalSummary.loadImbalanceRatio());

        // Sub-demo C: AdaptiveDynamicChunkingStrategy (Size-tiered adaptation)
        System.out.println("\n[Sub-demo C] Switching to Adaptive Dynamic Strategy across different payload sizes:");
        context.setStrategy(new AdaptiveDynamicChunkingStrategy());

        // Small file: 2 MB -> 1 chunk (no socket overhead)
        List<DownloadChunkRange> smallChunks = context.executePartition(2_000_000L, 8);
        System.out.printf("  Small file (2 MB):  %d chunk(s) generated -> %s%n",
                smallChunks.size(), smallChunks.getFirst().toHttpRangeHeader());

        // Medium file: 25 MB -> 4 chunks
        List<DownloadChunkRange> medChunks = context.executePartition(25_000_000L, 8);
        System.out.printf("  Medium file (25 MB): %d chunks generated across workers%n", medChunks.size());

        // Large file: 800 MB -> 16 chunks
        List<DownloadChunkRange> hugeChunks = context.executePartition(800_000_000L, 16);
        System.out.printf("  Huge file (800 MB):  %d chunks generated across 16 workers%n", hugeChunks.size());

        // Sub-demo D: BandwidthProportionalChunkingStrategy (Heterogeneous worker speeds)
        System.out.println("\n[Sub-demo D] Bandwidth Proportional Strategy (Heterogeneous mirror speeds):");
        // Worker 0 = 100 MB/s (Fiber), Worker 1 = 50 MB/s (Cable), Worker 2 = 25 MB/s (DSL), Worker 3 = 25 MB/s (DSL)
        List<Double> mirrorSpeeds = List.of(100.0, 50.0, 25.0, 25.0); // Ratio: 50%, 25%, 12.5%, 12.5%
        context.setStrategy(new BandwidthProportionalChunkingStrategy(mirrorSpeeds));
        List<DownloadChunkRange> propChunks = context.executePartition(payloadSize, 4);

        for (DownloadChunkRange chunk : propChunks) {
            double pct = ((double) chunk.length() / payloadSize) * 100.0;
            System.out.printf("  Worker #%d (Speed: %5.1f MB/s): %-25s len=%9d B (%4.1f%%)%n",
                    chunk.assignedWorkerId(),
                    mirrorSpeeds.get(chunk.assignedWorkerId()),
                    chunk.toHttpRangeHeader(),
                    chunk.length(),
                    pct);
        }

        ChunkPartitionSummary propSummary = context.getPartitionSummary(propChunks);
        System.out.printf("  Total covered: %d bytes (100%% contiguous, 0 lost bytes)%n", propSummary.totalBytes());

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Template Method Pattern:
     * - Fixed algorithm skeleton enforced by AbstractDownloadWorkflow.executeDownload() (final).
     * - Protocol-specific primitive steps (HTTP single-stream, HTTP resumable Range, BitTorrent swarm, Cloud S3).
     * - Hook methods: optional pre-download resume checks, sparse storage pre-allocation, integrity hashing, post-processing.
     * - Guaranteed resource cleanup in finally block.
     */
    public static void runTemplateMethodDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 9. TEMPLATE METHOD PATTERN DEMO (INVARIANT WORKFLOW SKELETON & HOOKS) <<<");
        System.out.println("================================================================================");

        // Sub-demo A: Standard HTTP Workflow
        System.out.println("\n[Sub-demo A] Standard single-stream HTTP download workflow:");
        AbstractDownloadWorkflow httpWorkflow = new HttpDownloadWorkflow();
        DownloadExecutionResult httpResult = httpWorkflow.executeDownload(
                "https://releases.ubuntu.com/noble/SHA256SUMS",
                "/downloads/SHA256SUMS"
        );
        System.out.printf("Result: success=%b, transferred=%d bytes, checksum=%s%n",
                httpResult.success(), httpResult.bytesTransferred(), httpResult.checksum());
        System.out.println("Step trace:");
        for (String step : httpResult.executionSteps()) {
            System.out.println("  -> " + step);
        }

        // Sub-demo B: Resumable HTTP Range Workflow
        System.out.println("\n[Sub-demo B] Resumable HTTP Range download workflow (40 MB partial file already present):");
        AbstractDownloadWorkflow resumableWorkflow = new ResumableDownloadWorkflow(40_000_000L); // 40 MB already downloaded
        DownloadExecutionResult resumeResult = resumableWorkflow.executeDownload(
                "https://cdn.example.org/large-dataset.tar.gz",
                "/downloads/large-dataset.tar.gz"
        );
        System.out.printf("Result: success=%b, total=%d bytes, transferred=%d bytes (only missing delta!)%n",
                resumeResult.success(), resumeResult.totalBytes(), resumeResult.bytesTransferred());

        // Sub-demo C: BitTorrent Workflow (Sparse File Pre-allocation Hook)
        System.out.println("\n[Sub-demo C] BitTorrent swarm workflow (Sparse storage pre-allocation & info-hash check):");
        AbstractDownloadWorkflow torrentWorkflow = new BitTorrentDownloadWorkflow(128);
        DownloadExecutionResult torrentResult = torrentWorkflow.executeDownload(
                "magnet:?xt=urn:btih:da39a3ee5e6b4b0d3255bfef95601890afd80709&dn=ubuntu-24.04.iso",
                "/downloads/ubuntu-24.04.iso"
        );
        System.out.printf("Result: success=%b, transferred=%d bytes, infoHash=%s%n",
                torrentResult.success(), torrentResult.bytesTransferred(), torrentResult.checksum());

        // Sub-demo D: Cloud S3 Multipart Workflow
        System.out.println("\n[Sub-demo D] Cloud Object Storage workflow (IAM SigV4 auth & multipart ETag check):");
        AbstractDownloadWorkflow cloudWorkflow = new CloudStorageDownloadWorkflow("production-assets-bucket");
        DownloadExecutionResult cloudResult = cloudWorkflow.executeDownload(
                "s3://production-assets-bucket/backups/db-backup-2026.sql.zst",
                "/downloads/db-backup.sql.zst"
        );
        System.out.printf("Result: success=%b, transferred=%d bytes, ETag=%s%n",
                cloudResult.success(), cloudResult.bytesTransferred(), cloudResult.checksum());

        // Sub-demo E: Error Handling & Guaranteed Cleanup on Invalid URL
        System.out.println("\n[Sub-demo E] Fault handling: Invalid scheme triggers safe cleanup in template method:");
        DownloadExecutionResult failResult = httpWorkflow.executeDownload("invalid-uri-no-scheme", "/tmp/err");
        System.out.printf("Result: success=%b, error=\"%s\"%n", failResult.success(), failResult.errorMessage());

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Demonstrates Visitor Pattern:
     * - Double dispatch mechanism over heterogeneous task structures.
     * - DownloadMetricsVisitor: aggregated bytes, leaf counts, chunks, mirrors, and percentage.
     * - StorageVerificationVisitor: disk space projection, dangerous extension detection, duplicate file check.
     * - JsonExportVisitor: pretty-printed JSON serialization of the task hierarchy.
     * - BandwidthAllocationVisitor: QoS proportional bandwidth partitioning based on active connections.
     */
    public static void runVisitorDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 10. VISITOR PATTERN DEMO (DECOUPLED OPERATIONS OVER TASK HIERARCHIES) <<<");
        System.out.println("================================================================================");

        // Construct composite download hierarchy
        BatchDownloadElement rootBatch = new BatchDownloadElement("Release-Bundle-v2.0");

        SimpleDownloadElement simpleFile = new SimpleDownloadElement(
                "release-notes.txt", "https://cdn.example.org/release-notes.txt",
                20_000L, 20_000L, "text/plain", "sha256:abc123notes"
        );

        SegmentedDownloadElement segmentedFile = new SegmentedDownloadElement(
                "application-server.tar.gz", "https://cdn.example.org/application-server.tar.gz",
                500_000_000L, 250_000_000L, 8, 4 // 8 chunks, 4 connections
        );

        MirroredDownloadElement mirroredFile = new MirroredDownloadElement(
                "database-dump.sql.zst", "https://us-east.mirror.org/db.sql.zst",
                List.of("https://eu-west.mirror.org/db.sql.zst", "https://ap-south.mirror.org/db.sql.zst"),
                1_000_000_000L, 400_000_000L
        );

        BatchDownloadElement subBatch = new BatchDownloadElement("Tools-And-Utilities");
        subBatch.addElement(new SimpleDownloadElement(
                "updater-patch.bin", "https://cdn.example.org/updater-patch.bin",
                50_000_000L, 10_000_000L, "application/octet-stream", "sha256:patch789"
        ));
        subBatch.addElement(new SimpleDownloadElement(
                "install-script.sh", "http://insecure.internal/install-script.sh", // Insecure HTTP & .sh extension
                15_000L, 0L, "text/x-shellscript", "sha256:script456"
        ));

        rootBatch.addElement(simpleFile);
        rootBatch.addElement(segmentedFile);
        rootBatch.addElement(mirroredFile);
        rootBatch.addElement(subBatch);

        // Sub-demo A: DownloadMetricsVisitor
        System.out.println("\n[Sub-demo A] DownloadMetricsVisitor: Calculating aggregated telemetry across hierarchy:");
        DownloadMetricsVisitor metricsVisitor = new DownloadMetricsVisitor();
        rootBatch.accept(metricsVisitor);
        System.out.printf("  Total Files:       %d%n", metricsVisitor.getTotalFilesCount());
        System.out.printf("  Total Batches:     %d%n", metricsVisitor.getTotalBatchesCount());
        System.out.printf("  Total Segments:    %d%n", metricsVisitor.getTotalSegmentsCount());
        System.out.printf("  Total Mirrors:     %d%n", metricsVisitor.getTotalMirrorsCount());
        System.out.printf("  Total Bytes:       %d bytes (%.2f MB)%n",
                metricsVisitor.getAggregatedTotalBytes(),
                metricsVisitor.getAggregatedTotalBytes() / (1024.0 * 1024.0));
        System.out.printf("  Downloaded Bytes:  %d bytes (%.2f MB)%n",
                metricsVisitor.getAggregatedDownloadedBytes(),
                metricsVisitor.getAggregatedDownloadedBytes() / (1024.0 * 1024.0));
        System.out.printf("  Overall Progress:  %.2f%%%n", metricsVisitor.getOverallProgressPercentage());

        // Sub-demo B: StorageVerificationVisitor
        System.out.println("\n[Sub-demo B] StorageVerificationVisitor: Auditing remaining disk space & security policies:");
        StorageVerificationVisitor storageVisitor = new StorageVerificationVisitor();
        rootBatch.accept(storageVisitor);
        System.out.printf("  Remaining disk space required: %d bytes (%.2f MB)%n",
                storageVisitor.getRequiredRemainingDiskBytes(),
                storageVisitor.getRequiredRemainingDiskBytes() / (1024.0 * 1024.0));
        System.out.printf("  Security Policy Passed:        %b%n", storageVisitor.isApproved());
        System.out.printf("  Warnings recorded:             %d%n", storageVisitor.getWarnings().size());
        for (String warning : storageVisitor.getWarnings()) {
            System.out.println("    [WARNING] " + warning);
        }

        // Sub-demo C: JsonExportVisitor
        System.out.println("\n[Sub-demo C] JsonExportVisitor: Serializing entire download tree into JSON:");
        JsonExportVisitor jsonVisitor = new JsonExportVisitor();
        rootBatch.accept(jsonVisitor);
        System.out.println(jsonVisitor.getJsonOutput());

        // Sub-demo D: BandwidthAllocationVisitor
        System.out.println("\n[Sub-demo D] BandwidthAllocationVisitor: Allocating 100 MB/s QoS pool across elements:");
        BandwidthAllocationVisitor bandwidthVisitor = new BandwidthAllocationVisitor(100_000_000L); // 100 MB/s
        rootBatch.accept(bandwidthVisitor);
        var allocations = bandwidthVisitor.computeAllocations();
        System.out.printf("  Total bandwidth pool: %d B/s (%.1f MB/s)%n",
                bandwidthVisitor.getTotalBandwidthPoolBytesPerSec(),
                bandwidthVisitor.getTotalBandwidthPoolBytesPerSec() / (1024.0 * 1024.0));
        for (var entry : allocations.entrySet()) {
            System.out.printf("  Element ID [%-36s]: %10d B/s (%5.2f MB/s)%n",
                    entry.getKey(), entry.getValue(), entry.getValue() / (1024.0 * 1024.0));
        }

        System.out.println("--------------------------------------------------------------------------------");
    }
}
