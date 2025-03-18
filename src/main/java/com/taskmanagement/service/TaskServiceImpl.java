package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.entity.enums.UserRole;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<TaskResponse> getAllTasks(TaskStatus status, TaskPriority priority, Pageable pageable) {
        return taskRepository.findAllByStatusAndPriority(status, priority, pageable)
            .map(this::convertToResponse);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return convertToResponse(task);
    }

    @Override
    public TaskResponse createTask(TaskRequest request) {
        User author = userRepository.findById(request.getAuthorId())
            .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAuthor(author);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Page<TaskResponse> getMyTasks(TaskStatus status, TaskPriority priority, Pageable pageable) {
        User currentUser = getCurrentUser();
        return taskRepository.findByAuthorAndStatusAndPriority(currentUser, status, priority, pageable)
            .map(this::convertToResponse);
    }

    @Override
    public Page<TaskResponse> getTasksAssignedToMe(TaskStatus status, TaskPriority priority, Pageable pageable) {
        User currentUser = getCurrentUser();
        return taskRepository.findByAssigneeAndStatusAndPriority(currentUser, status, priority, pageable)
            .map(this::convertToResponse);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        task.setAssignee(user);
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    
    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = modelMapper.map(task, TaskResponse.class);
        response.setAuthorEmail(task.getAuthor().getEmail());
        response.setAssigneeEmail(task.getAssignee() != null ? task.getAssignee().getEmail() : null);
        response.setComments(task.getComments().stream()
            .map(comment -> modelMapper.map(comment, CommentResponse.class))
            .collect(Collectors.toList()));
        return response;
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public void checkAdminAccess() {
    User currentUser = getCurrentUser();
    if (!currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
        throw new UnauthorizedException("Access denied");
    }
    }

    public boolean isAuthorOrAssignee(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        User currentUser = getCurrentUser();
        return task.getAuthor().equals(currentUser) || task.getAssignee().equals(currentUser);
    }
    
    public boolean isAuthor(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        User currentUser = getCurrentUser();
        return task.getAuthor().equals(currentUser);
    }
}