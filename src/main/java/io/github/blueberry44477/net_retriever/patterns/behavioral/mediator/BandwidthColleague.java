package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import lombok.Getter;

/**
 * Concrete Colleague managing network bandwidth allocation and QoS throttling.
 */
@Getter
public class BandwidthColleague extends DownloadColleague {

    private final long maxBandwidthBytesPerSec;
    private long allocatedBandwidthBytesPerSec;
    private boolean bandwidthCongested = false;

    public BandwidthColleague(long maxBandwidthBytesPerSec) {
        this.maxBandwidthBytesPerSec = maxBandwidthBytesPerSec;
        this.allocatedBandwidthBytesPerSec = 0L;
    }

    public BandwidthColleague(DownloadSessionMediator mediator, long maxBandwidthBytesPerSec) {
        super(mediator);
        this.maxBandwidthBytesPerSec = maxBandwidthBytesPerSec;
        this.allocatedBandwidthBytesPerSec = 0L;
    }

    @Override
    public String getName() {
        return "BandwidthManager";
    }

    /**
     * Requests bandwidth quota allocation for a download stream.
     *
     * @param bytesPerSec desired transfer rate in B/s
     * @return true if allocated within budget; false if exceeded
     */
    public boolean allocateBandwidth(long bytesPerSec) {
        System.out.printf("[BandwidthColleague] [Stub] allocateBandwidth(%d B/s) - Currently allocated: %d / %d B/s%n",
                bytesPerSec, allocatedBandwidthBytesPerSec, maxBandwidthBytesPerSec);
        if (allocatedBandwidthBytesPerSec + bytesPerSec > maxBandwidthBytesPerSec) {
            this.bandwidthCongested = true;
            System.out.printf("[BandwidthColleague] [Stub] Bandwidth ceiling exceeded! Request: %d, Available: %d%n",
                    bytesPerSec, maxBandwidthBytesPerSec - allocatedBandwidthBytesPerSec);
            if (mediator != null) {
                mediator.notify(this, DownloadEvent.BANDWIDTH_EXCEEDED, allocatedBandwidthBytesPerSec + bytesPerSec);
            }
            return false;
        }
        this.allocatedBandwidthBytesPerSec += bytesPerSec;
        return true;
    }

    /**
     * Releases previously allocated bandwidth upon completion or termination of a stream.
     *
     * @param bytesPerSec released rate in B/s
     */
    public void releaseBandwidth(long bytesPerSec) {
        this.allocatedBandwidthBytesPerSec = Math.max(0, allocatedBandwidthBytesPerSec - bytesPerSec);
        if (allocatedBandwidthBytesPerSec <= maxBandwidthBytesPerSec) {
            this.bandwidthCongested = false;
        }
        System.out.printf("[BandwidthColleague] [Stub] releaseBandwidth(%d B/s) - Remaining allocated: %d / %d B/s%n",
                bytesPerSec, allocatedBandwidthBytesPerSec, maxBandwidthBytesPerSec);
    }
}
