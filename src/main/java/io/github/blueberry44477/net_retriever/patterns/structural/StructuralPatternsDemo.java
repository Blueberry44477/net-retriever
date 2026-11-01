package io.github.blueberry44477.net_retriever.patterns.structural;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

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
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.LoggingMetricsDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.RateLimitingDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.decorator.RetryDownloaderDecorator;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.DownloadChunkContext;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.ProtocolPolicyFactory;
import io.github.blueberry44477.net_retriever.patterns.structural.flyweight.ProtocolPolicyFlyweight;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.AccessControlDownloaderProxy;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.CachingDownloaderProxy;
import io.github.blueberry44477.net_retriever.patterns.structural.proxy.LazyInitDownloaderProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Demonstration runner showcasing GoF Structural Design Patterns:
 * 1. Adapter Pattern (Object Adapter via Aggregation)
 * 2. Bridge Pattern (Orthogonal Abstraction and Implementation decoupled)
 * 3. Composite Pattern (Tree structure for uniform handling of individual files and batches)
 * 4. Flyweight Pattern (Efficient memory sharing across massive quantities of chunks)
 * 5. Decorator Pattern (Dynamic behavioral decoration: rate-limit, checksum, retry, metrics)
 * 6. Proxy Pattern (Access control, metadata caching, and virtual lazy loading)
 */
public class StructuralPatternsDemo {

    public static void main(String[] args) throws IOException {
        runAdapterDemo();
        runBridgeDemo();
        runCompositeDemo();
        runFlyweightDemo();
        runDecoratorDemo();
        runProxyDemo();
    }

    public static void runAdapterDemo() throws IOException {
        System.out.println("================================================================================");
        System.out.println(">>> 1. ADAPTER PATTERN DEMO (OBJECT ADAPTER VIA AGGREGATION) <<<");
        System.out.println("================================================================================");

        // Adaptee 1: Legacy FTP client
        LegacyFtpClient legacyFtp = new LegacyFtpClient();
        FileDownloader ftpAdapter = new LegacyFtpDownloaderAdapter(legacyFtp);

        URI ftpUri = URI.create("ftp://ftp.archive.org/releases/v2.1/archive.tar.gz");
        System.out.println("[Client] Querying FileDownloader adapter for FTP URI: " + ftpUri);
        System.out.println("[Client] Supports URI? -> " + ftpAdapter.supports(ftpUri));

        DownloadMetadata ftpMeta = ftpAdapter.fetchMetadata(ftpUri);
        System.out.println("[Client] Received Metadata: filename=" + ftpMeta.getSuggestedFileName()
                + ", size=" + ftpMeta.getTotalBytes() + ", resumable=" + ftpMeta.isResumable());

        System.out.println("[Client] Starting download via FileDownloader contract...");
        ftpAdapter.download(ftpUri, Path.of("/downloads/archive.tar.gz"), 0,
                downloaded -> System.out.println("[Client Callback] Progress: " + downloaded + " bytes"));
    }

    public static void runBridgeDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 2. BRIDGE PATTERN DEMO (DECOUPLED ABSTRACTION & IMPLEMENTATION) <<<");
        System.out.println("================================================================================");

        DownloadTransport httpTransport = new HttpDownloadTransport();
        DownloadTransport ftpTransport = new FtpDownloadTransport();
        DownloadTransport cloudTransport = new CloudStorageTransport();

        System.out.println("--- Scenario A: Simple Download Operation paired with HTTP Transport ---");
        DownloadOperation simpleHttp = new SimpleDownloadOperation(httpTransport);
        simpleHttp.execute("https://releases.ubuntu.com/26.04/ubuntu-desktop.iso", "/isos/ubuntu.iso");
        System.out.println();

        System.out.println("--- Scenario B: Resumable Download Operation paired with FTP Transport ---");
        DownloadOperation resumableFtp = new ResumableDownloadOperation(ftpTransport, 5_242_880L);
        resumableFtp.execute("ftp://mirror.kernel.org/linux-v6.tar.xz", "/sources/kernel.tar.xz");
        System.out.println();

        System.out.println("--- Scenario C: Split-Chunk Download Operation (4 parts) paired with Cloud Transport ---");
        DownloadOperation splitCloud = new SplitChunkDownloadOperation(cloudTransport, 4);
        splitCloud.execute("s3://datasets-bucket/ml/training-set-2026.bin", "/data/training.bin");
        System.out.println();

        System.out.println("--- Scenario D: Cross-Variation - Split-Chunk Operation dynamically used with HTTP Transport ---");
        DownloadOperation splitHttp = new SplitChunkDownloadOperation(httpTransport, 8);
        splitHttp.execute("https://fast-mirror.org/large-blob.dat", "/data/blob.dat");
        System.out.println();

