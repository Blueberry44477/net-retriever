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

/**
 * Demonstration runner showcasing GoF Behavioral Design Patterns:
 * 1. Chain of Responsibility Pattern (Sequential validation pipeline with pre-flight checks)
 * 2. Command Pattern (Encapsulated operations, multi-level Undo/Redo, macro batch commands, queueing)
 * 3. Iterator Pattern (Specialized sequential, status-filtered, and priority traversals of task collections)
 * 4. Mediator Pattern (Centralized coordination among downloader, storage, bandwidth QoS, and notifications)
 * 5. Memento Pattern (Non-invasive state snapshots, checkpointing, and error rollback)
 * 6. Observer Pattern (One-to-many reactive notifications: console progress, speed telemetry, audit logging, webhooks)
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

        System.out.println("--------------------------------------------------------------------------------");
    }
}
