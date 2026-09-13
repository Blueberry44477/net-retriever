package io.github.blueberry44477.net_retriever.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

@Entity
@Table(name = "download_task")
public class DownloadTask {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME) // UUIDv7
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank
    @Size(max = 2048)
    @Column(nullable = false)
    private String url;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String fileName;

    @NotBlank
    @Size(max = 512)
    @Column(nullable = false)
    private String savePath;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DownloadStatus status = DownloadStatus.CREATED;

    @PositiveOrZero
    @Column(nullable = false)
    private long downloadedBytes = 0L;

    @PositiveOrZero
    @Column(nullable = false)
    private long totalBytes = 0L;

    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
