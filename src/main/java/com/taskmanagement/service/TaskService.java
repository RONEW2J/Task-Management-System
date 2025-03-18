package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Page<TaskResponse> getAllTasks(TaskStatus status, TaskPriority priority, Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    Page<TaskResponse> getMyTasks(TaskStatus status, TaskPriority priority, Pageable pageable);
    Page<TaskResponse> getTasksAssignedToMe(TaskStatus status, TaskPriority priority, Pageable pageable);
    TaskResponse updateTaskStatus(Long id, TaskStatus status);
    TaskResponse assignTask(Long taskId, Long userId);
    void checkAdminAccess();
    boolean isAuthorOrAssignee(Long taskId);
    boolean isAuthor(Long taskId);
}