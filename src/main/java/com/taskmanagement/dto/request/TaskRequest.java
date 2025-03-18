package com.taskmanagement.dto.request;

import java.time.LocalDateTime;

import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    private Long assigneeId;

    private Long authorId;

    private LocalDateTime dueDate;
    
    public Long getAuthorId() {
        return authorId;
    }
}
