package com.taskmanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Task ID is required")
    private Long taskId;
}
