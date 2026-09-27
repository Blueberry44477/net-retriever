package io.github.blueberry44477.net_retriever.downloadtask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor
@Accessors(chain = true)
public class CreateDownloadTaskRequest {
    @NotBlank 
    @org.hibernate.validator.constraints.URL
    private String source;

    private String destinationDirectory;
}
