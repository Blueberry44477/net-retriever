package io.github.blueberry44477.net_retriever.downloadtask;

import org.mapstruct.Mapper;

import io.github.blueberry44477.net_retriever.downloadtask.dto.DownloadTaskResponse;

@Mapper(componentModel = "spring")
public interface DownloadTaskMapper {
    DownloadTaskResponse toResponse(DownloadTask entity);
}
