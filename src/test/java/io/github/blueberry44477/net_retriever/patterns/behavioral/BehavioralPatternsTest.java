package io.github.blueberry44477.net_retriever.patterns.behavioral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.PriorityTaskIterator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.SequentialTaskIterator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.StatusFilteredTaskIterator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.TaskCollection;
import io.github.blueberry44477.net_retriever.patterns.behavioral.iterator.TaskIterator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.BandwidthColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.DownloadCoordinatorMediator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.DownloadEvent;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.DownloaderColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.NotificationColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.mediator.StorageColleague;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.DownloadCheckpointCaretaker;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.DownloadTaskMemento;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.DownloadTaskOriginator;
import io.github.blueberry44477.net_retriever.patterns.behavioral.memento.TaskMemento;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.AuditLoggingObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.ConsoleProgressObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadCompletionWebhookObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadObserver;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadProgressEvent;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.DownloadTaskSubject;
import io.github.blueberry44477.net_retriever.patterns.behavioral.observer.SpeedTelemetryObserver;
import java.util.Optional;
import java.util.Set;

class BehavioralPatternsTest {

    @Nested
    @DisplayName("Chain of Responsibility Pattern Tests")
    class ChainOfResponsibilityPatternTests {

        @Test
        @DisplayName("Valid download request passes through all default pipeline handlers")
        void testValidRequestPassesAllPipelineHandlers() throws DownloadValidationException {
            DownloadRequestHandler pipeline = DownloadRequestPipeline.createDefaultPipeline();

            DownloadRequest request = DownloadRequest.builder()
                    .uri(URI.create("https://cdn.example.org/packages/release-2.0.tar.gz"))
                    .destinationPath(Path.of("/downloads/release-2.0.tar.gz"))
                    .expectedBytes(100_000_000L)
                    .availableDiskBytes(500_000_000L)
                    .authToken("BEARER-VALID-TOKEN")
                    .userRole("USER")
                    .allocatedBandwidthQuotaBytes(200_000_000L)
                    .build();

            pipeline.handle(request);
            assertTrue(request.isValidated());
        }

        @Test
        @DisplayName("UriValidationHandler rejects null, invalid scheme, or empty host")
        void testUriValidationRejections() {
            UriValidationHandler handler = new UriValidationHandler();

            // Null URI
            DownloadRequest nullUriRequest = DownloadRequest.builder().uri(null).build();
            DownloadValidationException ex1 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(nullUriRequest));
            assertEquals("ERR_NULL_URI", ex1.getErrorCode());

            // Unsupported scheme
            DownloadRequest unsupportedSchemeRequest = DownloadRequest.builder()
                    .uri(URI.create("ftp-custom://mirror.org/file.bin"))
                    .build();
            DownloadValidationException ex2 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(unsupportedSchemeRequest));
            assertEquals("ERR_UNSUPPORTED_SCHEME", ex2.getErrorCode());