        System.out.println("--- Scenario E: Operation Cancellation ---");
        DownloadOperation cancellableOp = new SimpleDownloadOperation(httpTransport);
        cancellableOp.cancel();

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runCompositeDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 3. COMPOSITE PATTERN DEMO (TREE STRUCTURE & UNIFORM PROCESSING) <<<");
        System.out.println("================================================================================");

        // Leaves: Single files and mirrored files
        DownloadItem jdkFile = new FileDownloadItem("openjdk-25_linux-x64_bin.tar.gz",
                "https://download.java.net/openjdk/25/bin.tar.gz", 200_000_000L, 100_000_000L);
        DownloadItem docsFile = new FileDownloadItem("openjdk-25-docs.zip",
                "https://download.java.net/openjdk/25/docs.zip", 50_000_000L, 50_000_000L);
        DownloadItem ideFile = new MirroredFileDownloadItem("ide-installer.tar.gz",
                "https://primary.mirror.org/ide.tar.gz",
                List.of("https://backup1.mirror.org/ide.tar.gz", "https://backup2.mirror.org/ide.tar.gz"),
                150_000_000L, 30_000_000L);
        DownloadItem licenseFile = new FileDownloadItem("LICENSE.txt",
                "https://example.com/license.txt", 15_000L, 15_000L);

        // Composite: Java SDK group
        DownloadGroup jdkGroup = new DownloadGroup("Java Development Kit 25 Suite");
        jdkGroup.add(jdkFile);
        jdkGroup.add(docsFile);

        // Root Composite: Developer Workstation Bundle
        PriorityDownloadGroup rootBundle = new PriorityDownloadGroup("Developer Workstation Bundle", 1);
        rootBundle.add(jdkGroup);
        rootBundle.add(ideFile);
        rootBundle.add(licenseFile);

        System.out.println("[Client] Displaying tree hierarchy:");
        rootBundle.printHierarchy("");
        System.out.println();

        System.out.println("[Client] Unified statistics via Component interface:");
        System.out.println("Total payload size   : " + rootBundle.getTotalBytes() + " bytes");
        System.out.println("Downloaded bytes     : " + rootBundle.getDownloadedBytes() + " bytes");
        System.out.printf("Combined progress    : %.2f%%%n", rootBundle.getProgressPercentage());
        System.out.println();

        System.out.println("[Client] Triggering start() on root bundle (propagates through entire tree):");
        rootBundle.start();
        System.out.println();

