package io.github.blueberry44477.net_retriever.mapper;

import org.mapstruct.Mapper;

import io.github.blueberry44477.net_retriever.dto.response.DownloadTaskDto;
import io.github.blueberry44477.net_retriever.model.DownloadTask;

@Mapper(componentModel = "spring")
public interface DownloadTaskMapStruct {
    DownloadTaskDto toDto(DownloadTask entity);
}