            // Empty host
            DownloadRequest emptyHostRequest = DownloadRequest.builder()
                    .uri(URI.create("http:///path/file.bin"))
                    .build();
            DownloadValidationException ex3 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(emptyHostRequest));
            assertEquals("ERR_EMPTY_HOST", ex3.getErrorCode());
        }

        @Test
        @DisplayName("AuthenticationHandler rejects missing, blank, or expired tokens")
        void testAuthenticationHandlerRejections() {
            AuthenticationHandler handler = new AuthenticationHandler();

            // Missing token
            DownloadRequest missingToken = DownloadRequest.builder().authToken(null).build();
            DownloadValidationException ex1 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(missingToken));
            assertEquals("ERR_MISSING_TOKEN", ex1.getErrorCode());

            // Blank token
            DownloadRequest blankToken = DownloadRequest.builder().authToken("   ").build();
            DownloadValidationException ex2 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(blankToken));
            assertEquals("ERR_MISSING_TOKEN", ex2.getErrorCode());

            // Expired token
            DownloadRequest expiredToken = DownloadRequest.builder().authToken("EXPIRED").build();
            DownloadValidationException ex3 = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(expiredToken));
            assertEquals("ERR_INVALID_TOKEN", ex3.getErrorCode());
        }

        @Test
        @DisplayName("ContentSecurityHandler blocks blacklisted domain")
        void testContentSecurityHandlerBlocksBlacklistedDomain() {
            ContentSecurityHandler handler = new ContentSecurityHandler();

            DownloadRequest malwareRequest = DownloadRequest.builder()
                    .uri(URI.create("https://malware-dist.evil/archive.zip"))
                    .destinationPath(Path.of("/downloads/archive.zip"))
                    .build();

            DownloadValidationException ex = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(malwareRequest));
            assertEquals("ERR_BLOCKED_DOMAIN", ex.getErrorCode());
        }

        @Test
        @DisplayName("ContentSecurityHandler blocks dangerous extensions for non-admin but allows for admin")
        void testContentSecurityHandlerExtensionEnforcement() throws DownloadValidationException {
            ContentSecurityHandler handler = new ContentSecurityHandler();

            DownloadRequest userExeRequest = DownloadRequest.builder()
                    .uri(URI.create("https://trusted.org/installer.exe"))
                    .destinationPath(Path.of("/downloads/installer.exe"))
                    .userRole("USER")
                    .build();

            DownloadValidationException ex = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(userExeRequest));
            assertEquals("ERR_BLOCKED_EXTENSION", ex.getErrorCode());

            // Admin role bypasses extension restriction
            DownloadRequest adminExeRequest = DownloadRequest.builder()
                    .uri(URI.create("https://trusted.org/installer.exe"))
                    .destinationPath(Path.of("/downloads/installer.exe"))
                    .userRole("ADMIN")
                    .build();

            handler.handle(adminExeRequest);
            assertTrue(adminExeRequest.isValidated());
        }

        @Test
        @DisplayName("DiskSpaceValidationHandler rejects when available disk space is insufficient")
        void testDiskSpaceValidationRejection() {
            DiskSpaceValidationHandler handler = new DiskSpaceValidationHandler();

            DownloadRequest insufficientDisk = DownloadRequest.builder()
                    .expectedBytes(20_000_000L)
                    .availableDiskBytes(5_000_000L)
                    .build();

            DownloadValidationException ex = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(insufficientDisk));
            assertEquals("ERR_INSUFFICIENT_DISK", ex.getErrorCode());
        }

        @Test
        @DisplayName("BandwidthQuotaHandler rejects when requested size exceeds remaining quota")
        void testBandwidthQuotaRejection() {
            BandwidthQuotaHandler handler = new BandwidthQuotaHandler();

            DownloadRequest overQuota = DownloadRequest.builder()
                    .expectedBytes(50_000_000L)
                    .allocatedBandwidthQuotaBytes(10_000_000L)
                    .build();

            DownloadValidationException ex = assertThrows(DownloadValidationException.class,
                    () -> handler.handle(overQuota));
            assertEquals("ERR_QUOTA_EXCEEDED", ex.getErrorCode());
        }

        @Test
        @DisplayName("Lightweight pipeline processes only URI and Disk validation")
        void testLightweightPipeline() throws DownloadValidationException {
            DownloadRequestHandler lightweightPipeline = DownloadRequestPipeline.createLightweightPipeline();

            // Omitting auth token and quota - still succeeds in lightweight pipeline
            DownloadRequest request = DownloadRequest.builder()
                    .uri(URI.create("http://localhost:8080/data.bin"))
                    .destinationPath(Path.of("/tmp/data.bin"))
                    .expectedBytes(1000L)
                    .availableDiskBytes(10_000L)
                    .authToken(null) // No auth token required
                    .build();

            lightweightPipeline.handle(request);
            assertTrue(request.isValidated());
        }
    }

    @Nested
    @DisplayName("Command Pattern Tests")
    class CommandPatternTests {

        @Test
        @DisplayName("StartDownloadCommand executes and undo reverts status")
        void testStartDownloadCommandExecuteAndUndo() throws Exception {
            DownloadTask task = new DownloadTask("https://example.org/file.bin", "file.bin", "/tmp");
            DownloadTaskReceiver receiver = new DownloadTaskReceiver(task);
            StartDownloadCommand command = new StartDownloadCommand(receiver, 1024L);

            assertEquals(DownloadStatus.CREATED, receiver.getStatus());
            assertFalse(command.isExecuted());
            assertTrue(command.canUndo());

            command.execute();
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());
            assertTrue(command.isExecuted());

            command.undo();
            assertEquals(DownloadStatus.CANCELED, receiver.getStatus());
            assertFalse(command.isExecuted());
        }

        @Test
        @DisplayName("Pause and Resume download commands with Undo")
        void testPauseAndResumeDownloadCommands() throws Exception {
            DownloadTask task = new DownloadTask("https://example.org/test.iso", "test.iso", "/tmp");
            DownloadTaskReceiver receiver = new DownloadTaskReceiver(task);
            receiver.actionStart(5000L);
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());

            // Pause
            PauseDownloadCommand pauseCmd = new PauseDownloadCommand(receiver);
            pauseCmd.execute();
            assertEquals(DownloadStatus.PAUSED, receiver.getStatus());
            assertTrue(pauseCmd.isExecuted());

            // Undo Pause -> Resumes
            pauseCmd.undo();
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());

            // Resume Command
            pauseCmd.execute(); // pause again
            ResumeDownloadCommand resumeCmd = new ResumeDownloadCommand(receiver);
            resumeCmd.execute();
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());

            // Undo Resume -> Pauses
            resumeCmd.undo();
            assertEquals(DownloadStatus.PAUSED, receiver.getStatus());
        }

        @Test
        @DisplayName("CancelDownloadCommand marks task canceled and is irreversible")
        void testCancelDownloadCommand() {
            DownloadTask task = new DownloadTask("https://example.org/doc.pdf", "doc.pdf", "/tmp");
            DownloadTaskReceiver receiver = new DownloadTaskReceiver(task);
            receiver.actionStart(2048L);

            CancelDownloadCommand cancelCmd = new CancelDownloadCommand(receiver);
            assertFalse(cancelCmd.canUndo());

            cancelCmd.execute();
            assertEquals(DownloadStatus.CANCELED, receiver.getStatus());
            assertTrue(cancelCmd.isExecuted());

            assertThrows(UnsupportedOperationException.class, cancelCmd::undo);
        }

        @Test
        @DisplayName("DownloadCommandInvoker supports multi-level Undo and Redo")
        void testInvokerMultiLevelUndoRedo() throws Exception {
            DownloadTask task = new DownloadTask("https://example.org/video.mp4", "video.mp4", "/tmp");
            DownloadTaskReceiver receiver = new DownloadTaskReceiver(task);
            DownloadCommandInvoker invoker = new DownloadCommandInvoker();

            StartDownloadCommand startCmd = new StartDownloadCommand(receiver, 10_000L);
            PauseDownloadCommand pauseCmd = new PauseDownloadCommand(receiver);

            invoker.executeCommand(startCmd);
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());
            assertEquals(1, invoker.getUndoStackSize());

            invoker.executeCommand(pauseCmd);
            assertEquals(DownloadStatus.PAUSED, receiver.getStatus());
            assertEquals(2, invoker.getUndoStackSize());

            // Undo pause -> DOWNLOADING
            assertTrue(invoker.undoLast());
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());
            assertEquals(1, invoker.getUndoStackSize());
            assertEquals(1, invoker.getRedoStackSize());

            // Redo pause -> PAUSED
            assertTrue(invoker.redoLast());
            assertEquals(DownloadStatus.PAUSED, receiver.getStatus());
            assertEquals(2, invoker.getUndoStackSize());
            assertEquals(0, invoker.getRedoStackSize());

            // History inspection
            assertEquals(2, invoker.getHistory().size());
        }

        @Test
        @DisplayName("BatchDownloadCommand executes atomically and rolls back on failure")
        void testBatchDownloadCommandAtomicRollbackOnFailure() {
            DownloadTask task1 = new DownloadTask("https://example.org/1.bin", "1.bin", "/tmp");
            DownloadTask task2 = new DownloadTask("https://example.org/2.bin", "2.bin", "/tmp");

            DownloadTaskReceiver receiver1 = new DownloadTaskReceiver(task1);
            DownloadTaskReceiver receiver2 = new DownloadTaskReceiver(task2);

            DownloadCommand cmd1 = new StartDownloadCommand(receiver1, 1000L);
            DownloadCommand cmd2 = new StartDownloadCommand(receiver2, 2000L);
            DownloadCommand failingCmd = new DownloadCommand() {
                @Override
                public void execute() throws Exception {
                    throw new RuntimeException("Simulated network outage");
                }

                @Override
                public void undo() {
                }

                @Override
                public String getName() {
                    return "FailingCommand";
                }

                @Override
                public boolean isExecuted() {
                    return false;
                }
            };

            BatchDownloadCommand batch = new BatchDownloadCommand("Atomic Batch", List.of(cmd1, cmd2, failingCmd));

            assertThrows(RuntimeException.class, batch::execute);
            assertFalse(batch.isExecuted());

            // Verify rollback: task1 and task2 should have had their start reverted (status CANCELED)
            assertEquals(DownloadStatus.CANCELED, receiver1.getStatus());
            assertEquals(DownloadStatus.CANCELED, receiver2.getStatus());
        }

        @Test
        @DisplayName("BatchDownloadCommand undo reverses all executed child commands")
        void testBatchDownloadCommandUndo() throws Exception {
            DownloadTask task1 = new DownloadTask("https://example.org/1.bin", "1.bin", "/tmp");
            DownloadTask task2 = new DownloadTask("https://example.org/2.bin", "2.bin", "/tmp");

            DownloadTaskReceiver receiver1 = new DownloadTaskReceiver(task1);
            DownloadTaskReceiver receiver2 = new DownloadTaskReceiver(task2);

            DownloadCommand cmd1 = new StartDownloadCommand(receiver1, 1000L);
            DownloadCommand cmd2 = new StartDownloadCommand(receiver2, 2000L);

            BatchDownloadCommand batch = new BatchDownloadCommand("Batch Start", List.of(cmd1, cmd2));
            batch.execute();

            assertEquals(DownloadStatus.DOWNLOADING, receiver1.getStatus());
            assertEquals(DownloadStatus.DOWNLOADING, receiver2.getStatus());
            assertTrue(batch.isExecuted());

            batch.undo();
            assertEquals(DownloadStatus.CANCELED, receiver1.getStatus());
            assertEquals(DownloadStatus.CANCELED, receiver2.getStatus());
            assertFalse(batch.isExecuted());
        }

        @Test
        @DisplayName("DownloadCommandInvoker queue execution processes in FIFO order")
        void testInvokerQueueAndDrain() throws Exception {
            DownloadTask task = new DownloadTask("https://example.org/file.bin", "file.bin", "/tmp");
            DownloadTaskReceiver receiver = new DownloadTaskReceiver(task);
            DownloadCommandInvoker invoker = new DownloadCommandInvoker();

            invoker.queueCommand(new StartDownloadCommand(receiver, 1000L));
            invoker.queueCommand(new PauseDownloadCommand(receiver));
            invoker.queueCommand(new ResumeDownloadCommand(receiver));

            assertEquals(3, invoker.getQueueSize());
            int executed = invoker.executeQueuedCommands();

            assertEquals(3, executed);
            assertEquals(0, invoker.getQueueSize());
            assertEquals(DownloadStatus.DOWNLOADING, receiver.getStatus());

            invoker.clearHistory();
            assertEquals(0, invoker.getHistory().size());
            assertEquals(0, invoker.getUndoStackSize());
            assertEquals(0, invoker.getRedoStackSize());
        }
    }

    @Nested
    @DisplayName("Iterator Pattern Tests")
    class IteratorPatternTests {

        @Test
        @DisplayName("SequentialTaskIterator traverses tasks in insertion order and throws on exhaustion")
        void testSequentialTaskIteratorTraversalOrder() {
            DownloadTaskCollection collection = new DownloadTaskCollection();
            DownloadTask t1 = new DownloadTask("https://example.org/1.bin", "1.bin", "/tmp");
            DownloadTask t2 = new DownloadTask("https://example.org/2.bin", "2.bin", "/tmp");
            DownloadTask t3 = new DownloadTask("https://example.org/3.bin", "3.bin", "/tmp");

            collection.addTask(t1);
            collection.addTask(t2);
            collection.addTask(t3);

            assertEquals(3, collection.size());
            assertFalse(collection.isEmpty());

            TaskIterator iterator = collection.createIterator();
            assertTrue(iterator.hasNext());
            assertEquals("1.bin", iterator.next().getFileName());
            assertTrue(iterator.hasNext());
            assertEquals("2.bin", iterator.next().getFileName());
            assertTrue(iterator.hasNext());
            assertEquals("3.bin", iterator.next().getFileName());
            assertFalse(iterator.hasNext());

            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @DisplayName("SequentialTaskIterator reset rewinds traversal position to the beginning")
        void testSequentialTaskIteratorReset() {
            DownloadTaskCollection collection = new DownloadTaskCollection();
            collection.addTask(new DownloadTask("https://example.org/a.zip", "a.zip", "/tmp"));
            collection.addTask(new DownloadTask("https://example.org/b.zip", "b.zip", "/tmp"));

            TaskIterator iterator = collection.createIterator();
            assertEquals("a.zip", iterator.next().getFileName());
            assertEquals("b.zip", iterator.next().getFileName());
            assertFalse(iterator.hasNext());

            iterator.reset();
            assertTrue(iterator.hasNext());
            assertEquals("a.zip", iterator.next().getFileName());
        }

        @Test
        @DisplayName("StatusFilteredTaskIterator filters tasks by exact DownloadStatus")
        void testStatusFilteredTaskIterator() {
            DownloadTaskCollection collection = new DownloadTaskCollection();

            DownloadTask t1 = new DownloadTask("https://example.org/f1", "f1.iso", "/tmp");
            t1.start(100L); // DOWNLOADING

            DownloadTask t2 = new DownloadTask("https://example.org/f2", "f2.iso", "/tmp");
            t2.start(100L);
            t2.pause(); // PAUSED

            DownloadTask t3 = new DownloadTask("https://example.org/f3", "f3.iso", "/tmp");
            t3.start(200L); // DOWNLOADING

            DownloadTask t4 = new DownloadTask("https://example.org/f4", "f4.iso", "/tmp");
            t4.start(100L);
            t4.fail("Network error"); // ERROR

            collection.addTask(t1);
            collection.addTask(t2);
            collection.addTask(t3);
            collection.addTask(t4);

            StatusFilteredTaskIterator activeIterator =
                    (StatusFilteredTaskIterator) collection.createStatusIterator(DownloadStatus.DOWNLOADING);

            assertEquals(DownloadStatus.DOWNLOADING, activeIterator.getTargetStatus());
            assertEquals(2, activeIterator.getMatchCount());

            assertTrue(activeIterator.hasNext());
            assertEquals("f1.iso", activeIterator.next().getFileName());
            assertTrue(activeIterator.hasNext());
            assertEquals("f3.iso", activeIterator.next().getFileName());
            assertFalse(activeIterator.hasNext());
            assertThrows(NoSuchElementException.class, activeIterator::next);

            // Test reset
            activeIterator.reset();
            assertTrue(activeIterator.hasNext());
            assertEquals("f1.iso", activeIterator.next().getFileName());
        }

        @Test
        @DisplayName("Custom predicate-filtered iterator filters by filename pattern")
        void testFilteredIteratorWithCustomPredicate() {
            DownloadTaskCollection collection = new DownloadTaskCollection();
            collection.addTask(new DownloadTask("https://example.org/doc.pdf", "doc.pdf", "/tmp"));
            collection.addTask(new DownloadTask("https://example.org/archive.tar.gz", "archive.tar.gz", "/tmp"));
            collection.addTask(new DownloadTask("https://example.org/sheet.xlsx", "sheet.xlsx", "/tmp"));

            TaskIterator tarIterator = collection.createFilteredIterator(t -> t.getFileName().endsWith(".tar.gz"));
            assertTrue(tarIterator.hasNext());
            assertEquals("archive.tar.gz", tarIterator.next().getFileName());
            assertFalse(tarIterator.hasNext());
        }

        @Test
        @DisplayName("PriorityTaskIterator traverses tasks sorted by descending file size")
        void testPriorityTaskIteratorOrdering() {
            DownloadTaskCollection collection = new DownloadTaskCollection();

            DownloadTask small = new DownloadTask("https://example.org/small", "small.txt", "/tmp");
            small.start(10_000L); // 10 KB

            DownloadTask huge = new DownloadTask("https://example.org/huge", "huge.iso", "/tmp");
            huge.start(2_000_000_000L); // 2 GB

            DownloadTask medium = new DownloadTask("https://example.org/medium", "medium.zip", "/tmp");
            medium.start(50_000_000L); // 50 MB

            DownloadTask large = new DownloadTask("https://example.org/large", "large.tar", "/tmp");
            large.start(500_000_000L); // 500 MB

            // Added out of size order
            collection.addTask(small);
            collection.addTask(huge);
            collection.addTask(medium);
            collection.addTask(large);

            PriorityTaskIterator priorityIterator =
                    (PriorityTaskIterator) collection.createPriorityIterator();

            assertEquals(4, priorityIterator.getTaskCount());

            // Expected order: huge (2GB) -> large (500MB) -> medium (50MB) -> small (10KB)
            assertTrue(priorityIterator.hasNext());
            assertEquals("huge.iso", priorityIterator.next().getFileName());
            assertTrue(priorityIterator.hasNext());
            assertEquals("large.tar", priorityIterator.next().getFileName());
            assertTrue(priorityIterator.hasNext());
            assertEquals("medium.zip", priorityIterator.next().getFileName());
            assertTrue(priorityIterator.hasNext());
            assertEquals("small.txt", priorityIterator.next().getFileName());
            assertFalse(priorityIterator.hasNext());
            assertThrows(NoSuchElementException.class, priorityIterator::next);

            // Verify original collection order has not been mutated
            TaskIterator seqIterator = collection.createIterator();
            assertEquals("small.txt", seqIterator.next().getFileName());
            assertEquals("huge.iso", seqIterator.next().getFileName());
        }

        @Test
        @DisplayName("Custom sorted iterator traverses by alphabetical filename order")
        void testPriorityTaskIteratorCustomComparator() {
            DownloadTaskCollection collection = new DownloadTaskCollection();
            collection.addTask(new DownloadTask("https://example.org/z", "zebra.bin", "/tmp"));
            collection.addTask(new DownloadTask("https://example.org/a", "apple.bin", "/tmp"));
            collection.addTask(new DownloadTask("https://example.org/m", "mango.bin", "/tmp"));

            TaskIterator alphaIterator = collection.createSortedIterator(
                    Comparator.comparing(DownloadTask::getFileName)
            );

            assertEquals("apple.bin", alphaIterator.next().getFileName());
            assertEquals("mango.bin", alphaIterator.next().getFileName());
            assertEquals("zebra.bin", alphaIterator.next().getFileName());
            assertFalse(alphaIterator.hasNext());
        }

        @Test
        @DisplayName("Empty collection iterator handles hasNext and next gracefully")
        void testEmptyCollectionIteratorBehavior() {
            DownloadTaskCollection empty = new DownloadTaskCollection();
            assertTrue(empty.isEmpty());
            assertEquals(0, empty.size());

            TaskIterator it = empty.createIterator();
            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);

            TaskIterator statusIt = empty.createStatusIterator(DownloadStatus.DOWNLOADING);
            assertFalse(statusIt.hasNext());
            assertThrows(NoSuchElementException.class, statusIt::next);
        }

        @Test
        @DisplayName("DownloadTaskCollection add, remove, clear, and snapshot operations")
        void testCollectionAddRemoveAndSnapshot() {
            DownloadTaskCollection collection = new DownloadTaskCollection();
            DownloadTask t1 = new DownloadTask("https://example.org/1", "1.bin", "/tmp");
            DownloadTask t2 = new DownloadTask("https://example.org/2", "2.bin", "/tmp");

            collection.addTask(t1);
            collection.addTask(t2);
            assertEquals(2, collection.size());

            List<DownloadTask> snapshot = collection.getTasksSnapshot();
            assertEquals(2, snapshot.size());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new DownloadTask()));

            assertTrue(collection.removeTask(t1));
            assertEquals(1, collection.size());
            assertFalse(collection.removeTask(t1));

            collection.clear();
            assertTrue(collection.isEmpty());
        }
    }

    @Nested
    @DisplayName("Mediator Pattern Tests")
    class MediatorPatternTests {

        @Test
        @DisplayName("Complete download lifecycle coordinates Downloader, Storage, Bandwidth, and Notifications")
        void testDownloadLifecycleCoordination() {
            DownloaderColleague downloader = new DownloaderColleague();
            StorageColleague storage = new StorageColleague(10_000_000_000L, 1_000_000_000L); // 10 GB
            BandwidthColleague bandwidth = new BandwidthColleague(10_000_000L); // 10 MB/s
            NotificationColleague notifications = new NotificationColleague();

            DownloadCoordinatorMediator mediator = new DownloadCoordinatorMediator(
                    downloader, storage, bandwidth, notifications
            );

            DownloadTask task = new DownloadTask("https://example.org/file.iso", "file.iso", "/storage/file.iso");

            // 1. Start download -> Reserves disk, allocates bandwidth, notifies
            downloader.startDownload(task, 500_000_000L); // 500 MB
            assertEquals(DownloadStatus.DOWNLOADING, task.getStatus());
            assertEquals(500_000_000L, storage.getAllocatedDiskBytes());
            assertEquals(2_097_152L, bandwidth.getAllocatedBandwidthBytesPerSec());
            assertEquals(1, downloader.getActiveDownloadsCount());
            assertTrue(notifications.getInfoCount() >= 1);

            // 2. Report progress
            downloader.reportProgress(task, 250_000_000L);
            assertEquals(250_000_000L, task.getDownloadedBytes());

            // 3. Complete download -> Releases bandwidth, finalizes storage, notifies
            downloader.completeDownload(task);
            assertEquals(DownloadStatus.COMPLETED, task.getStatus());
            assertEquals(0L, bandwidth.getAllocatedBandwidthBytesPerSec());
            assertEquals(0, downloader.getActiveDownloadsCount());
            assertTrue(notifications.getHistory().stream()
                    .anyMatch(msg -> msg.contains("Download finished successfully")));
        }

        @Test
        @DisplayName("Storage low capacity warning coordinates automatic Downloader pause")
        void testStorageLowWarningTriggersDownloaderPause() {
            DownloaderColleague downloader = new DownloaderColleague();
            StorageColleague storage = new StorageColleague(500_000L, 1_000_000L); // Low disk
            BandwidthColleague bandwidth = new BandwidthColleague(5_000_000L);
            NotificationColleague notifications = new NotificationColleague();

            new DownloadCoordinatorMediator(downloader, storage, bandwidth, notifications);

            assertFalse(downloader.isPaused());
            storage.triggerDiskLow();

            assertTrue(downloader.isPaused(), "Downloader must be paused by mediator upon low disk warning");
            assertTrue(storage.isDiskLowDetected());
            assertTrue(notifications.getWarningCount() >= 1);
            assertTrue(notifications.getHistory().stream()
                    .anyMatch(msg -> msg.contains("CRITICAL: Disk capacity critically low")));

            // Resume
            downloader.resumeActiveDownloads();
            assertFalse(downloader.isPaused());
        }

        @Test
        @DisplayName("Bandwidth exceeded coordinates QoS speed throttling on Downloader")
        void testBandwidthExceededCoordinatesThrottling() {
            DownloaderColleague downloader = new DownloaderColleague();
            StorageColleague storage = new StorageColleague(10_000_000L, 1_000_000L);
            BandwidthColleague bandwidth = new BandwidthColleague(2_000_000L); // 2 MB/s pool
            NotificationColleague notifications = new NotificationColleague();

            new DownloadCoordinatorMediator(downloader, storage, bandwidth, notifications);

            assertEquals(0, downloader.getThrottleSpeedKbps());

            // Requesting 5 MB/s when max is 2 MB/s -> Exceeded
            boolean granted = bandwidth.allocateBandwidth(5_000_000L);
            assertFalse(granted);
            assertTrue(bandwidth.isBandwidthCongested());

            // Mediator should have throttled downloader to 512 KB/s
            assertEquals(512, downloader.getThrottleSpeedKbps());
            assertTrue(notifications.getWarningCount() >= 1);
        }

        @Test
        @DisplayName("Download failure coordinates temporary space cleanup, bandwidth release, and error alert")
        void testDownloadFailureCoordinatesCleanupAndAlert() {
            DownloaderColleague downloader = new DownloaderColleague();
            StorageColleague storage = new StorageColleague(10_000_000L, 1_000_000L);
            BandwidthColleague bandwidth = new BandwidthColleague(5_000_000L);
            NotificationColleague notifications = new NotificationColleague();

            new DownloadCoordinatorMediator(downloader, storage, bandwidth, notifications);

            DownloadTask task = new DownloadTask("https://example.org/fail.bin", "fail.bin", "/tmp/fail.bin");
            downloader.startDownload(task, 4_000_000L);
            downloader.reportProgress(task, 1_500_000L);

            downloader.failDownload(task, "Connection timeout");
            assertEquals(DownloadStatus.ERROR, task.getStatus());
            assertEquals(0L, bandwidth.getAllocatedBandwidthBytesPerSec());
            assertEquals(0, downloader.getActiveDownloadsCount());
            assertTrue(notifications.getErrorCount() >= 1);
            assertTrue(notifications.getHistory().stream()
                    .anyMatch(msg -> msg.contains("Connection timeout")));
        }

        @Test
        @DisplayName("Colleague registration, naming, and mediator communication wiring")
        void testColleagueRegistrationAndWiring() {
            DownloadCoordinatorMediator mediator = new DownloadCoordinatorMediator();

            DownloaderColleague downloader = new DownloaderColleague();
            StorageColleague storage = new StorageColleague(1000L, 100L);
            BandwidthColleague bandwidth = new BandwidthColleague(1000L);
            NotificationColleague notifications = new NotificationColleague();

            mediator.registerColleague(downloader);
            mediator.registerColleague(storage);
            mediator.registerColleague(bandwidth);
            mediator.registerColleague(notifications);

            assertEquals("DownloaderEngine", downloader.getName());
            assertEquals("StorageManager", storage.getName());
            assertEquals("BandwidthManager", bandwidth.getName());
            assertEquals("NotificationService", notifications.getName());

            assertEquals(downloader, mediator.getDownloaderColleague());
            assertEquals(storage, mediator.getStorageColleague());
            assertEquals(bandwidth, mediator.getBandwidthColleague());
            assertEquals(notifications, mediator.getNotificationColleague());

            notifications.notifyInfo("test info");
            notifications.notifyWarning("test warning");
            notifications.notifyError("test error");

            assertEquals(1, notifications.getInfoCount());
            assertEquals(1, notifications.getWarningCount());
            assertEquals(1, notifications.getErrorCount());
            assertEquals(3, notifications.getHistory().size());

            notifications.clearHistory();
            assertEquals(0, notifications.getHistory().size());
            assertEquals(0, notifications.getInfoCount());
        }
    }

    @Nested
    @DisplayName("Memento Pattern Tests")
    class MementoPatternTests {

        @Test
        @DisplayName("Originator captures and completely restores internal transfer state")
        void testSaveAndRestoreSnapshot() {
            DownloadTaskOriginator originator = new DownloadTaskOriginator(
                    "https://example.org/archive.tar.gz", "archive.tar.gz", "/tmp/archive.tar.gz"
            );

            originator.start(1_000_000L);
            originator.recordChunkProgress(250_000L, 0);
            originator.recordChunkProgress(500_000L, 1);

            TaskMemento snapshot = originator.saveState("checkpoint-50%");
            assertEquals("checkpoint-50%", snapshot.getLabel());
            assertEquals(500_000L, snapshot.getDownloadedBytes());
            assertEquals(DownloadStatus.DOWNLOADING, snapshot.getStatus());
            assertNotNull(snapshot.getCheckpointId());
            assertNotNull(snapshot.getTimestamp());

            // Mutate state further
            originator.recordChunkProgress(750_000L, 2);
            originator.fail("Simulated crash");
            assertEquals(DownloadStatus.ERROR, originator.getStatus());
            assertEquals(750_000L, originator.getDownloadedBytes());
            assertEquals(3, originator.getCompletedChunkIndices().size());

            // Restore from snapshot
            originator.restoreState(snapshot);
            assertEquals(DownloadStatus.DOWNLOADING, originator.getStatus());
            assertEquals(500_000L, originator.getDownloadedBytes());
            assertEquals(Set.of(0, 1), originator.getCompletedChunkIndices());
            assertEquals("archive.tar.gz", originator.getFileName());
        }

        @Test
        @DisplayName("Caretaker manages sequential undo and redo stack of checkpoints")
        void testCheckpointCaretakerUndoAndRedoStack() {
            DownloadTaskOriginator originator = new DownloadTaskOriginator(
                    "https://example.org/film.mkv", "film.mkv", "/tmp"
            );
            DownloadCheckpointCaretaker caretaker = new DownloadCheckpointCaretaker();

            originator.start(10_000_000L);

            // Checkpoint 1
            originator.recordChunkProgress(2_000_000L, 0);
            TaskMemento cp1 = originator.saveState("cp1");
            caretaker.pushCheckpoint(cp1);

            // Checkpoint 2
            originator.recordChunkProgress(5_000_000L, 1);
            TaskMemento cp2 = originator.saveState("cp2");
            caretaker.pushCheckpoint(cp2);

            assertEquals(2, caretaker.getHistorySize());
            assertEquals(0, caretaker.getRedoSize());

            // Failure
            originator.fail("Disk write timeout");

            // Rollback 1: restores Checkpoint 2 (5MB)
            Optional<TaskMemento> pop1 = caretaker.popCheckpoint();
            assertTrue(pop1.isPresent());
            originator.restoreState(pop1.get());
            assertEquals(5_000_000L, originator.getDownloadedBytes());
            assertEquals(1, caretaker.getHistorySize());
            assertEquals(1, caretaker.getRedoSize());

            // Rollback 2: restores Checkpoint 1 (2MB)
            Optional<TaskMemento> pop2 = caretaker.popCheckpoint();
            assertTrue(pop2.isPresent());
            originator.restoreState(pop2.get());
            assertEquals(2_000_000L, originator.getDownloadedBytes());
            assertEquals(0, caretaker.getHistorySize());
            assertEquals(2, caretaker.getRedoSize());

            // Redo: restores Checkpoint 2 (5MB)
            Optional<TaskMemento> redo = caretaker.redoCheckpoint();
            assertTrue(redo.isPresent());
            originator.restoreState(redo.get());
            assertEquals(5_000_000L, originator.getDownloadedBytes());
        }

        @Test
        @DisplayName("Caretaker manages named milestone checkpoints")
        void testNamedCheckpoints() {
            DownloadTaskOriginator originator = new DownloadTaskOriginator(
                    "https://example.org/dataset.csv", "dataset.csv", "/tmp"
            );
            DownloadCheckpointCaretaker caretaker = new DownloadCheckpointCaretaker();

            originator.start(100_000L);
            originator.recordChunkProgress(100_000L, 0);
            originator.pause();

            TaskMemento goldenState = originator.saveState("Golden Completed State");
            caretaker.saveNamedCheckpoint("golden-state", goldenState);

            Optional<TaskMemento> retrieved = caretaker.getNamedCheckpoint("golden-state");
            assertTrue(retrieved.isPresent());
            assertEquals("Golden Completed State", retrieved.get().getLabel());
            assertEquals(100_000L, retrieved.get().getDownloadedBytes());

            assertTrue(caretaker.getNamedCheckpoint("non-existent").isEmpty());
        }

        @Test
        @DisplayName("Caretaker empty stack operations and clear history")
        void testCaretakerEmptyOperations() {
            DownloadCheckpointCaretaker caretaker = new DownloadCheckpointCaretaker();
            assertTrue(caretaker.popCheckpoint().isEmpty());
            assertTrue(caretaker.redoCheckpoint().isEmpty());
            assertEquals(0, caretaker.getHistorySize());
            assertEquals(0, caretaker.getCheckpointLog().size());

            caretaker.saveNamedCheckpoint("test", new DownloadTaskMemento(
                    "label", null, "http://a", "f.bin", "/tmp", 10L, 100L, DownloadStatus.CREATED, null, null
            ));
            assertEquals(1, caretaker.getCheckpointLog().size());

            caretaker.clearHistory();
            assertEquals(0, caretaker.getHistorySize());
            assertEquals(0, caretaker.getCheckpointLog().size());
            assertTrue(caretaker.getNamedCheckpoint("test").isEmpty());
        }
    }

    @Nested
    @DisplayName("Observer Pattern Tests")
    class ObserverPatternTests {

        @Test
        @DisplayName("DownloadTaskSubject broadcasts lifecycle and progress events to all registered observers")
        void testDownloadTaskSubjectLifecycleNotification() {
            DownloadTaskSubject subject = new DownloadTaskSubject(
                    "https://example.org/ubuntu.iso", "ubuntu.iso", "/downloads"
            );

            ConsoleProgressObserver consoleObserver = new ConsoleProgressObserver();
            AuditLoggingObserver auditObserver = new AuditLoggingObserver();

            subject.addObserver(consoleObserver);
            subject.addObserver(auditObserver);
            assertEquals(2, subject.getObserverCount());

            // Start
            subject.start(1_000_000_000L); // 1 GB
            assertEquals(DownloadStatus.DOWNLOADING, subject.getStatus());

            // Progress updates
            subject.updateProgress(250_000_000L, 25_000_000L);
            assertEquals(25.0, consoleObserver.getLastReportedPercentage(), 0.01);
            assertEquals(1, consoleObserver.getUpdateCount());

            subject.updateProgress(500_000_000L, 50_000_000L);
            assertEquals(50.0, consoleObserver.getLastReportedPercentage(), 0.01);
            assertEquals(2, consoleObserver.getUpdateCount());

            // Pause
            subject.pause();
            assertEquals(DownloadStatus.PAUSED, subject.getStatus());

            // Complete
            subject.complete();
            assertEquals(DownloadStatus.COMPLETED, subject.getStatus());
            assertEquals(1_000_000_000L, subject.getDownloadedBytes());

            // Verify audit logs captured all events
            assertTrue(auditObserver.getLogCount() >= 5);
            List<DownloadProgressEvent> logs = auditObserver.getAuditLog();
            assertEquals(DownloadStatus.DOWNLOADING, logs.get(0).getStatus());
            assertEquals(DownloadStatus.COMPLETED, logs.get(logs.size() - 1).getStatus());
        }

        @Test
        @DisplayName("SpeedTelemetryObserver computes average speed, peak speed, and ETA correctly")
        void testSpeedTelemetryObserverCalculations() {
            DownloadTaskSubject subject = new DownloadTaskSubject(
                    "https://example.org/test.bin", "test.bin", "/tmp"
            );
            SpeedTelemetryObserver speedObserver = new SpeedTelemetryObserver();
            subject.addObserver(speedObserver);

            subject.start(100_000_000L); // 100 MB

            // Sample 1: 10 MB/s
            subject.updateProgress(10_000_000L, 10_000_000L);
            assertEquals(10_000_000L, speedObserver.getPeakSpeedBytesPerSec());
            assertEquals(10_000_000L, speedObserver.getAverageSpeedBytesPerSec());

            // Sample 2: 30 MB/s (new peak)
            subject.updateProgress(40_000_000L, 30_000_000L);
            assertEquals(30_000_000L, speedObserver.getPeakSpeedBytesPerSec());
            assertEquals(20_000_000L, speedObserver.getAverageSpeedBytesPerSec()); // (10 + 30) / 2 = 20 MB/s

            // Sample 3: 20 MB/s
            subject.updateProgress(60_000_000L, 20_000_000L);
            assertEquals(30_000_000L, speedObserver.getPeakSpeedBytesPerSec());
            assertEquals(20_000_000L, speedObserver.getAverageSpeedBytesPerSec()); // (10 + 30 + 20) / 3 = 20 MB/s

            // Remaining bytes = 40 MB. At 20 MB/s average, ETA = 2 seconds
            assertEquals(2L, speedObserver.getEstimatedRemainingSeconds());
        }

        @Test
        @DisplayName("DownloadCompletionWebhookObserver triggers only upon terminal completion or error")
        void testDownloadCompletionWebhookObserver() {
            DownloadTaskSubject subject = new DownloadTaskSubject(
                    "https://example.org/pkg.deb", "pkg.deb", "/tmp"
            );
            DownloadCompletionWebhookObserver webhookObserver =
                    new DownloadCompletionWebhookObserver(URI.create("https://webhook.test/notify"));
            subject.addObserver(webhookObserver);

            assertEquals(0, webhookObserver.getCallbackCount());

            subject.start(10_000L);
            assertEquals(0, webhookObserver.getCallbackCount());

            subject.updateProgress(5_000L, 1_000L);
            assertEquals(0, webhookObserver.getCallbackCount());

            subject.complete();
            assertEquals(1, webhookObserver.getCallbackCount());
            assertNotNull(webhookObserver.getLastTriggeredEvent());
            assertEquals(DownloadStatus.COMPLETED, webhookObserver.getLastTriggeredEvent().getStatus());
            assertEquals("pkg.deb", webhookObserver.getLastTriggeredEvent().getFileName());
        }

        @Test
        @DisplayName("DownloadTaskSubject handles dynamic observer attachment and detachment")
        void testObserverDynamicAddAndRemove() {
            DownloadTaskSubject subject = new DownloadTaskSubject(
                    "https://example.org/data.zip", "data.zip", "/tmp"
            );
            ConsoleProgressObserver obs = new ConsoleProgressObserver();

            assertEquals(0, subject.getObserverCount());
            subject.addObserver(obs);
            assertEquals(1, subject.getObserverCount());

            subject.start(1000L);
            assertTrue(subject.removeObserver(obs));
            assertEquals(0, subject.getObserverCount());
            assertFalse(subject.removeObserver(obs));

            // Updates after removal should not affect observer
            int updatesBefore = obs.getUpdateCount();
            subject.updateProgress(500L, 100L);
            assertEquals(updatesBefore, obs.getUpdateCount());
        }

        @Test
        @DisplayName("DownloadTaskSubject isolates errors thrown by faulty observers")
        void testObserverFaultIsolation() {
            DownloadTaskSubject subject = new DownloadTaskSubject(
                    "https://example.org/data.zip", "data.zip", "/tmp"
            );

            DownloadObserver failingObserver = new DownloadObserver() {
                @Override
                public void onProgressUpdate(DownloadProgressEvent event) {
                    throw new RuntimeException("Simulated observer crash");
                }

                @Override
                public void onStatusChanged(DownloadProgressEvent event) {
                    throw new RuntimeException("Simulated observer crash");
                }

                @Override
                public void onError(DownloadProgressEvent event) {
                }

                @Override
                public String getName() {
                    return "FailingObserver";
                }
            };

            AuditLoggingObserver healthyObserver = new AuditLoggingObserver();

            subject.addObserver(failingObserver);
            subject.addObserver(healthyObserver);

            // Even though failingObserver throws, healthyObserver should receive event without crashing subject
            subject.start(1000L);
            subject.updateProgress(500L, 50L);

            assertEquals(2, healthyObserver.getLogCount());
        }
    }
}
