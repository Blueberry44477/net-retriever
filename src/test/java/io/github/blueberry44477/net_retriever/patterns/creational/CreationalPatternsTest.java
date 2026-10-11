package io.github.blueberry44477.net_retriever.patterns.creational;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.FtpComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.HttpComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.TorrentComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpEtagValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products.HttpSpeedMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJob;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJobBuilder;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJobDirector;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadPriority;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.DownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.FtpDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.HttpDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.TorrentDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.object_pool.DownloadBuffer;
import io.github.blueberry44477.net_retriever.patterns.creational.object_pool.DownloadBufferPool;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.BackgroundDownloadTaskPrototype;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.DownloadTaskPrototype;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.PriorityDownloadTaskPrototype;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.StandardDownloadTaskPrototype;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.TaskPrototypeRegistry;
import io.github.blueberry44477.net_retriever.patterns.creational.singleton.DownloadManagerConfig;

/**
 * Unit test suite verifying the behavior of all 6 GoF Creational Design Patterns.
 */
class CreationalPatternsTest {

    // =========================================================================
    // Lab 3: Singleton Pattern
    // =========================================================================
    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance() returns identical reference on subsequent calls")
        void shouldReturnSameInstance() {
            DownloadManagerConfig instance1 = DownloadManagerConfig.getInstance();
            DownloadManagerConfig instance2 = DownloadManagerConfig.getInstance();

            assertNotNull(instance1);
            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName("State updates persist across all reference lookups")
        void shouldPersistStateAcrossCalls() {
            DownloadManagerConfig config = DownloadManagerConfig.getInstance();
            config.setMaxConcurrentDownloads(12);
            config.setGlobalBandwidthLimitBytesPerSec(5000000L);

            DownloadManagerConfig sameConfig = DownloadManagerConfig.getInstance();
            assertEquals(12, sameConfig.getMaxConcurrentDownloads());
            assertEquals(5000000L, sameConfig.getGlobalBandwidthLimitBytesPerSec());
        }

        @Test
        @DisplayName("readResolve() maintains singleton identity during serialization")
        void shouldMaintainSingletonPropertyDuringSerialization() throws Exception {
            DownloadManagerConfig original = DownloadManagerConfig.getInstance();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            DownloadManagerConfig deserialized;
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                deserialized = (DownloadManagerConfig) ois.readObject();
            }

            assertSame(original, deserialized);
        }
    }

    // =========================================================================
    // Lab 3: Prototype Pattern
    // =========================================================================
    @Nested
    @DisplayName("Prototype Pattern Tests")
    class PrototypeTests {

        @Test
        @DisplayName("Cloning produces an independent instance with copied state")
        void shouldCreateIndependentClone() {
            StandardDownloadTaskPrototype original = new StandardDownloadTaskPrototype();
            StandardDownloadTaskPrototype clone = original.cloneTask();

            assertNotEquals(System.identityHashCode(original), System.identityHashCode(clone));
            assertEquals(original.getPriority(), clone.getPriority());
            assertEquals(original.getBufferSize(), clone.getBufferSize());

            clone.setCustomHeader("X-Clone-Header", "Value123");
            assertTrue(clone.getCustomHeaders().containsKey("X-Clone-Header"));
            assertFalse(original.getCustomHeaders().containsKey("X-Clone-Header"));
        }

        @Test
        @DisplayName("Registry provides independent clones of registered prototypes")
        void shouldRetrieveClonesFromRegistry() {
            TaskPrototypeRegistry registry = new TaskPrototypeRegistry();

            DownloadTaskPrototype p1 = registry.createFromPrototype("priority");
            DownloadTaskPrototype p2 = registry.createFromPrototype("priority");

            assertNotNull(p1);
            assertNotNull(p2);
            assertNotEquals(System.identityHashCode(p1), System.identityHashCode(p2));
            assertTrue(p1 instanceof PriorityDownloadTaskPrototype);

            DownloadTaskPrototype bg = registry.createFromPrototype("background");
            assertTrue(bg instanceof BackgroundDownloadTaskPrototype);
            assertEquals("LOW", bg.getPriority());
        }

        @Test
        @DisplayName("Registry throws IllegalArgumentException when key is unknown")
        void shouldThrowExceptionForUnknownPrototype() {
            TaskPrototypeRegistry registry = new TaskPrototypeRegistry();
            assertThrows(IllegalArgumentException.class, () -> registry.createFromPrototype("unknown_key"));
        }
    }

    // =========================================================================
    // Lab 4: Factory Method Pattern
    // =========================================================================
    @Nested
    @DisplayName("Factory Method Pattern Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("HttpDownloaderProvider executes download using HTTP downloader")
        void shouldCreateHttpDownloader() {
            DownloaderProvider provider = new HttpDownloaderProvider();
            provider.processDownload("https://example.com/test.bin", "/tmp/test.bin");
        }

        @Test
        @DisplayName("FtpDownloaderProvider executes download using FTP downloader")
        void shouldCreateFtpDownloader() {
            DownloaderProvider provider = new FtpDownloaderProvider();
            provider.processDownload("ftp://ftp.example.com/file.zip", "/tmp/file.zip");
        }

        @Test
        @DisplayName("TorrentDownloaderProvider executes download using Torrent downloader")
        void shouldCreateTorrentDownloader() {
            DownloaderProvider provider = new TorrentDownloaderProvider();
            provider.processDownload("magnet:?xt=urn:btih:1234567890abcdef", "/tmp/torrent.iso");
        }
    }

    // =========================================================================
    // Lab 4: Abstract Factory Pattern
    // =========================================================================
    @Nested
    @DisplayName("Abstract Factory Pattern Tests")
    class AbstractFactoryTests {

        @Test
        @DisplayName("HttpComponentFactory instantiates compatible HTTP components")
        void shouldCreateMatchingHttpFamily() {
            ProtocolComponentFactory factory = new HttpComponentFactory();
            ProtocolEngine engine = factory.createEngine();
            IntegrityValidator validator = factory.createValidator();
            TransferMonitor monitor = factory.createMonitor();

            assertTrue(engine instanceof HttpProtocolEngine);
            assertTrue(validator instanceof HttpEtagValidator);
            assertTrue(monitor instanceof HttpSpeedMonitor);
            assertEquals("HTTP/HTTPS Family", factory.getFamilyName());

            assertTrue(validator.validateIntegrity("/tmp/test", "etag-value-123"));
        }

        @Test
        @DisplayName("FTP and Torrent factories instantiate compatible components")
        void shouldCreateFtpAndTorrentFamilies() {
            ProtocolComponentFactory ftpFactory = new FtpComponentFactory();
            assertEquals("FTP Family", ftpFactory.getFamilyName());
            assertNotNull(ftpFactory.createEngine());
            assertNotNull(ftpFactory.createValidator());
            assertNotNull(ftpFactory.createMonitor());

            ProtocolComponentFactory torrentFactory = new TorrentComponentFactory();
            assertEquals("BitTorrent Family", torrentFactory.getFamilyName());
            assertNotNull(torrentFactory.createEngine());
            assertNotNull(torrentFactory.createValidator());
            assertNotNull(torrentFactory.createMonitor());
        }
    }

    // =========================================================================
    // Lab 5: Builder Pattern
    // =========================================================================
    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder constructs a valid immutable DownloadJob")
        void shouldBuildJobCorrectly() {
            DownloadJob job = new DownloadJobBuilder()
                    .setUrl("https://cdn.example.com/video.mp4")
                    .setDestinationPath("/media")
                    .setFileName("video.mp4")
                    .setPriority(DownloadPriority.HIGH)
                    .setSpeedLimitBytesPerSec(2048000L)
                    .setChunkCount(4)
                    .setMaxRetries(5)
                    .setTimeoutSeconds(60)
                    .setChecksum("sha256hashcode", "SHA-256")
                    .addHeader("Authorization", "Bearer jwt-token")
                    .addTag("video")
                    .addTag("streaming")
                    .build();

            assertNotNull(job.getId());
            assertEquals("https://cdn.example.com/video.mp4", job.getUrl());
            assertEquals("video.mp4", job.getFileName());
            assertEquals(DownloadPriority.HIGH, job.getPriority());
            assertEquals(4, job.getChunkCount());
            assertEquals("sha256hashcode", job.getExpectedChecksum());
            assertTrue(job.getTags().contains("video"));
            assertTrue(job.getCustomHeaders().containsKey("Authorization"));
        }

        @Test
        @DisplayName("Director builds standard configurations according to recipes")
        void shouldConstructJobsUsingDirector() {
            DownloadJobDirector director = new DownloadJobDirector();

            DownloadJob fastJob = director.constructFastMultiThreadJob(
                    new DownloadJobBuilder(),
                    "https://fast.mirror/archive.tar",
                    "/archives"
            );
            assertEquals(8, fastJob.getChunkCount());
            assertEquals(DownloadPriority.HIGH, fastJob.getPriority());

            DownloadJob bgJob = director.constructBackgroundJob(
                    new DownloadJobBuilder(),
                    "https://slow.mirror/bg.tar",
                    "/bg"
            );
            assertEquals(1, bgJob.getChunkCount());
            assertEquals(DownloadPriority.LOW, bgJob.getPriority());
        }

        @Test
        @DisplayName("Builder throws IllegalStateException when URL is missing")
        void shouldThrowExceptionWhenUrlMissing() {
            DownloadJobBuilder builder = new DownloadJobBuilder();
            assertThrows(IllegalStateException.class, builder::build);
        }
    }

    // =========================================================================
    // Lab 5: Object Pool Pattern
    // =========================================================================
    @Nested
    @DisplayName("Object Pool Pattern Tests")
    class ObjectPoolTests {

        @Test
        @DisplayName("Pool leases, tracks, and reuses memory buffers with state reset")
        void shouldAcquireAndReuseBuffers() {
            DownloadBufferPool pool = new DownloadBufferPool(2, 1024);

            DownloadBuffer b1 = pool.acquireBuffer();
            DownloadBuffer b2 = pool.acquireBuffer();

            assertEquals(2, pool.getInUseCount());
            assertEquals(0, pool.getAvailableCount());
            assertNotEquals(b1.getBufferId(), b2.getBufferId());

            byte[] data = new byte[]{1, 2, 3, 4};
            b1.writeData(data, 0, data.length);
            assertEquals(4, b1.getCurrentSize());

            pool.releaseBuffer(b1);
            assertEquals(1, pool.getInUseCount());
            assertEquals(1, pool.getAvailableCount());

            DownloadBuffer reused = pool.acquireBuffer();
            assertEquals(b1.getBufferId(), reused.getBufferId());
            assertEquals(0, reused.getCurrentSize());

            pool.releaseBuffer(b2);
            pool.releaseBuffer(reused);
            assertEquals(0, pool.getInUseCount());
            assertEquals(2, pool.getAvailableCount());
        }

        @Test
        @DisplayName("Pool throws IllegalStateException upon timeout when exhausted")
        void shouldTimeoutWhenPoolExhausted() {
            DownloadBufferPool pool = new DownloadBufferPool(1, 512);
            DownloadBuffer b = pool.acquireBuffer();

            assertThrows(IllegalStateException.class, () -> pool.acquireBuffer(50));

            pool.releaseBuffer(b);
        }
    }
}
