package io.github.blueberry44477.net_retriever.patterns.structural;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.blueberry44477.net_retriever.downloader.FileDownloader;
import io.github.blueberry44477.net_retriever.downloader.dto.DownloadMetadata;
import io.github.blueberry44477.net_retriever.patterns.structural.adapter.LegacyFtpClient;
import io.github.blueberry44477.net_retriever.patterns.structural.adapter.LegacyFtpDownloaderAdapter;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.CloudStorageTransport;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation.DownloadOperation;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.DownloadTransport;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.FtpDownloadTransport;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.HttpDownloadTransport;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation.ResumableDownloadOperation;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation.SimpleDownloadOperation;
import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation.SplitChunkDownloadOperation;
import io.github.blueberry44477.net_retriever.patterns.structural.composite.DownloadGroup;
import io.github.blueberry44477.net_retriever.patterns.structural.composite.DownloadItem;
import io.github.blueberry44477.net_retriever.patterns.structural.composite.FileDownloadItem;
import io.github.blueberry44477.net_retriever.patterns.structural.composite.MirroredFileDownloadItem;
import io.github.blueberry44477.net_retriever.patterns.structural.composite.PriorityDownloadGroup;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.BaseFileDownloader;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.ChecksumVerifyingDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.FileDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.LoggingMetricsDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.RateLimitingDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.RetryDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.ChunkStatus;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.DownloadChunkContext;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.ProtocolPolicyFactory;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.ProtocolPolicyFlyweight;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.AccessControlDownloaderProxy;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.CachingDownloaderProxy;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.LazyInitDownloaderProxy;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class StructuralPatternsTest {

    @Nested
    @DisplayName("Adapter Pattern Tests (Object Adapter via Aggregation)")
    class AdapterPatternTests {

        @Test
        @DisplayName("LegacyFtpDownloaderAdapter verifies scheme support")
        void testFtpAdapterSchemeSupport() {
            LegacyFtpClient ftpClient = new LegacyFtpClient();
            FileDownloader adapter = new LegacyFtpDownloaderAdapter(ftpClient);

            assertTrue(adapter.supports(URI.create("ftp://ftp.example.com/file.iso")));
            assertFalse(adapter.supports(URI.create("http://example.com/file.iso")));
            assertFalse(adapter.supports(URI.create("s3://bucket/file.iso")));
            assertFalse(adapter.supports(null));
        }

        @Test
        @DisplayName("LegacyFtpDownloaderAdapter fetches and translates metadata")
        void testFtpAdapterFetchMetadata() throws IOException {
            LegacyFtpClient ftpClient = new LegacyFtpClient();
            FileDownloader adapter = new LegacyFtpDownloaderAdapter(ftpClient);

            DownloadMetadata meta = adapter.fetchMetadata(URI.create("ftp://ftp.kernel.org/pub/linux.tar.xz"));
            assertNotNull(meta);
            assertEquals(15_728_640L, meta.getTotalBytes());
            assertTrue(meta.isResumable());
            assertEquals("linux.tar.xz", meta.getSuggestedFileName());
        }

        @Test
        @DisplayName("LegacyFtpDownloaderAdapter delegates download and triggers progress callback")
        void testFtpAdapterDownloadProgress() throws IOException {
            LegacyFtpClient ftpClient = new LegacyFtpClient();
            FileDownloader adapter = new LegacyFtpDownloaderAdapter(ftpClient);

            AtomicLong reportedProgress = new AtomicLong(0);
            adapter.download(
                    URI.create("ftp://ftp.kernel.org/pub/linux.tar.xz"),
                    Path.of("/tmp/linux.tar.xz"),
                    1024,
                    reportedProgress::set
            );

            assertTrue(reportedProgress.get() > 1024);
        }

        @Test
        @DisplayName("LegacyFtpDownloaderAdapter rejects unsupported scheme")
        void testFtpAdapterRejectsInvalidScheme() {
            LegacyFtpClient ftpClient = new LegacyFtpClient();
            FileDownloader adapter = new LegacyFtpDownloaderAdapter(ftpClient);

            assertThrows(IllegalArgumentException.class, () ->
                    adapter.fetchMetadata(URI.create("http://invalid.scheme/file.zip")));
            assertThrows(IllegalArgumentException.class, () ->
                    adapter.download(URI.create("http://invalid.scheme/file.zip"), Path.of("/tmp"), 0, null));
        }


        @Test
        @DisplayName("Object Adapter uses composition (aggregation) rather than inheritance")
        void testObjectAdapterCompositionSemantics() {
            LegacyFtpClient ftpClient = new LegacyFtpClient();
            LegacyFtpDownloaderAdapter adapter = new LegacyFtpDownloaderAdapter(ftpClient);

            assertSame(ftpClient, adapter.getAdaptee(), "Adapter must aggregate Adaptee instance");
            assertFalse(LegacyFtpClient.class.isAssignableFrom(LegacyFtpDownloaderAdapter.class),
                    "Adapter must NOT inherit from Adaptee class (Object Adapter requirement)");
        }
    }

    @Nested
    @DisplayName("Bridge Pattern Tests (Decoupled Abstraction & Implementation)")
    class BridgePatternTests {

        @Test
        @DisplayName("SimpleDownloadOperation executes sequential transfer via HttpDownloadTransport")
        void testSimpleHttpDownload() {
            HttpDownloadTransport transport = new HttpDownloadTransport();
            DownloadOperation op = new SimpleDownloadOperation(transport);

            op.execute("https://example.com/archive.zip", "/downloads/archive.zip");
            assertFalse(transport.isConnected(), "Transport should be disconnected after operation execution");
        }

        @Test
        @DisplayName("ResumableDownloadOperation resumes from specified offset")
        void testResumableDownloadOperation() {
            FtpDownloadTransport transport = new FtpDownloadTransport();
            ResumableDownloadOperation op = new ResumableDownloadOperation(transport, 1024L);

            assertEquals(1024L, op.getResumeOffset());
            op.execute("ftp://example.com/huge.bin", "/downloads/huge.bin");
            assertFalse(transport.isConnected());
        }

        @Test
        @DisplayName("ResumableDownloadOperation rejects negative resume offset")
        void testResumableDownloadRejectsNegativeOffset() {
            HttpDownloadTransport transport = new HttpDownloadTransport();
            assertThrows(IllegalArgumentException.class, () -> new ResumableDownloadOperation(transport, -1L));
        }

        @Test
        @DisplayName("ResumableDownloadOperation fails when transport does not support range requests")
        void testResumableDownloadFailsWithoutRangeSupport() {
            DownloadTransport noRangeTransport = new DownloadTransport() {
                @Override public void connect(String sourceUrl) {}
                @Override public long fetchContentLength(String sourceUrl) { return 1000L; }
                @Override public boolean supportsRangeRequests() { return false; }
                @Override public byte[] readChunk(long offset, int length) { return new byte[0]; }
                @Override public void disconnect() {}
                @Override public String getProtocolName() { return "NoRangeTransport"; }
            };

            ResumableDownloadOperation op = new ResumableDownloadOperation(noRangeTransport, 100L);
            assertThrows(UnsupportedOperationException.class, () ->
                    op.execute("dummy://source", "/target"));
        }

        @Test
        @DisplayName("SplitChunkDownloadOperation executes multi-chunk partitioning")
        void testSplitChunkDownloadOperation() {
            CloudStorageTransport transport = new CloudStorageTransport();
            SplitChunkDownloadOperation op = new SplitChunkDownloadOperation(transport, 4);

            assertEquals(4, op.getChunkCount());
            op.execute("s3://bucket/bigfile.bin", "/target/bigfile.bin");
            assertFalse(transport.isConnected());
        }

        @Test
        @DisplayName("SplitChunkDownloadOperation correctly partitions chunk offsets and lengths")
        void testSplitChunkOffsetCalculations() {
            List<Long> requestedOffsets = new ArrayList<>();
            List<Integer> requestedLengths = new ArrayList<>();

            DownloadTransport mockTransport = new DownloadTransport() {
                @Override public void connect(String sourceUrl) {}
                @Override public long fetchContentLength(String sourceUrl) { return 100L; }
                @Override public boolean supportsRangeRequests() { return true; }
                @Override
                public byte[] readChunk(long offset, int length) {
                    requestedOffsets.add(offset);
                    requestedLengths.add(length);
                    return new byte[length];
                }
                @Override public void disconnect() {}
                @Override public String getProtocolName() { return "Mock"; }
            };

            SplitChunkDownloadOperation op = new SplitChunkDownloadOperation(mockTransport, 3);
            op.execute("mock://test", "/target");

            // 100 bytes into 3 parts:
            // chunk 0: offset 0, length 33
            // chunk 1: offset 33, length 33
            // chunk 2: offset 66, length 34 (remainder)
            assertEquals(List.of(0L, 33L, 66L), requestedOffsets);
            assertEquals(List.of(33, 33, 34), requestedLengths);
        }

        @Test
        @DisplayName("SplitChunkDownloadOperation rejects non-positive chunk count")
        void testSplitChunkRejectsInvalidChunkCount() {
            HttpDownloadTransport transport = new HttpDownloadTransport();
            assertThrows(IllegalArgumentException.class, () -> new SplitChunkDownloadOperation(transport, 0));
            assertThrows(IllegalArgumentException.class, () -> new SplitChunkDownloadOperation(transport, -5));
        }

        @Test
        @DisplayName("Bridge enables independent variation of Abstractions and Implementors")
        void testIndependentVariation() {
            DownloadTransport[] transports = {
                    new HttpDownloadTransport(),
                    new FtpDownloadTransport(),
                    new CloudStorageTransport()
            };

            for (DownloadTransport transport : transports) {
                DownloadOperation op1 = new SimpleDownloadOperation(transport);
                DownloadOperation op2 = new ResumableDownloadOperation(transport, 500L);
                DownloadOperation op3 = new SplitChunkDownloadOperation(transport, 2);

                assertSame(transport, op1.getTransport());
                assertSame(transport, op2.getTransport());
                assertSame(transport, op3.getTransport());

                op1.execute("http://test/file", "/target");
                op2.execute("http://test/file", "/target");
                op3.execute("http://test/file", "/target");
            }
        }

        @Test
        @DisplayName("DownloadOperation cancel terminates connection via transport")
        void testCancelDisconnectsTransport() {
            HttpDownloadTransport transport = new HttpDownloadTransport();
            transport.connect("https://test.org");
            assertTrue(transport.isConnected());

            DownloadOperation op = new SimpleDownloadOperation(transport);
            op.cancel();
            assertFalse(transport.isConnected());
        }
    }

    @Nested
    @DisplayName("Composite Pattern Tests (Tree Structure & Uniform Operations)")
    class CompositePatternTests {

        @Test
        @DisplayName("FileDownloadItem leaf correctly reports state and computes progress")
        void testFileDownloadItemLeaf() {
            FileDownloadItem file = new FileDownloadItem("ubuntu.iso", "https://releases.ubuntu.com/ubuntu.iso", 1000L, 250L);

            assertEquals("ubuntu.iso", file.getName());
            assertEquals("https://releases.ubuntu.com/ubuntu.iso", file.getUrl());
            assertEquals(1000L, file.getTotalBytes());
            assertEquals(250L, file.getDownloadedBytes());
            assertEquals(25.0, file.getProgressPercentage(), 0.001);

            file.start();
            assertTrue(file.isActive());

            file.pause();
            assertFalse(file.isActive());

            file.simulateProgress(250L);
            assertEquals(500L, file.getDownloadedBytes());
            assertEquals(50.0, file.getProgressPercentage(), 0.001);

            file.cancel();
            assertFalse(file.isActive());
            assertEquals(0L, file.getDownloadedBytes());
        }

        @Test
        @DisplayName("Leaf nodes reject child management operations")
        void testLeafRejectsChildOperations() {
            FileDownloadItem leaf1 = new FileDownloadItem("file1.zip", "http://test/1.zip", 100L);
            FileDownloadItem leaf2 = new FileDownloadItem("file2.zip", "http://test/2.zip", 200L);

            assertTrue(leaf1.getChildren().isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> leaf1.add(leaf2));
            assertThrows(UnsupportedOperationException.class, () -> leaf1.remove(leaf2));
        }

        @Test
        @DisplayName("MirroredFileDownloadItem leaf handles failover mirror lists")
        void testMirroredFileDownloadItem() {
            List<String> mirrors = List.of("https://mirror1.com/file.bin", "https://mirror2.com/file.bin");
            MirroredFileDownloadItem item = new MirroredFileDownloadItem("file.bin", "https://primary.com/file.bin",
                    mirrors, 2000L, 1000L);

            assertEquals(2, item.getMirrorUrls().size());
            assertEquals(50.0, item.getProgressPercentage(), 0.001);

            item.start();
            assertTrue(item.isActive());
            item.pause();
            assertFalse(item.isActive());
        }

        @Test
        @DisplayName("DownloadGroup composite recursively aggregates sizes and calculates total progress")
        void testCompositeAggregation() {
            FileDownloadItem file1 = new FileDownloadItem("kernel.tar.xz", "http://test/kernel", 1000L, 500L);
            FileDownloadItem file2 = new FileDownloadItem("patch.diff", "http://test/patch", 500L, 250L);

            DownloadGroup subGroup = new DownloadGroup("Kernel Subsystem");
            subGroup.add(file1);
            subGroup.add(file2);

            FileDownloadItem rootFile = new FileDownloadItem("readme.txt", "http://test/readme", 500L, 500L);

            DownloadGroup rootGroup = new DownloadGroup("Root Project");
            rootGroup.add(subGroup);
            rootGroup.add(rootFile);

            assertEquals(2000L, rootGroup.getTotalBytes());
            assertEquals(1250L, rootGroup.getDownloadedBytes());
            // 1250 / 2000 * 100 = 62.5%
            assertEquals(62.5, rootGroup.getProgressPercentage(), 0.001);
        }

        @Test
        @DisplayName("DownloadGroup lifecycle methods (start, pause, cancel) propagate down the entire tree")
        void testCompositeLifecyclePropagation() {
            FileDownloadItem file1 = new FileDownloadItem("f1.bin", "http://test/f1", 100L, 50L);
            FileDownloadItem file2 = new FileDownloadItem("f2.bin", "http://test/f2", 100L, 50L);

            DownloadGroup group = new DownloadGroup("Batch");
            group.add(file1);
            group.add(file2);

            group.start();
            assertTrue(file1.isActive());
            assertTrue(file2.isActive());

            group.pause();
            assertFalse(file1.isActive());
            assertFalse(file2.isActive());

            group.cancel();
            assertEquals(0L, file1.getDownloadedBytes());
            assertEquals(0L, file2.getDownloadedBytes());
        }

        @Test
        @DisplayName("DownloadGroup correctly manages adding and removing children")
        void testCompositeAddRemove() {
            DownloadGroup group = new DownloadGroup("Group");
            FileDownloadItem item = new FileDownloadItem("doc.pdf", "http://test/doc.pdf", 500L);

            assertEquals(0, group.getChildren().size());
            group.add(item);
            assertEquals(1, group.getChildren().size());

            group.remove(item);
            assertEquals(0, group.getChildren().size());
        }

        @Test
        @DisplayName("PriorityDownloadGroup executes start based on priority sorting")
        void testPriorityDownloadGroup() {
            PriorityDownloadGroup pGroup = new PriorityDownloadGroup("Priority Queue", 1);
            FileDownloadItem fileLowProgress = new FileDownloadItem("f1.bin", "http://test/f1", 100L, 10L); // 10%
            FileDownloadItem fileHighProgress = new FileDownloadItem("f2.bin", "http://test/f2", 100L, 90L); // 90%

            pGroup.add(fileLowProgress);
            pGroup.add(fileHighProgress);

            assertEquals(1, pGroup.getPriorityLevel());
            pGroup.start();

            assertTrue(fileLowProgress.isActive());
            assertTrue(fileHighProgress.isActive());
        }
    }

    @Nested
    @DisplayName("Flyweight Pattern Tests (Intrinsic Sharing & Extrinsic Context)")
    class FlyweightPatternTests {

        @Test
        @DisplayName("ProtocolPolicyFactory reuses existing flyweight instance for matching parameters")
        void testFlyweightFactoryReusesInstances() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();

            ProtocolPolicyFlyweight policy1 = factory.getPolicy("https", "cdn.example.org", 443, 65536, "BEARER");
            ProtocolPolicyFlyweight policy2 = factory.getPolicy("https", "cdn.example.org", 443, 65536, "BEARER");

            assertSame(policy1, policy2, "Factory must return the exact same instance for identical intrinsic state");
            assertEquals(1, factory.getFlyweightCount());
        }

        @Test
        @DisplayName("ProtocolPolicyFactory creates distinct instances for different intrinsic states")
        void testFlyweightFactoryDistinctInstances() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();

            ProtocolPolicyFlyweight httpPolicy = factory.getPolicy("http", "mirror.org", 80, 32768, "BASIC");
            ProtocolPolicyFlyweight httpsPolicy = factory.getPolicy("https", "mirror.org", 443, 32768, "BASIC");
            ProtocolPolicyFlyweight ftpPolicy = factory.getPolicy("ftp", "mirror.org", 21, 65536, null);

            assertNotSame(httpPolicy, httpsPolicy);
            assertNotSame(httpsPolicy, ftpPolicy);
            assertEquals(3, factory.getFlyweightCount());
        }

        @Test
        @DisplayName("SharedProtocolPolicy correctly reports intrinsic properties and policy key")
        void testSharedProtocolPolicyProperties() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();
            ProtocolPolicyFlyweight policy = factory.getPolicy("s3", "storage.amazon.com", 443, 1048576, "AWS-SIGV4");

            assertEquals("s3", policy.getProtocol());
            assertEquals("storage.amazon.com", policy.getHost());
            assertEquals(443, policy.getPort());
            assertEquals(1048576, policy.getBufferSize());
            assertEquals("AWS-SIGV4", policy.getAuthScheme());
            assertEquals("s3://storage.amazon.com:443:1048576:aws-sigv4", policy.getPolicyKey());
        }

        @Test
        @DisplayName("DownloadChunkContext executes transfer and manages chunk lifecycle status")
        void testDownloadChunkContext() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();
            ProtocolPolicyFlyweight policy = factory.getPolicy("https", "fast.cdn.com", 443, 65536, "BEARER");

            DownloadChunkContext chunk = new DownloadChunkContext(42L, 1024L, 512, "/tmp/part42.bin", policy);

            assertEquals(42L, chunk.getChunkId());
            assertEquals(1024L, chunk.getByteOffset());
            assertEquals(512, chunk.getLength());
            assertEquals("/tmp/part42.bin", chunk.getDestination());
            assertSame(policy, chunk.getPolicy());
            assertEquals(ChunkStatus.PENDING, chunk.getStatus());

            chunk.download();
            assertEquals(ChunkStatus.COMPLETED, chunk.getStatus());
        }

        @Test
        @DisplayName("Massive chunk context generation shares minimal flyweight instances")
        void testMassiveChunkContextSharing() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();
            ProtocolPolicyFlyweight policyA = factory.getPolicy("https", "cdnA.org", 443, 65536, "BEARER");
            ProtocolPolicyFlyweight policyB = factory.getPolicy("ftp", "mirrorB.org", 21, 32768, "ANONYMOUS");

            List<DownloadChunkContext> contexts = new ArrayList<>();
            for (int i = 0; i < 5_000; i++) {
                contexts.add(new DownloadChunkContext(i, i * 1024L, 1024, "/file.iso", policyA));
            }
            for (int i = 5_000; i < 10_000; i++) {
                contexts.add(new DownloadChunkContext(i, i * 1024L, 1024, "/file.iso", policyB));
            }

            assertEquals(10_000, contexts.size());
            assertEquals(2, factory.getFlyweightCount(), "All 10,000 contexts must share only 2 Flyweight objects");

            // Verify instance references
            assertSame(policyA, contexts.get(0).getPolicy());
            assertSame(policyA, contexts.get(4_999).getPolicy());
            assertSame(policyB, contexts.get(5_000).getPolicy());
            assertSame(policyB, contexts.get(9_999).getPolicy());
        }

        @Test
        @DisplayName("ProtocolPolicyFactory clearPool removes cached flyweights")
        void testClearPool() {
            ProtocolPolicyFactory factory = new ProtocolPolicyFactory();
            factory.getPolicy("http", "host1", 80, 1024, null);
            factory.getPolicy("http", "host2", 80, 1024, null);

            assertEquals(2, factory.getFlyweightCount());
            factory.clearPool();
            assertEquals(0, factory.getFlyweightCount());
        }
    }

    @Nested
    @DisplayName("Decorator Pattern Tests")
    class DecoratorPatternTests {

        @Test
        @DisplayName("BaseFileDownloader performs baseline fetchMetadata and download")
        void testBaseFileDownloaderOperations() throws IOException {
            BaseFileDownloader downloader = new BaseFileDownloader();
            URI source = URI.create("https://downloads.example.org/files/archive.tar.gz");

            DownloadMetadata metadata = downloader.fetchMetadata(source);
            assertNotNull(metadata);
            assertEquals(25_000_000L, metadata.getTotalBytes());
            assertTrue(metadata.isResumable());
            assertEquals("archive.tar.gz", metadata.getSuggestedFileName());

            AtomicLong progressReported = new AtomicLong(-1);
            downloader.download(source, Path.of("/tmp/archive.tar.gz"), 1000L, progressReported::set);
            assertEquals(2024L, progressReported.get());

            assertTrue(downloader.supports(URI.create("https://downloads.example.org/file.bin")));
            assertTrue(downloader.supports(URI.create("http://downloads.example.org/file.bin")));
            assertTrue(downloader.supports(URI.create("ftp://ftp.example.org/file.bin")));
            assertTrue(downloader.supports(URI.create("s3://bucket/file.bin")));
            assertFalse(downloader.supports(URI.create("mailto:user@example.org")));
            assertFalse(downloader.supports(null));
        }

        @Test
        @DisplayName("RateLimitingDownloaderDecorator enforces positive bandwidth limit")
        void testRateLimitingDownloaderDecoratorEnforcesLimit() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            RateLimitingDownloaderDecorator decorator = new RateLimitingDownloaderDecorator(base, 500_000L);

            assertEquals(500_000L, decorator.getMaxBytesPerSecond());

            AtomicLong progressReported = new AtomicLong(-1);
            URI source = URI.create("https://example.org/bundle.zip");
            decorator.download(source, Path.of("/tmp/bundle.zip"), 0L, progressReported::set);
            assertEquals(1024L, progressReported.get());

            assertThrows(IllegalArgumentException.class, () -> new RateLimitingDownloaderDecorator(base, 0));
            assertThrows(IllegalArgumentException.class, () -> new RateLimitingDownloaderDecorator(base, -100));
        }

        @Test
        @DisplayName("ChecksumVerifyingDownloaderDecorator performs hash verification")
        void testChecksumVerifyingDownloaderDecoratorIntegrityCheck() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            String sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
            ChecksumVerifyingDownloaderDecorator decorator =
                    new ChecksumVerifyingDownloaderDecorator(base, sha256, "sha-256");

            assertEquals(sha256, decorator.getExpectedChecksum());
            assertEquals("SHA-256", decorator.getAlgorithm());
            assertFalse(decorator.isVerified());

            URI source = URI.create("https://example.org/binary.iso");
            decorator.download(source, Path.of("/tmp/binary.iso"), 0L, null);
            assertTrue(decorator.isVerified());

            assertThrows(NullPointerException.class, () -> new ChecksumVerifyingDownloaderDecorator(base, null, "MD5"));
        }

        @Test
        @DisplayName("RetryDownloaderDecorator succeeds after transient failure")
        void testRetryDownloaderDecoratorSucceedsOnEventualAttempt() throws IOException {
            AtomicInteger attempts = new AtomicInteger(0);
            FileDownloader transientDownloader = new FileDownloader() {
                @Override
                public DownloadMetadata fetchMetadata(URI source) {
                    return new DownloadMetadata(100L, true, "f.bin");
                }

                @Override
                public void download(URI source, Path destination, long startByte, io.github.blueberry44477.net_retriever.downloader.ProgressListener progressListener) throws IOException {
                    if (attempts.incrementAndGet() < 2) {
                        throw new IOException("Temporary connection reset");
                    }
                }

                @Override
                public boolean supports(URI source) {
                    return true;
                }
            };

            RetryDownloaderDecorator retryDecorator = new RetryDownloaderDecorator(transientDownloader, 3, 5L);
            retryDecorator.download(URI.create("https://example.org/transient.bin"), Path.of("/tmp/transient.bin"), 0L, null);

            assertEquals(2, retryDecorator.getAttemptCount());
            assertEquals(3, retryDecorator.getMaxRetries());
            assertEquals(5L, retryDecorator.getBackoffDelayMs());
        }

        @Test
        @DisplayName("RetryDownloaderDecorator throws IOException when retries are exhausted")
        void testRetryDownloaderDecoratorExhaustsAttempts() {
            FileDownloader alwaysFailingDownloader = new FileDownloader() {
                @Override
                public DownloadMetadata fetchMetadata(URI source) {
                    return new DownloadMetadata(100L, true, "f.bin");
                }

                @Override
                public void download(URI source, Path destination, long startByte, io.github.blueberry44477.net_retriever.downloader.ProgressListener progressListener) throws IOException {
                    throw new IOException("Remote server unreachable");
                }

                @Override
                public boolean supports(URI source) {
                    return true;
                }
            };

            RetryDownloaderDecorator retryDecorator = new RetryDownloaderDecorator(alwaysFailingDownloader, 3, 5L);
            IOException thrown = assertThrows(IOException.class, () ->
                    retryDecorator.download(URI.create("https://example.org/fail.bin"), Path.of("/tmp/fail.bin"), 0L, null)
            );

            assertEquals("Remote server unreachable", thrown.getMessage());
            assertEquals(3, retryDecorator.getAttemptCount());

            assertThrows(IllegalArgumentException.class, () -> new RetryDownloaderDecorator(alwaysFailingDownloader, 0));
        }

        @Test
        @DisplayName("LoggingMetricsDownloaderDecorator records telemetry timings")
        void testLoggingMetricsDownloaderDecoratorRecordsDurations() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            LoggingMetricsDownloaderDecorator decorator = new LoggingMetricsDownloaderDecorator(base);

            assertEquals(0L, decorator.getLastMetadataDurationMs());
            assertEquals(0L, decorator.getLastDownloadDurationMs());

            URI source = URI.create("https://example.org/metrics.iso");
            DownloadMetadata meta = decorator.fetchMetadata(source);
            assertNotNull(meta);
            assertTrue(decorator.getLastMetadataDurationMs() >= 0L);

            decorator.download(source, Path.of("/tmp/metrics.iso"), 0L, null);
            assertTrue(decorator.getLastDownloadDurationMs() >= 0L);
        }

        @Test
        @DisplayName("Chained decorators execute in layered sequence")
        void testChainedDecoratorsExecuteInLayers() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            RetryDownloaderDecorator retry = new RetryDownloaderDecorator(base, 2, 5L);
            ChecksumVerifyingDownloaderDecorator checksum =
                    new ChecksumVerifyingDownloaderDecorator(retry, "abc123hash", "SHA-256");
            RateLimitingDownloaderDecorator rateLimiter =
                    new RateLimitingDownloaderDecorator(checksum, 1_048_576L);
            LoggingMetricsDownloaderDecorator metrics =
                    new LoggingMetricsDownloaderDecorator(rateLimiter);

            URI source = URI.create("https://cdn.example.org/firmware.bin");
            DownloadMetadata meta = metrics.fetchMetadata(source);
            assertNotNull(meta);

            AtomicLong progress = new AtomicLong(-1);
            metrics.download(source, Path.of("/tmp/firmware.bin"), 0L, progress::set);

            assertEquals(1024L, progress.get());
            assertTrue(checksum.isVerified());
            assertEquals(1, retry.getAttemptCount());
            assertEquals(1_048_576L, rateLimiter.getMaxBytesPerSecond());
            assertTrue(metrics.getLastDownloadDurationMs() >= 0L);
            assertTrue(metrics.supports(source));
        }
    }

    @Nested
    @DisplayName("Proxy Pattern Tests")
    class ProxyPatternTests {

        @Test
        @DisplayName("CachingDownloaderProxy avoids repeated fetchMetadata remote calls")
        void testCachingDownloaderProxyCachesMetadata() throws IOException {
            AtomicInteger remoteCallCounter = new AtomicInteger(0);
            FileDownloader realDownloader = new FileDownloader() {
                @Override
                public DownloadMetadata fetchMetadata(URI source) {
                    remoteCallCounter.incrementAndGet();
                    return new DownloadMetadata(10_000_000L, true, "cached-test.pkg");
                }

                @Override
                public void download(URI source, Path destination, long startByte, io.github.blueberry44477.net_retriever.downloader.ProgressListener progressListener) {
                }

                @Override
                public boolean supports(URI source) {
                    return true;
                }
            };

            CachingDownloaderProxy proxy = new CachingDownloaderProxy(realDownloader);
            URI uri1 = URI.create("https://example.org/archive1.zip");
            URI uri2 = URI.create("https://example.org/archive2.zip");

            assertEquals(0, proxy.getCacheHits());
            assertEquals(0, proxy.getCacheMisses());

            // First call for URI 1 -> Cache Miss
            DownloadMetadata meta1 = proxy.fetchMetadata(uri1);
            assertNotNull(meta1);
            assertEquals(1, remoteCallCounter.get());
            assertEquals(0, proxy.getCacheHits());
            assertEquals(1, proxy.getCacheMisses());

            // Second call for URI 1 -> Cache Hit
            DownloadMetadata meta2 = proxy.fetchMetadata(uri1);
            assertSame(meta1, meta2);
            assertEquals(1, remoteCallCounter.get());
            assertEquals(1, proxy.getCacheHits());
            assertEquals(1, proxy.getCacheMisses());

            // First call for URI 2 -> Cache Miss
            DownloadMetadata meta3 = proxy.fetchMetadata(uri2);
            assertNotNull(meta3);
            assertEquals(2, remoteCallCounter.get());
            assertEquals(1, proxy.getCacheHits());
            assertEquals(2, proxy.getCacheMisses());

            // Third call for URI 1 -> Cache Hit
            DownloadMetadata meta4 = proxy.fetchMetadata(uri1);
            assertSame(meta1, meta4);
            assertEquals(2, remoteCallCounter.get());
            assertEquals(2, proxy.getCacheHits());
            assertEquals(2, proxy.getCacheMisses());

            assertTrue(proxy.supports(uri1));
        }

        @Test
        @DisplayName("CachingDownloaderProxy clearCache resets cache and counters")
        void testCachingDownloaderProxyClearCache() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            CachingDownloaderProxy proxy = new CachingDownloaderProxy(base);
            URI source = URI.create("https://example.org/data.tar");

            proxy.fetchMetadata(source);
            assertEquals(1, proxy.getCacheMisses());

            proxy.clearCache();
            assertEquals(0, proxy.getCacheHits());
            assertEquals(0, proxy.getCacheMisses());
            assertTrue(proxy.getCache().isEmpty());

            proxy.fetchMetadata(source);
            assertEquals(1, proxy.getCacheMisses());
        }

        @Test
        @DisplayName("AccessControlDownloaderProxy allows whitelisted domains and HTTPS")
        void testAccessControlDownloaderProxyAllowsWhitelistedDomains() throws IOException {
            BaseFileDownloader base = new BaseFileDownloader();
            Set<String> allowed = Set.of("secure-corp.org", "internal.io");
            AccessControlDownloaderProxy proxy = new AccessControlDownloaderProxy(base, allowed, true);

            URI validUri1 = URI.create("https://secure-corp.org/package.zip");
            URI validUri2 = URI.create("https://sub.secure-corp.org/patch.zip");
            URI validS3 = URI.create("s3://internal.io/blob.data");

            assertTrue(proxy.isAuthorized(validUri1));
            assertTrue(proxy.isAuthorized(validUri2));
            assertTrue(proxy.isAuthorized(validS3));
            assertTrue(proxy.supports(validUri1));

            DownloadMetadata meta = proxy.fetchMetadata(validUri1);
            assertNotNull(meta);

            AtomicLong progress = new AtomicLong(-1);
            proxy.download(validUri1, Path.of("/tmp/test.zip"), 0L, progress::set);
            assertEquals(1024L, progress.get());
        }

        @Test
        @DisplayName("AccessControlDownloaderProxy rejects unauthorized domain with SecurityException")
        void testAccessControlDownloaderProxyRejectsUnauthorizedDomain() {
            BaseFileDownloader base = new BaseFileDownloader();
            Set<String> allowed = Set.of("secure-corp.org");
            AccessControlDownloaderProxy proxy = new AccessControlDownloaderProxy(base, allowed, true);

            URI unauthorizedUri = URI.create("https://malicious.evil.com/payload.exe");

            assertFalse(proxy.isAuthorized(unauthorizedUri));
            assertFalse(proxy.supports(unauthorizedUri));

            assertThrows(SecurityException.class, () -> proxy.fetchMetadata(unauthorizedUri));
            assertThrows(SecurityException.class, () ->
                    proxy.download(unauthorizedUri, Path.of("/tmp/payload.exe"), 0L, null)
            );
        }

        @Test
        @DisplayName("AccessControlDownloaderProxy rejects insecure HTTP when HTTPS required")
        void testAccessControlDownloaderProxyEnforcesHttps() {
            BaseFileDownloader base = new BaseFileDownloader();
            Set<String> allowed = Set.of("secure-corp.org");
            AccessControlDownloaderProxy proxy = new AccessControlDownloaderProxy(base, allowed, true);

            URI insecureUri = URI.create("http://secure-corp.org/insecure.txt");

            assertFalse(proxy.isAuthorized(insecureUri));
            assertThrows(SecurityException.class, () -> proxy.fetchMetadata(insecureUri));
        }

        @Test
        @DisplayName("AccessControlDownloaderProxy handles null and invalid host URIs gracefully")
        void testAccessControlDownloaderProxyInvalidUris() {
            BaseFileDownloader base = new BaseFileDownloader();
            AccessControlDownloaderProxy proxy = new AccessControlDownloaderProxy(base, Set.of("example.org"), false);

            assertFalse(proxy.isAuthorized(null));
            assertFalse(proxy.isAuthorized(URI.create("opaque:justastring")));
            assertFalse(proxy.supports(null));
        }

        @Test
        @DisplayName("LazyInitDownloaderProxy defers RealSubject instantiation until invoked")
        void testLazyInitDownloaderProxyInitializesOnFirstCall() throws IOException {
            AtomicInteger supplierCounter = new AtomicInteger(0);
            LazyInitDownloaderProxy proxy = new LazyInitDownloaderProxy(() -> {
                supplierCounter.incrementAndGet();
                return new BaseFileDownloader();
            });

            assertFalse(proxy.isInitialized());
            assertEquals(0, supplierCounter.get());

            URI source = URI.create("https://example.org/lazy.bin");

            // supports() triggers initialization
            boolean supported = proxy.supports(source);
            assertTrue(supported);
            assertTrue(proxy.isInitialized());
            assertEquals(1, supplierCounter.get());

            // Subsequent fetchMetadata uses already initialized RealSubject
            DownloadMetadata metadata = proxy.fetchMetadata(source);
            assertNotNull(metadata);
            assertEquals(1, supplierCounter.get(), "Supplier must only be invoked once");

            // Subsequent download also uses initialized RealSubject
            proxy.download(source, Path.of("/tmp/lazy.bin"), 0L, null);
            assertEquals(1, supplierCounter.get());
        }
    }
}