        System.out.println("[Client] Triggering pause() on root bundle:");
        rootBundle.pause();

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runFlyweightDemo() {
        System.out.println("================================================================================");
        System.out.println(">>> 4. FLYWEIGHT PATTERN DEMO (INTRINSIC SHARING & EXTRINSIC CONTEXT) <<<");
        System.out.println("================================================================================");

        ProtocolPolicyFactory factory = new ProtocolPolicyFactory();

        System.out.println("[Client] Requesting policies for 10,000 simulated chunks...");
        // 5,000 chunks for HTTPS CDN
        ProtocolPolicyFlyweight httpsPolicy1 = factory.getPolicy("https", "cdn1.mirror.org", 443, 65536, "BEARER-TOKEN");
        ProtocolPolicyFlyweight httpsPolicy2 = factory.getPolicy("https", "cdn1.mirror.org", 443, 65536, "BEARER-TOKEN");

        System.out.println("Are httpsPolicy1 and httpsPolicy2 the exact same instance? -> " + (httpsPolicy1 == httpsPolicy2));
        System.out.println("Identity hash code: " + System.identityHashCode(httpsPolicy1));

        // 5,000 chunks for S3 endpoint
        ProtocolPolicyFlyweight s3Policy = factory.getPolicy("s3", "storage.cloud.org", 443, 1048576, "AWS-SIGV4");

        List<DownloadChunkContext> chunks = new ArrayList<>();
        long chunkSize = 1_048_576L; // 1 MB
        for (int i = 0; i < 5_000; i++) {
            chunks.add(new DownloadChunkContext(i, i * chunkSize, (int) chunkSize, "/storage/large-dataset.tar", httpsPolicy1));
        }
        for (int i = 5_000; i < 10_000; i++) {
            chunks.add(new DownloadChunkContext(i, i * chunkSize, (int) chunkSize, "/storage/large-dataset.tar", s3Policy));
        }

        System.out.println("[Client] Total chunk contexts created : " + chunks.size());
        System.out.println("[Client] Total Flyweight instances in pool: " + factory.getFlyweightCount());
        System.out.println("[Client] Memory savings ratio: " + (chunks.size() / factory.getFlyweightCount()) + ":1");
        System.out.println();

        System.out.println("[Client] Executing transfer for sample chunks:");
        chunks.get(0).download();
        chunks.get(2500).download();
        chunks.get(7500).download();

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runDecoratorDemo() throws IOException {
        System.out.println("================================================================================");
        System.out.println(">>> 5. DECORATOR PATTERN DEMO (DYNAMIC BEHAVIOR COMPOSITION) <<<");
        System.out.println("================================================================================");

        FileDownloader baseDownloader = new BaseFileDownloader();
        URI ubuntuIso = URI.create("https://releases.ubuntu.com/noble/ubuntu-24.04-desktop-amd64.iso");

        System.out.println("[Client] Chaining decorators: Base -> Retry -> Checksum -> RateLimiting -> Metrics");
        FileDownloader fullyDecoratedDownloader = new LoggingMetricsDownloaderDecorator(
                new RateLimitingDownloaderDecorator(
                        new ChecksumVerifyingDownloaderDecorator(
                                new RetryDownloaderDecorator(baseDownloader, 3, 50L),
                                "a2e8c6799d511893c834a36d22efd8e7d23d8c1c4f52f360934e8f1ecf42f70c",
                                "SHA-256"
                        ),
                        20 * 1024 * 1024L // 20 MB/s rate limit
                )
        );

        System.out.println("[Client] Executing download through fully decorated pipeline:");
        fullyDecoratedDownloader.download(
                ubuntuIso,
                Path.of("/downloads/ubuntu-desktop.iso"),
                0L,
                bytes -> System.out.println("[Client Callback] Progress received: " + bytes + " bytes")
        );

        System.out.println("--------------------------------------------------------------------------------\n");
    }

    public static void runProxyDemo() throws IOException {
        System.out.println("================================================================================");
        System.out.println(">>> 6. PROXY PATTERN DEMO (CACHING, PROTECTION & VIRTUAL PROXIES) <<<");
        System.out.println("================================================================================");

        // Scenario 1: Protection Proxy with Domain Whitelist
        System.out.println("--- Sub-demo A: AccessControl (Protection) Proxy ---");
        Set<String> whitelist = Set.of("kernel.org", "ubuntu.com");
        FileDownloader protectionProxy = new AccessControlDownloaderProxy(new BaseFileDownloader(), whitelist, true);

        URI allowedUri = URI.create("https://cdn.kernel.org/pub/linux/kernel/v6.x/linux-6.10.tar.xz");
        System.out.println("[Client] Requesting allowed URI: " + allowedUri);
        protectionProxy.fetchMetadata(allowedUri);

        URI blockedUri = URI.create("https://untrusted-mirror.net/file.bin");
        System.out.println("[Client] Requesting unauthorized URI: " + blockedUri);
        try {
            protectionProxy.fetchMetadata(blockedUri);
        } catch (SecurityException ex) {
            System.out.println("[Client Caught Expected Error] " + ex.getMessage());
        }
        System.out.println();

        // Scenario 2: Caching Proxy for Metadata
        System.out.println("--- Sub-demo B: Caching Proxy (Redundant Request Suppression) ---");
        CachingDownloaderProxy cachingProxy = new CachingDownloaderProxy(new BaseFileDownloader());

        System.out.println("[Client] First metadata fetch (Cache MISS expected):");
        DownloadMetadata meta1 = cachingProxy.fetchMetadata(allowedUri);
        System.out.println("Result: filename=" + meta1.getSuggestedFileName() + ", size=" + meta1.getTotalBytes());

        System.out.println("[Client] Second metadata fetch for same URI (Cache HIT expected):");
        DownloadMetadata meta2 = cachingProxy.fetchMetadata(allowedUri);
        System.out.println("Result: filename=" + meta2.getSuggestedFileName() + ", size=" + meta2.getTotalBytes());
        System.out.printf("Cache stats: hits=%d, misses=%d%n%n", cachingProxy.getCacheHits(), cachingProxy.getCacheMisses());

        // Scenario 3: Virtual (Lazy Initialization) Proxy
        System.out.println("--- Sub-demo C: Lazy Initialization (Virtual) Proxy ---");
        LazyInitDownloaderProxy lazyProxy = new LazyInitDownloaderProxy(() -> {
            System.out.println("[Supplier] Instantiating expensive BaseFileDownloader network pool...");
            return new BaseFileDownloader();
        });

        System.out.println("Is RealSubject initialized before any calls? -> " + lazyProxy.isInitialized());
        System.out.println("[Client] Invoking first method on LazyProxy:");
        lazyProxy.fetchMetadata(allowedUri);
        System.out.println("Is RealSubject initialized after first call? -> " + lazyProxy.isInitialized());

        System.out.println("--------------------------------------------------------------------------------\n");
    }
}
