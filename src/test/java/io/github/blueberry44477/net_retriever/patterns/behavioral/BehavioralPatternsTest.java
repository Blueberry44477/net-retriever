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
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.CanceledDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.CompletedDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.CreatedDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.DownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.DownloadTaskStateContext;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.DownloadingDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.ErrorDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.PausedDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.RetryingDownloadState;
import io.github.blueberry44477.net_retriever.patterns.behavioral.state.StateTransitionRecord;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.AdaptiveDynamicChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.BandwidthProportionalChunkingStrategy;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.ChunkPartitionSummary;
import io.github.blueberry44477.net_retriever.patterns.behavioral.strategy.ChunkPartitioningStrategy;
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
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.DownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.DownloadMetricsVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.DownloadVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.JsonExportVisitor;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.MirroredDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.SegmentedDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.SimpleDownloadElement;
import io.github.blueberry44477.net_retriever.patterns.behavioral.visitor.StorageVerificationVisitor;
import java.io.IOException;
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

    @Nested
    @DisplayName("State Pattern Tests")
    class StatePatternTests {

        @Test
        @DisplayName("Newly created context initializes in CREATED state with valid defaults")
        void testInitialCreatedState() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/file.zip", "file.zip", "/downloads/file.zip", 3
            );

            assertEquals(DownloadStatus.CREATED, context.getStatus());
            assertEquals("CREATED", context.getStateName());
            assertFalse(context.isTerminal());
            assertFalse(context.canResume());
            assertTrue(context.canRetry());
            assertEquals(0L, context.getDownloadedBytes());
            assertEquals(0L, context.getTotalBytes());
            assertEquals(0.0, context.getProgressPercentage());
            assertTrue(context.getState() instanceof CreatedDownloadState);
        }

        @Test
        @DisplayName("Starting from CREATED transitions cleanly to DOWNLOADING")
        void testStartFromCreated() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/file.zip", "file.zip", "/downloads/file.zip"
            );

            context.start(1000L);

            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());
            assertEquals("DOWNLOADING", context.getStateName());
            assertEquals(1000L, context.getTotalBytes());
            assertEquals(0L, context.getDownloadedBytes());
            assertTrue(context.getState() instanceof DownloadingDownloadState);

            List<StateTransitionRecord> history = context.getTransitionHistory();
            assertEquals(1, history.size());
            assertEquals(DownloadStatus.CREATED, history.getFirst().fromStatus());
            assertEquals(DownloadStatus.DOWNLOADING, history.getFirst().toStatus());
        }

        @Test
        @DisplayName("Calling invalid operations from CREATED state throws IllegalStateException")
        void testCreatedStateGuards() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/file.zip", "file.zip", "/downloads/file.zip"
            );

            assertThrows(IllegalStateException.class, () -> context.updateProgress(100L));
            assertThrows(IllegalStateException.class, context::pause);
            assertThrows(IllegalStateException.class, context::resume);
            assertThrows(IllegalStateException.class, context::complete);
            assertThrows(IllegalStateException.class, context::retry);
            assertThrows(IllegalArgumentException.class, () -> context.start(0L));
        }

        @Test
        @DisplayName("Full normal lifecycle transitions: CREATED -> DOWNLOADING -> PAUSED -> RESUMED -> COMPLETED")
        void testNormalLifecycleFlow() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/video.mp4", "video.mp4", "/downloads"
            );

            context.start(2000L);
            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());

            context.updateProgress(500L);
            assertEquals(500L, context.getDownloadedBytes());
            assertEquals(25.0, context.getProgressPercentage());

            context.pause();
            assertEquals(DownloadStatus.PAUSED, context.getStatus());
            assertTrue(context.canResume());
            assertFalse(context.isTerminal());
            assertTrue(context.getState() instanceof PausedDownloadState);

            // Resuming
            context.resume();
            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());
            assertEquals(500L, context.getDownloadedBytes());

            // Complete
            context.updateProgress(2000L);
            assertEquals(100.0, context.getProgressPercentage());
            context.complete();

            assertEquals(DownloadStatus.COMPLETED, context.getStatus());
            assertTrue(context.isTerminal());
            assertFalse(context.canResume());
            assertTrue(context.getState() instanceof CompletedDownloadState);
        }

        @Test
        @DisplayName("Byte progress update enforces non-regression and total byte limits")
        void testProgressUpdateValidations() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/data.bin", "data.bin", "/tmp"
            );
            context.start(1000L);
            context.updateProgress(400L);

            // Regression is rejected
            assertThrows(IllegalArgumentException.class, () -> context.updateProgress(350L));

            // Exceeding totalBytes is rejected
            assertThrows(IllegalArgumentException.class, () -> context.updateProgress(1001L));

            // Valid progress continues
            context.updateProgress(600L);
            assertEquals(600L, context.getDownloadedBytes());
        }

        @Test
        @DisplayName("Transient network failure enters RETRYING state and recovers seamlessly via retry()")
        void testTransientFailureWithRetryRecovery() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/archive.tar", "archive.tar", "/tmp", 3
            );
            context.start(10000L);
            context.updateProgress(4000L);

            context.fail("Connection reset by peer");

            // Status is DOWNLOADING (in retry backoff) and state name is RETRYING
            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());
            assertEquals("RETRYING", context.getStateName());
            assertTrue(context.canResume());
            assertFalse(context.isTerminal());
            assertTrue(context.getState() instanceof RetryingDownloadState);

            // Reconnecting via retry()
            context.retry();
            assertEquals("DOWNLOADING", context.getStateName());
            assertEquals(4000L, context.getDownloadedBytes());

            context.updateProgress(10000L);
            context.complete();
            assertEquals(DownloadStatus.COMPLETED, context.getStatus());
        }

        @Test
        @DisplayName("Exhausting retry attempts transitions permanently to ERROR state")
        void testRetryBudgetExhaustion() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://broken.server/file", "file", "/tmp", 2 // max 2 retries
            );
            context.start(5000L);

            // Fail 1: retrying (attempt 1/2)
            context.fail("Timeout 1");
            assertEquals("RETRYING", context.getStateName());

            // Fail 2: retrying (attempt 2/2)
            context.fail("Timeout 2");
            assertEquals("RETRYING", context.getStateName());

            // Fail 3: budget exhausted -> permanent ERROR
            context.fail("Timeout 3");
            assertEquals(DownloadStatus.ERROR, context.getStatus());
            assertEquals("ERROR", context.getStateName());
            assertFalse(context.isTerminal()); // Error is recoverable via restart
            assertTrue(context.getState() instanceof ErrorDownloadState);
        }

        @Test
        @DisplayName("Failed download in ERROR state can be restarted fresh or retried")
        void testErrorRecoveryViaRestartAndRetry() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/app.dmg", "app.dmg", "/tmp", 0 // 0 retries
            );
            context.start(1000L);
            context.fail("Fatal I/O Error");

            assertEquals(DownloadStatus.ERROR, context.getStatus());

            // Recovery 1: retry from current bytes
            context.retry();
            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());

            // Re-fail
            context.fail("Second failure");
            assertEquals(DownloadStatus.ERROR, context.getStatus());

            // Recovery 2: start from beginning with new size
            context.start(2000L);
            assertEquals(DownloadStatus.DOWNLOADING, context.getStatus());
            assertEquals(0L, context.getDownloadedBytes());
            assertEquals(2000L, context.getTotalBytes());
        }

        @Test
        @DisplayName("Task cancellation from CREATED, DOWNLOADING, and PAUSED transitions to CANCELED")
        void testCancellationScenarios() {
            // Cancel from CREATED
            DownloadTaskStateContext c1 = new DownloadTaskStateContext("https://a.com/1", "1", "/tmp");
            c1.cancel();
            assertEquals(DownloadStatus.CANCELED, c1.getStatus());
            assertTrue(c1.isTerminal());

            // Cancel from DOWNLOADING
            DownloadTaskStateContext c2 = new DownloadTaskStateContext("https://a.com/2", "2", "/tmp");
            c2.start(500L);
            c2.cancel();
            assertEquals(DownloadStatus.CANCELED, c2.getStatus());
            assertTrue(c2.isTerminal());

            // Cancel from PAUSED
            DownloadTaskStateContext c3 = new DownloadTaskStateContext("https://a.com/3", "3", "/tmp");
            c3.start(500L);
            c3.pause();
            c3.cancel();
            assertEquals(DownloadStatus.CANCELED, c3.getStatus());
            assertTrue(c3.isTerminal());
        }

        @Test
        @DisplayName("Terminal states (COMPLETED, CANCELED) strictly forbid any further lifecycle operations")
        void testTerminalStateGuards() {
            DownloadTaskStateContext completed = new DownloadTaskStateContext("https://a.com/c", "c", "/tmp");
            completed.start(100L);
            completed.complete();

            assertThrows(IllegalStateException.class, () -> completed.start(100L));
            assertThrows(IllegalStateException.class, () -> completed.updateProgress(50L));
            assertThrows(IllegalStateException.class, completed::pause);
            assertThrows(IllegalStateException.class, completed::resume);
            assertThrows(IllegalStateException.class, completed::complete);
            assertThrows(IllegalStateException.class, completed::cancel);
            assertThrows(IllegalStateException.class, () -> completed.fail("err"));
            assertThrows(IllegalStateException.class, completed::retry);

            DownloadTaskStateContext canceled = new DownloadTaskStateContext("https://a.com/x", "x", "/tmp");
            canceled.cancel();

            assertThrows(IllegalStateException.class, () -> canceled.start(100L));
            assertThrows(IllegalStateException.class, () -> canceled.updateProgress(50L));
            assertThrows(IllegalStateException.class, canceled::pause);
            assertThrows(IllegalStateException.class, canceled::resume);
            assertThrows(IllegalStateException.class, canceled::complete);
            assertThrows(IllegalStateException.class, canceled::cancel);
            assertThrows(IllegalStateException.class, () -> canceled.fail("err"));
            assertThrows(IllegalStateException.class, canceled::retry);
        }

        @Test
        @DisplayName("StateTransitionRecord captures full chronological audit log of lifecycle events")
        void testStateTransitionAuditHistory() {
            DownloadTaskStateContext context = new DownloadTaskStateContext("https://a.com/log", "log.bin", "/tmp");
            context.start(1000L);
            context.pause();
            context.resume();
            context.complete();

            List<StateTransitionRecord> history = context.getTransitionHistory();
            assertEquals(4, history.size());

            assertEquals(DownloadStatus.CREATED, history.get(0).fromStatus());
            assertEquals(DownloadStatus.DOWNLOADING, history.get(0).toStatus());

            assertEquals(DownloadStatus.DOWNLOADING, history.get(1).fromStatus());
            assertEquals(DownloadStatus.PAUSED, history.get(1).toStatus());

            assertEquals(DownloadStatus.PAUSED, history.get(2).fromStatus());
            assertEquals(DownloadStatus.DOWNLOADING, history.get(2).toStatus());

            assertEquals(DownloadStatus.DOWNLOADING, history.get(3).fromStatus());
            assertEquals(DownloadStatus.COMPLETED, history.get(3).toStatus());

            for (StateTransitionRecord r : history) {
                assertNotNull(r.timestamp());
                assertNotNull(r.reason());
            }
        }

        @Test
        @DisplayName("toDownloadTask converts state context to matching domain DownloadTask entity")
        void testToDownloadTaskConversion() {
            DownloadTaskStateContext context = new DownloadTaskStateContext(
                    "https://example.com/entity.iso", "entity.iso", "/downloads/entity.iso"
            );
            context.start(5000L);
            context.updateProgress(2500L);
            context.pause();

            var task = context.toDownloadTask();
            assertEquals("https://example.com/entity.iso", task.getUrl());
            assertEquals("entity.iso", task.getFileName());
            assertEquals(5000L, task.getTotalBytes());
            assertEquals(2500L, task.getDownloadedBytes());
            assertEquals(DownloadStatus.PAUSED, task.getStatus());
        }
    }

    @Nested
    @DisplayName("Strategy Pattern Tests")
    class StrategyPatternTests {

        @Test
        @DisplayName("DownloadChunkRange validates bounds, checks containment, overlap, adjacency and HTTP Range")
        void testDownloadChunkRangeValidation() {
            DownloadChunkRange chunk0 = DownloadChunkRange.of(0, 0, 999, 0);
            DownloadChunkRange chunk1 = DownloadChunkRange.of(1, 1000, 1999, 1);
            DownloadChunkRange chunkOverlap = DownloadChunkRange.of(2, 500, 1500, 2);

            assertEquals(1000L, chunk0.length());
            assertEquals("bytes=0-999", chunk0.toHttpRangeHeader());
            assertEquals("bytes=1000-1999", chunk1.toHttpRangeHeader());

            assertTrue(chunk0.contains(0));
            assertTrue(chunk0.contains(500));
            assertTrue(chunk0.contains(999));
            assertFalse(chunk0.contains(1000));

            assertTrue(chunk0.isAdjacent(chunk1));
            assertFalse(chunk0.overlaps(chunk1));

            assertTrue(chunk0.overlaps(chunkOverlap));
            assertTrue(chunk1.overlaps(chunkOverlap));

            assertThrows(IllegalArgumentException.class, () -> new DownloadChunkRange(-1, 0, 10, 11, 0));
            assertThrows(IllegalArgumentException.class, () -> new DownloadChunkRange(0, -1, 10, 12, 0));
            assertThrows(IllegalArgumentException.class, () -> new DownloadChunkRange(0, 10, 5, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new DownloadChunkRange(0, 0, 10, 999, 0)); // wrong length
        }

        @Test
        @DisplayName("FixedSizeChunkingStrategy divides payload evenly when total is a multiple of chunk size")
        void testFixedSizeChunkingStrategyDivisible() {
            ChunkPartitioningStrategy strategy = new FixedSizeChunkingStrategy(2_000_000L); // 2 MB
            DownloadPartitionContext context = new DownloadPartitionContext(strategy);

            List<DownloadChunkRange> chunks = context.executePartition(10_000_000L, 4); // 10 MB across 4 workers

            assertEquals(5, chunks.size());
            for (DownloadChunkRange c : chunks) {
                assertEquals(2_000_000L, c.length());
            }

            // Verify round robin worker assignment
            assertEquals(0, chunks.get(0).assignedWorkerId());
            assertEquals(1, chunks.get(1).assignedWorkerId());
            assertEquals(2, chunks.get(2).assignedWorkerId());
            assertEquals(3, chunks.get(3).assignedWorkerId());
            assertEquals(0, chunks.get(4).assignedWorkerId());
        }

        @Test
        @DisplayName("FixedSizeChunkingStrategy assigns remainder to last chunk when not evenly divisible")
        void testFixedSizeChunkingStrategyWithRemainder() {
            DownloadPartitionContext context = new DownloadPartitionContext(new FixedSizeChunkingStrategy(3_000_000L));

            List<DownloadChunkRange> chunks = context.executePartition(10_000_000L, 2); // 10 MB / 3 MB = 3 full + 1 remainder

            assertEquals(4, chunks.size());
            assertEquals(3_000_000L, chunks.get(0).length());
            assertEquals(3_000_000L, chunks.get(1).length());
            assertEquals(3_000_000L, chunks.get(2).length());
            assertEquals(1_000_000L, chunks.get(3).length());
            assertEquals(9_999_999L, chunks.get(3).endByte());
        }

        @Test
        @DisplayName("FixedSizeChunkingStrategy produces single chunk if payload size is smaller than chunk size")
        void testFixedSizeChunkingFileSmallerThanChunk() {
            DownloadPartitionContext context = new DownloadPartitionContext(new FixedSizeChunkingStrategy(5_000_000L));
            List<DownloadChunkRange> chunks = context.executePartition(1_000_000L, 4);

            assertEquals(1, chunks.size());
            assertEquals(0L, chunks.getFirst().startByte());
            assertEquals(999_999L, chunks.getFirst().endByte());
            assertEquals(1_000_000L, chunks.getFirst().length());
        }

        @Test
        @DisplayName("EqualWorkerChunkingStrategy produces exact worker count with 1-byte balanced distribution")
        void testEqualWorkerChunkingStrategy() {
            DownloadPartitionContext context = new DownloadPartitionContext(new EqualWorkerChunkingStrategy());

            // 10 bytes across 3 workers -> 4, 3, 3 bytes
            List<DownloadChunkRange> chunks = context.executePartition(10L, 3);
            assertEquals(3, chunks.size());
            assertEquals(4L, chunks.get(0).length());
            assertEquals(3L, chunks.get(1).length());
            assertEquals(3L, chunks.get(2).length());

            assertEquals(0, chunks.get(0).assignedWorkerId());
            assertEquals(1, chunks.get(1).assignedWorkerId());
            assertEquals(2, chunks.get(2).assignedWorkerId());

            // 100 MB across 4 workers -> 25 MB each
            List<DownloadChunkRange> largeChunks = context.executePartition(100_000_000L, 4);
            assertEquals(4, largeChunks.size());
            for (DownloadChunkRange c : largeChunks) {
                assertEquals(25_000_000L, c.length());
            }
        }

        @Test
        @DisplayName("EqualWorkerChunkingStrategy clamps workers when totalBytes is less than worker count")
        void testEqualWorkerChunkingWorkerCountExceedsBytes() {
            DownloadPartitionContext context = new DownloadPartitionContext(new EqualWorkerChunkingStrategy());
            List<DownloadChunkRange> chunks = context.executePartition(3L, 10);

            assertEquals(3, chunks.size());
            for (int i = 0; i < 3; i++) {
                assertEquals(1L, chunks.get(i).length());
                assertEquals(i, chunks.get(i).assignedWorkerId());
            }
        }

        @Test
        @DisplayName("AdaptiveDynamicChunkingStrategy automatically adapts chunk count based on size tiers")
        void testAdaptiveDynamicChunkingTiers() {
            DownloadPartitionContext context = new DownloadPartitionContext(new AdaptiveDynamicChunkingStrategy());

            // Tier 1: Small (< 5 MB) -> 1 chunk
            List<DownloadChunkRange> small = context.executePartition(2_000_000L, 8);
            assertEquals(1, small.size());

            // Tier 2: Medium (5 MB - 50 MB) -> 4 chunks
            List<DownloadChunkRange> medium = context.executePartition(20_000_000L, 8);
            assertEquals(4, medium.size());

            // Tier 3: Large (50 MB - 500 MB) -> 8 chunks
            List<DownloadChunkRange> large = context.executePartition(200_000_000L, 16);
            assertEquals(8, large.size());

            // Tier 4: Huge (>= 500 MB) -> 16 chunks
            List<DownloadChunkRange> huge = context.executePartition(1_000_000_000L, 32);
            assertEquals(16, huge.size());
        }

        @Test
        @DisplayName("BandwidthProportionalChunkingStrategy divides chunks proportionally to worker speeds")
        void testBandwidthProportionalChunkingStrategy() {
            // Worker 0: 3x speed, Worker 1: 1x speed (Total: 4 parts, 75% and 25%)
            DownloadPartitionContext context = new DownloadPartitionContext(
                    new BandwidthProportionalChunkingStrategy(List.of(3.0, 1.0))
            );

            List<DownloadChunkRange> chunks = context.executePartition(100_000_000L, 2);
            assertEquals(2, chunks.size());

            assertEquals(75_000_000L, chunks.get(0).length());
            assertEquals(0L, chunks.get(0).startByte());
            assertEquals(74_999_999L, chunks.get(0).endByte());

            assertEquals(25_000_000L, chunks.get(1).length());
            assertEquals(75_000_000L, chunks.get(1).startByte());
            assertEquals(99_999_999L, chunks.get(1).endByte());

            // 4 workers with speeds: 100, 50, 25, 25
            context.setStrategy(new BandwidthProportionalChunkingStrategy(List.of(100.0, 50.0, 25.0, 25.0)));
            List<DownloadChunkRange> prop4 = context.executePartition(100_000_000L, 4);
            assertEquals(4, prop4.size());
            assertEquals(50_000_000L, prop4.get(0).length());
            assertEquals(25_000_000L, prop4.get(1).length());
            assertEquals(12_500_000L, prop4.get(2).length());
            assertEquals(12_500_000L, prop4.get(3).length());
        }

        @Test
        @DisplayName("DownloadPartitionContext supports dynamic runtime strategy switching")
        void testRuntimeStrategySwitching() {
            DownloadPartitionContext context = new DownloadPartitionContext(new FixedSizeChunkingStrategy(50_000_000L));
            assertEquals("FixedSizeChunkingStrategy", context.getStrategy().getStrategyName());

            List<DownloadChunkRange> fixed = context.executePartition(100_000_000L, 2);
            assertEquals(2, fixed.size());

            // Switch to equal worker
            context.setStrategy(new EqualWorkerChunkingStrategy());
            assertEquals("EqualWorkerChunkingStrategy", context.getStrategy().getStrategyName());

            List<DownloadChunkRange> equal = context.executePartition(100_000_000L, 5);
            assertEquals(5, equal.size());
        }

        @Test
        @DisplayName("DownloadPartitionContext detects and rejects faulty strategies violating invariants")
        void testPartitionInvariantsEnforcement() {
            // Faulty strategy with a gap
            ChunkPartitioningStrategy gapStrategy = new ChunkPartitioningStrategy() {
                @Override
                public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
                    return List.of(
                            DownloadChunkRange.of(0, 0, 400, 0),
                            DownloadChunkRange.of(1, 500, 999, 1) // Gap: 401-499 missing!
                    );
                }

                @Override
                public String getStrategyName() {
                    return "GapStrategy";
                }

                @Override
                public String getDescription() {
                    return "Broken";
                }
            };

            DownloadPartitionContext context = new DownloadPartitionContext(gapStrategy);
            assertThrows(IllegalStateException.class, () -> context.executePartition(1000L, 2));

            // Faulty strategy with an overlap
            ChunkPartitioningStrategy overlapStrategy = new ChunkPartitioningStrategy() {
                @Override
                public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
                    return List.of(
                            DownloadChunkRange.of(0, 0, 600, 0),
                            DownloadChunkRange.of(1, 500, 999, 1) // Overlap: 500-600 duplicated!
                    );
                }

                @Override
                public String getStrategyName() {
                    return "OverlapStrategy";
                }

                @Override
                public String getDescription() {
                    return "Broken";
                }
            };

            context.setStrategy(overlapStrategy);
            assertThrows(IllegalStateException.class, () -> context.executePartition(1000L, 2));
        }

        @Test
        @DisplayName("Validation guards reject invalid parameters across strategies")
        void testInvalidParameters() {
            DownloadPartitionContext context = new DownloadPartitionContext(new EqualWorkerChunkingStrategy());

            assertThrows(IllegalArgumentException.class, () -> context.executePartition(0L, 4));
            assertThrows(IllegalArgumentException.class, () -> context.executePartition(-100L, 4));
            assertThrows(IllegalArgumentException.class, () -> context.executePartition(1000L, 0));
            assertThrows(IllegalArgumentException.class, () -> context.executePartition(1000L, -1));

            assertThrows(IllegalArgumentException.class, () -> new FixedSizeChunkingStrategy(0L));
            assertThrows(IllegalArgumentException.class, () -> new BandwidthProportionalChunkingStrategy(List.of()));
            assertThrows(IllegalArgumentException.class, () -> new BandwidthProportionalChunkingStrategy(List.of(-1.0)));
        }

        @Test
        @DisplayName("ChunkPartitionSummary computes accurate analytical metrics")
        void testChunkPartitionSummaryMetrics() {
            DownloadPartitionContext context = new DownloadPartitionContext(new EqualWorkerChunkingStrategy());
            List<DownloadChunkRange> chunks = context.executePartition(100_000_000L, 4);

            ChunkPartitionSummary summary = context.getPartitionSummary(chunks);
            assertEquals(4, summary.totalChunks());
            assertEquals(100_000_000L, summary.totalBytes());
            assertEquals(25_000_000L, summary.minChunkSize());
            assertEquals(25_000_000L, summary.maxChunkSize());
            assertEquals(25_000_000.0, summary.averageChunkSize());
            assertEquals(1.0, summary.loadImbalanceRatio());
            assertEquals(4, summary.workerByteDistribution().size());
            assertEquals(25_000_000L, summary.workerByteDistribution().get(0));
        }
    }

    @Nested
    @DisplayName("Template Method Pattern Tests")
    class TemplateMethodPatternTests {

        @Test
        @DisplayName("HttpDownloadWorkflow executes all 10 template steps in invariant order")
        void testHttpDownloadWorkflowSuccess() {
            AbstractDownloadWorkflow workflow = new HttpDownloadWorkflow();
            DownloadExecutionResult result = workflow.executeDownload(
                    "https://example.com/file.bin", "/tmp/file.bin"
            );

            assertTrue(result.success());
            assertEquals("HttpDownloadWorkflow", result.workflowName());
            assertEquals(50_000_000L, result.bytesTransferred());
            assertEquals(50_000_000L, result.totalBytes());
            assertNotNull(result.checksum());
            assertTrue(result.checksum().startsWith("sha256:"));

            List<String> steps = result.executionSteps();
            // HTTP workflow does not require sparse pre-allocation, so step 6 is skipped -> 9 steps total
            assertEquals(9, steps.size());
            assertFalse(steps.stream().anyMatch(s -> s.contains("6. Pre-allocate")));
            assertTrue(steps.get(0).contains("1. Validate"));
            assertTrue(steps.get(1).contains("2. Prepare destination"));
            assertTrue(steps.get(2).contains("3. Connect"));
            assertTrue(steps.get(3).contains("4. Fetch remote metadata"));
            assertTrue(steps.get(4).contains("5. Pre-download check"));
            assertTrue(steps.get(5).contains("7. Transfer payload bytes"));
            assertTrue(steps.get(6).contains("8. Verify file integrity"));
            assertTrue(steps.get(7).contains("9. Execute post-processing"));
            assertTrue(steps.get(8).contains("10. Workflow completed successfully"));
        }

        @Test
        @DisplayName("ResumableDownloadWorkflow resumes from existing byte offset, downloading only missing tail")
        void testResumableDownloadWorkflowPartialFile() {
            // Existing file has 40 MB of 100 MB -> transfers only remaining 60 MB
            AbstractDownloadWorkflow workflow = new ResumableDownloadWorkflow(40_000_000L);
            DownloadExecutionResult result = workflow.executeDownload(
                    "https://example.com/dataset.zip", "/downloads/dataset.zip"
            );

            assertTrue(result.success());
            assertEquals(100_000_000L, result.totalBytes());
            assertEquals(60_000_000L, result.bytesTransferred());
            assertEquals("sha256:resumed-checksum-valid", result.checksum());
        }

        @Test
        @DisplayName("ResumableDownloadWorkflow detects completed file and performs zero byte transfer")
        void testResumableDownloadWorkflowAlreadyComplete() {
            AbstractDownloadWorkflow workflow = new ResumableDownloadWorkflow(100_000_000L);
            DownloadExecutionResult result = workflow.executeDownload(
                    "https://example.com/dataset.zip", "/downloads/dataset.zip"
            );

            assertTrue(result.success());
            assertEquals(0L, result.bytesTransferred());
        }

        @Test
        @DisplayName("BitTorrentDownloadWorkflow forces sparse disk pre-allocation step via hook override")
        void testBitTorrentDownloadWorkflowPreAllocation() {
            AbstractDownloadWorkflow workflow = new BitTorrentDownloadWorkflow(64);
            DownloadExecutionResult result = workflow.executeDownload(
                    "magnet:?xt=urn:btih:da39a3ee5e6b4b0d3255bfef95601890afd80709", "/downloads/linux.iso"
            );

            assertTrue(result.success());
            assertEquals(1_000_000_000L, result.bytesTransferred());
            assertTrue(result.checksum().startsWith("sha1:"));

            // Must include step 6 (pre-allocate storage)
            boolean hasPreAllocation = result.executionSteps().stream()
                    .anyMatch(step -> step.contains("6. Pre-allocate storage"));
            assertTrue(hasPreAllocation);
        }

        @Test
        @DisplayName("CloudStorageDownloadWorkflow downloads multipart object and checks ETag")
        void testCloudStorageDownloadWorkflow() {
            AbstractDownloadWorkflow workflow = new CloudStorageDownloadWorkflow("test-s3-bucket");
            DownloadExecutionResult result = workflow.executeDownload(
                    "s3://test-s3-bucket/images/photo.raw", "/downloads/photo.raw"
            );

            assertTrue(result.success());
            assertEquals(250_000_000L, result.bytesTransferred());
            assertTrue(result.checksum().contains("9b105d4bb446482f6943f732b1a6238c-10"));
        }

        @Test
        @DisplayName("Template Method guarantees parameter validation, failure reporting, and finally cleanup")
        void testTemplateMethodFailureHandling() {
            AbstractDownloadWorkflow workflow = new HttpDownloadWorkflow();

            // Null / blank URLs
            DownloadExecutionResult r1 = workflow.executeDownload("", "/tmp/out");
            assertFalse(r1.success());
            assertNotNull(r1.errorMessage());

            // Invalid URI scheme
            DownloadExecutionResult r2 = workflow.executeDownload("invalid-scheme-url", "/tmp/out");
            assertFalse(r2.success());
            assertTrue(r2.errorMessage().contains("Invalid URI scheme"));
            assertTrue(r2.executionSteps().getLast().startsWith("FAILED:"));
        }

        @Test
        @DisplayName("Custom workflow with custom hook overrides executes hooks at precise algorithmic stages")
        void testCustomWorkflowHooks() {
            boolean[] hookCalled = new boolean[3]; // [preDownload, verifyIntegrity, postProcess]

            AbstractDownloadWorkflow customWorkflow = new AbstractDownloadWorkflow("CustomWorkflow") {
                @Override
                protected void connect(String sourceUrl) throws IOException {}

                @Override
                protected long fetchContentLength(String sourceUrl) throws IOException { return 1000L; }

                @Override
                protected long transferPayload(String sourceUrl, java.nio.file.Path destination, long startByte, long expectedSize) throws IOException {
                    return expectedSize - startByte;
                }

                @Override
                protected void disconnect() {}

                @Override
                protected long preDownloadCheck(String sourceUrl, java.nio.file.Path destination, long remoteSize) throws IOException {
                    hookCalled[0] = true;
                    return 200L;
                }

                @Override
                protected String verifyIntegrity(java.nio.file.Path destination) throws IOException {
                    hookCalled[1] = true;
                    return "custom-hash";
                }

                @Override
                protected void postProcess(java.nio.file.Path destination) throws IOException {
                    hookCalled[2] = true;
                }
            };

            DownloadExecutionResult result = customWorkflow.executeDownload("https://custom.org/test", "/tmp/custom");
            assertTrue(result.success());
            assertEquals(800L, result.bytesTransferred()); // 1000 - 200
            assertEquals("custom-hash", result.checksum());
            assertTrue(hookCalled[0]);
            assertTrue(hookCalled[1]);
            assertTrue(hookCalled[2]);
        }
    }

    @Nested
    @DisplayName("Visitor Pattern Tests")
    class VisitorPatternTests {

        @Test
        @DisplayName("SimpleDownloadElement correctly dispatches to visitor.visit(SimpleDownloadElement)")
        void testSimpleDownloadElementAccept() {
            SimpleDownloadElement element = new SimpleDownloadElement(
                    "readme.txt", "https://example.com/readme.txt", 1000L, 1000L, "text/plain", "hash"
            );

            boolean[] visited = new boolean[1];
            DownloadVisitor visitor = new DownloadVisitor() {
                @Override public void visit(SimpleDownloadElement e) { visited[0] = true; assertEquals("readme.txt", e.getName()); }
                @Override public void visit(SegmentedDownloadElement e) {}
                @Override public void visit(MirroredDownloadElement e) {}
                @Override public void visit(BatchDownloadElement e) {}
            };

            element.accept(visitor);
            assertTrue(visited[0]);
            assertEquals(100.0, element.getProgressPercentage());
        }

        @Test
        @DisplayName("SegmentedDownloadElement correctly dispatches to visitor.visit(SegmentedDownloadElement)")
        void testSegmentedDownloadElementAccept() {
            SegmentedDownloadElement element = new SegmentedDownloadElement(
                    "video.mp4", "https://example.com/video.mp4", 10_000L, 5_000L, 4, 2
            );

            boolean[] visited = new boolean[1];
            DownloadVisitor visitor = new DownloadVisitor() {
                @Override public void visit(SimpleDownloadElement e) {}
                @Override public void visit(SegmentedDownloadElement e) { visited[0] = true; assertEquals(4, e.getChunkCount()); }
                @Override public void visit(MirroredDownloadElement e) {}
                @Override public void visit(BatchDownloadElement e) {}
            };

            element.accept(visitor);
            assertTrue(visited[0]);
            assertEquals(50.0, element.getProgressPercentage());
        }

        @Test
        @DisplayName("MirroredDownloadElement correctly dispatches to visitor.visit(MirroredDownloadElement)")
        void testMirroredDownloadElementAccept() {
            MirroredDownloadElement element = new MirroredDownloadElement(
                    "archive.tar", "https://primary.com/archive.tar", List.of("https://m1.com/a", "https://m2.com/a"), 50_000L, 25_000L
            );

            boolean[] visited = new boolean[1];
            DownloadVisitor visitor = new DownloadVisitor() {
                @Override public void visit(SimpleDownloadElement e) {}
                @Override public void visit(SegmentedDownloadElement e) {}
                @Override public void visit(MirroredDownloadElement e) { visited[0] = true; assertEquals(2, e.getMirrorUrls().size()); }
                @Override public void visit(BatchDownloadElement e) {}
            };

            element.accept(visitor);
            assertTrue(visited[0]);
        }

        @Test
        @DisplayName("DownloadMetricsVisitor aggregates metrics across hierarchical composite structure")
        void testDownloadMetricsVisitor() {
            BatchDownloadElement root = new BatchDownloadElement("Root");
            root.addElement(new SimpleDownloadElement("1.txt", "https://a.com/1", 100L, 100L, "text/plain", "h1"));
            root.addElement(new SegmentedDownloadElement("2.bin", "https://a.com/2", 400L, 200L, 4, 2));

            BatchDownloadElement sub = new BatchDownloadElement("Sub");
            sub.addElement(new MirroredDownloadElement("3.iso", "https://a.com/3", List.of("https://m.com/3"), 500L, 100L));
            root.addElement(sub);

            DownloadMetricsVisitor visitor = new DownloadMetricsVisitor();
            root.accept(visitor);

            assertEquals(3, visitor.getTotalFilesCount());
            assertEquals(2, visitor.getTotalBatchesCount()); // Root + Sub
            assertEquals(4, visitor.getTotalSegmentsCount());
            assertEquals(2, visitor.getTotalMirrorsCount()); // 1 mirror + 1 primary
            assertEquals(1000L, visitor.getAggregatedTotalBytes());
            assertEquals(400L, visitor.getAggregatedDownloadedBytes());
            assertEquals(40.0, visitor.getOverallProgressPercentage());
        }

        @Test
        @DisplayName("StorageVerificationVisitor flags dangerous file extensions, insecure URLs, and duplicate names")
        void testStorageVerificationVisitorWarnings() {
            BatchDownloadElement batch = new BatchDownloadElement("Untrusted");
            batch.addElement(new SimpleDownloadElement("payload.exe", "https://a.com/payload.exe", 1000L, 0L, "app", "h1"));
            batch.addElement(new SimpleDownloadElement("script.sh", "http://insecure.com/script.sh", 500L, 0L, "sh", "h2"));
            batch.addElement(new SimpleDownloadElement("payload.exe", "https://a.com/payload2.exe", 1000L, 0L, "app", "h3")); // duplicate name!

            StorageVerificationVisitor visitor = new StorageVerificationVisitor();
            batch.accept(visitor);

            assertFalse(visitor.isApproved());
            assertEquals(2500L, visitor.getRequiredRemainingDiskBytes());
            List<String> warnings = visitor.getWarnings();
            assertTrue(warnings.stream().anyMatch(w -> w.contains(".exe")));
            assertTrue(warnings.stream().anyMatch(w -> w.contains(".sh")));
            assertTrue(warnings.stream().anyMatch(w -> w.contains("Insecure HTTP")));
            assertTrue(warnings.stream().anyMatch(w -> w.contains("Filename collision")));
        }

        @Test
        @DisplayName("StorageVerificationVisitor approves clean and secure download batch")
        void testStorageVerificationVisitorApproved() {
            BatchDownloadElement batch = new BatchDownloadElement("SecureBatch");
            batch.addElement(new SimpleDownloadElement("document.pdf", "https://secure.org/doc.pdf", 2000L, 1000L, "pdf", "h"));
            batch.addElement(new SegmentedDownloadElement("archive.tar.gz", "https://secure.org/arch.tar.gz", 8000L, 4000L, 4, 2));

            StorageVerificationVisitor visitor = new StorageVerificationVisitor();
            batch.accept(visitor);

            assertTrue(visitor.isApproved());
            assertTrue(visitor.getWarnings().isEmpty());
            assertEquals(5000L, visitor.getRequiredRemainingDiskBytes());
        }

        @Test
        @DisplayName("JsonExportVisitor serializes download tree into structured JSON")
        void testJsonExportVisitor() {
            BatchDownloadElement batch = new BatchDownloadElement("JsonTest");
            batch.addElement(new SimpleDownloadElement("doc.txt", "https://a.com/doc.txt", 100L, 50L, "text/plain", "h1"));
            batch.addElement(new SegmentedDownloadElement("chunks.bin", "https://a.com/c.bin", 200L, 100L, 2, 2));

            JsonExportVisitor visitor = new JsonExportVisitor();
            batch.accept(visitor);
            String json = visitor.getJsonOutput();

            assertNotNull(json);
            assertTrue(json.contains("\"type\": \"BATCH\""));
            assertTrue(json.contains("\"name\": \"JsonTest\""));
            assertTrue(json.contains("\"children\": ["));
            assertTrue(json.contains("\"type\": \"SIMPLE\""));
            assertTrue(json.contains("\"name\": \"doc.txt\""));
            assertTrue(json.contains("\"type\": \"SEGMENTED\""));
            assertTrue(json.contains("\"name\": \"chunks.bin\""));
        }

        @Test
        @DisplayName("BandwidthAllocationVisitor distributes bandwidth pool proportionally based on element weights")
        void testBandwidthAllocationVisitor() {
            BatchDownloadElement batch = new BatchDownloadElement("QoSTest");
            SimpleDownloadElement simple = new SimpleDownloadElement("s.txt", "https://a.com/s", 100L, 0L, "text", "h");
            SegmentedDownloadElement seg = new SegmentedDownloadElement("seg.bin", "https://a.com/seg", 1000L, 0L, 4, 3); // 3 connections -> weight 6.0
            MirroredDownloadElement mirror = new MirroredDownloadElement("m.iso", "https://a.com/m", List.of("https://m.com/m"), 2000L, 0L); // weight 2.5

            batch.addElement(simple);
            batch.addElement(seg);
            batch.addElement(mirror);

            BandwidthAllocationVisitor visitor = new BandwidthAllocationVisitor(95_000_000L); // 95 MB/s
            batch.accept(visitor);
            var allocations = visitor.computeAllocations();

            assertEquals(3, allocations.size());
            // Segmented (weight 6.0) gets more than mirrored (2.5), which gets more than simple (1.0)
            assertTrue(allocations.get(seg.getId()) > allocations.get(mirror.getId()));
            assertTrue(allocations.get(mirror.getId()) > allocations.get(simple.getId()));

            long sum = allocations.values().stream().mapToLong(Long::longValue).sum();
            assertEquals(95_000_000L, sum);
        }

        @Test
        @DisplayName("DownloadElement getProgressPercentage clamps between 0.0 and 100.0")
        void testProgressPercentageClamping() {
            SimpleDownloadElement empty = new SimpleDownloadElement("0.bin", "https://a.com/0", 0L, 0L, "bin", "h");
            assertEquals(0.0, empty.getProgressPercentage());

            SimpleDownloadElement half = new SimpleDownloadElement("50.bin", "https://a.com/50", 200L, 100L, "bin", "h");
            assertEquals(50.0, half.getProgressPercentage());

            SimpleDownloadElement over = new SimpleDownloadElement("100.bin", "https://a.com/100", 200L, 300L, "bin", "h");
            assertEquals(100.0, over.getProgressPercentage());
        }
    }
}
