package com.taskmanagement.controller;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getAllTasks(status, priority, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        TaskResponse createdTask = taskService.createTask(taskRequest);
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest taskRequest) {
        TaskResponse updatedTask = taskService.updateTask(id, taskRequest);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<Page<TaskResponse>> getMyTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getMyTasks(status, priority, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<Page<TaskResponse>> getTasksAssignedToMe(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksAssignedToMe(status, priority, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @taskService.isAuthorOrAssignee(#id)")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        TaskResponse updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or @taskService.isAuthorOrAssignee(#id)")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        TaskResponse updatedTask = taskService.assignTask(id, userId);
        return ResponseEntity.ok(updatedTask);
    }
}