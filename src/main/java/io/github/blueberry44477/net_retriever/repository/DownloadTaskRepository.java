package io.github.blueberry44477.net_retriever.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.blueberry44477.net_retriever.model.DownloadStatus;
import io.github.blueberry44477.net_retriever.model.DownloadTask;

public interface DownloadTaskRepository extends JpaRepository<DownloadTask, UUID> {
    Page<DownloadTask> findByStatus(DownloadStatus status, Pageable pageable);
}
