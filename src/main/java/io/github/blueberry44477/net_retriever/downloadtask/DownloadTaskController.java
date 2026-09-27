package io.github.blueberry44477.net_retriever.downloadtask;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.blueberry44477.net_retriever.downloadtask.dto.CreateDownloadTaskRequest;
import io.github.blueberry44477.net_retriever.downloadtask.dto.DownloadTaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor 
@RequestMapping("/api/v1/tasks")
public class DownloadTaskController {
    private final DownloadTaskService service;
    private final DownloadTaskMapper downloadTaskMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DownloadTaskResponse createTask(@Valid @RequestBody CreateDownloadTaskRequest request) throws IOException {
        return downloadTaskMapper.toResponse(service.createTask(request.getSource(), 
                request.getDestinationDirectory()));
    }

    @PostMapping("/{id}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startDownload(@PathVariable UUID id) {
        // service.startDownload(id);
    }
}
