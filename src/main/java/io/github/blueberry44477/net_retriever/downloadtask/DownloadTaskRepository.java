package io.github.blueberry44477.net_retriever.downloadtask;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

public interface DownloadTaskRepository extends JpaRepository<DownloadTask, UUID> {
    Page<DownloadTask> findByStatus(DownloadStatus status, Pageable pageable);
}
