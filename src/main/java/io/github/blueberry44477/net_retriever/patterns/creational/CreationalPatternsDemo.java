package io.github.blueberry44477.net_retriever.patterns.creational;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.IntegrityValidator;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.TransferMonitor;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.FtpComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.HttpComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.component_factory.TorrentComponentFactory;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJob;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJobBuilder;
import io.github.blueberry44477.net_retriever.patterns.creational.builder.DownloadJobDirector;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.DownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.FtpDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.HttpDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.factory_method.TorrentDownloaderProvider;
import io.github.blueberry44477.net_retriever.patterns.creational.object_pool.DownloadBuffer;
import io.github.blueberry44477.net_retriever.patterns.creational.object_pool.DownloadBufferPool;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.DownloadTaskPrototype;
import io.github.blueberry44477.net_retriever.patterns.creational.prototype.TaskPrototypeRegistry;
import io.github.blueberry44477.net_retriever.patterns.creational.singleton.DownloadManagerConfig;

/**
 * Demonstration runner showcasing all 6 GoF Creational Design Patterns in action.
 */
public class CreationalPatternsDemo {

    public static void main(String[] args) {
        runSingletonDemo();
        runPrototypeDemo();
        runFactoryMethodDemo();
        runAbstractFactoryDemo();
        runBuilderDemo();
        runObjectPoolDemo();
    }

    public static void runSingletonDemo() {
        System.out.println(">>> 1. SINGLETON PATTERN DEMO <<<");
        DownloadManagerConfig config1 = DownloadManagerConfig.getInstance();
        DownloadManagerConfig config2 = DownloadManagerConfig.getInstance();

        System.out.println("config1 identity hash: " + System.identityHashCode(config1));
        System.out.println("config2 identity hash: " + System.identityHashCode(config2));
        System.out.println("Same instance reference? -> " + (config1 == config2));

        config1.setMaxConcurrentDownloads(8);
        config1.setGlobalBandwidthLimitBytesPerSec(20 * 1024 * 1024L);
        System.out.println("Values checked through config2 after modifying config1: maxDownloads="
                + config2.getMaxConcurrentDownloads() + ", speedLimit=" + config2.getGlobalBandwidthLimitBytesPerSec());
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runPrototypeDemo() {
        System.out.println(">>> 2. PROTOTYPE PATTERN DEMO <<<");
        TaskPrototypeRegistry registry = new TaskPrototypeRegistry();

        DownloadTaskPrototype standardClone = registry.createFromPrototype("standard");
        standardClone.setCustomHeader("Authorization", "Bearer sample-token-123");
        standardClone.executeDownload("https://example.com/file1.zip", "/downloads/file1.zip");

        DownloadTaskPrototype priorityClone = registry.createFromPrototype("priority");
        priorityClone.executeDownload("https://example.com/kernel-update.bin", "/system/kernel.bin");

        DownloadTaskPrototype backgroundClone = registry.createFromPrototype("background");
        backgroundClone.executeDownload("https://example.com/backup-dump.sql", "/backups/db.sql");

        System.out.println("Verifying deep-copy isolation between clones:");
        DownloadTaskPrototype priorityClone2 = registry.createFromPrototype("priority");
        priorityClone2.setPriority("CUSTOM-HIGH");
        System.out.println("priorityClone priority: " + priorityClone.getPriority());
        System.out.println("priorityClone2 priority: " + priorityClone2.getPriority());
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runFactoryMethodDemo() {
        System.out.println(">>> 3. FACTORY METHOD PATTERN DEMO <<<");
        DownloaderProvider httpProvider = new HttpDownloaderProvider();
        httpProvider.processDownload("https://releases.ubuntu.com/noble/ubuntu.iso", "/isos/ubuntu.iso");

        DownloaderProvider ftpProvider = new FtpDownloaderProvider();
        ftpProvider.processDownload("ftp://archive.kernel.org/pub/linux.tar.xz", "/sources/linux.tar.xz");

        DownloaderProvider torrentProvider = new TorrentDownloaderProvider();
        torrentProvider.processDownload("magnet:?xt=urn:btih:da39a3ee5e6b4b0d3255bfef95601890afd80709", "/p2p/arch.iso");
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runAbstractFactoryDemo() {
        System.out.println(">>> 4. ABSTRACT FACTORY PATTERN DEMO <<<");
        ProtocolComponentFactory[] factories = {
                new HttpComponentFactory(),
                new FtpComponentFactory(),
                new TorrentComponentFactory()
        };

        for (ProtocolComponentFactory factory : factories) {
            System.out.println("--- Testing Family: " + factory.getFamilyName() + " ---");
            ProtocolEngine engine = factory.createEngine();
            IntegrityValidator validator = factory.createValidator();
            TransferMonitor monitor = factory.createMonitor();

            engine.connect("resource://demo-endpoint");
            engine.transferData("/tmp/data", 1024 * 1024 * 10);
            monitor.recordProgress(1024 * 1024 * 10, 1024 * 1024 * 10);
            validator.validateIntegrity("/tmp/data", "EXPECTED_HASH_VAL");
            monitor.logCompletionSummary();
            engine.disconnect();
            System.out.println();
        }
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runBuilderDemo() {
        System.out.println(">>> 5. BUILDER PATTERN DEMO <<<");
        DownloadJobDirector director = new DownloadJobDirector();

        DownloadJob fastJob = director.constructFastMultiThreadJob(
                new DownloadJobBuilder(),
                "https://mirror.archlinux.org/archlinux-x86_64.iso",
                "/isos"
        );
        fastJob.start();

        DownloadJob secureJob = director.constructSecureVerifiedJob(
                new DownloadJobBuilder(),
                "https://cdn.openbsd.org/base76.tgz",
                "/bsd",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        );
        secureJob.start();

        DownloadJob customJob = new DownloadJobBuilder()
                .setUrl("https://data.gov/archive.csv")
                .setDestinationPath("/data")
                .setFileName("analytics.csv")
                .setSpeedLimitBytesPerSec(1024 * 1024)
                .addHeader("X-ApiKey", "secret123")
                .addTag("data-analysis")
                .build();
        customJob.start();
        System.out.println("Constructed Custom Job: " + customJob);
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runObjectPoolDemo() {
        System.out.println(">>> 6. OBJECT POOL PATTERN DEMO <<<");
        DownloadBufferPool pool = new DownloadBufferPool(3, 65536);

        DownloadBuffer buf1 = pool.acquireBuffer();
        DownloadBuffer buf2 = pool.acquireBuffer();

        byte[] sampleData = new byte[]{10, 20, 30, 40, 50};
        buf1.writeData(sampleData, 0, sampleData.length);
        buf2.writeData(sampleData, 0, sampleData.length);

        System.out.println("Pool state: inUse=" + pool.getInUseCount() + ", available=" + pool.getAvailableCount());

        pool.releaseBuffer(buf1);
        System.out.println("After releasing buf1: inUse=" + pool.getInUseCount() + ", available=" + pool.getAvailableCount());

        DownloadBuffer reusedBuf = pool.acquireBuffer();
        System.out.println("Reacquired buffer #" + reusedBuf.getBufferId()
                + " (size after reset: " + reusedBuf.getCurrentSize() + ")");

        pool.releaseBuffer(buf2);
        pool.releaseBuffer(reusedBuf);
        System.out.println("Final pool state: inUse=" + pool.getInUseCount() + ", available=" + pool.getAvailableCount());
        System.out.println("--------------------------------------------------------------------------------\n");
    }
}
