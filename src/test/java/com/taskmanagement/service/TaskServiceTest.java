package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.entity.enums.UserRole;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private User adminUser;
    private User assigneeUser;
    private Task task;
    private TaskRequest taskRequest;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(UserRole.ROLE_USER);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(UserRole.ROLE_ADMIN);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        assigneeUser = new User();
        assigneeUser.setId(3L);
        assigneeUser.setUsername("assignee");
        assigneeUser.setEmail("assignee@example.com");
        assigneeUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(testUser);
        task.setAssignee(assigneeUser);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskRequest = new TaskRequest();
        taskRequest.setTitle("New Task");
        taskRequest.setDescription("New Description");
        taskRequest.setPriority(TaskPriority.HIGH);
        taskRequest.setAssigneeId(3L);
        taskRequest.setDueDate(LocalDateTime.now().plusDays(10));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(3L)).thenReturn(Optional.of(assigneeUser));
    }

    @Test
    void getAllTasks_ShouldReturnPageOfTasks() {
        List<Task> tasks = Collections.singletonList(task);
        Page<Task> taskPage = new PageImpl<>(tasks);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);
        
        Page<TaskResponse> result = taskService.getAllTasks(null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(task.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getTaskById_WithValidId_ShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        TaskResponse result = taskService.getTaskById(1L);
        
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals(task.getTitle(), result.getTitle());
    }

    @Test
    void getTaskById_WithInvalidId_ShouldThrowException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        TaskResponse result = taskService.createTask(taskRequest);
        
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_AsAuthor_ShouldReturnUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        TaskResponse result = taskService.updateTask(1L, taskRequest);
        
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_AsUnauthorized_ShouldThrowException() {
        Task otherTask = new Task();
        otherTask.setId(2L);
        otherTask.setTitle("Other Task");
        otherTask.setAuthor(adminUser);
        
        when(taskRepository.findById(2L)).thenReturn(Optional.of(otherTask));
        
        assertThrows(UnauthorizedException.class, () -> taskService.updateTask(2L, taskRequest));
    }

    @Test
    void deleteTask_AsAuthor_ShouldDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);
        
        taskService.deleteTask(1L);
        
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_AsUnauthorized_ShouldThrowException() {
        Task otherTask = new Task();
        otherTask.setId(2L);
        otherTask.setTitle("Other Task");
        otherTask.setAuthor(adminUser);
        
        when(taskRepository.findById(2L)).thenReturn(Optional.of(otherTask));
        
        assertThrows(UnauthorizedException.class, () -> taskService.deleteTask(2L));
    }

    @Test
    void getMyTasks_ShouldReturnUserTasks() {
        List<Task> tasks = Collections.singletonList(task);
        Page<Task> taskPage = new PageImpl<>(tasks);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(taskRepository.findByAuthor(eq(testUser), any(Pageable.class))).thenReturn(taskPage);
        
        Page<TaskResponse> result = taskService.getMyTasks(null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(task.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getTasksAssignedToMe_ShouldReturnAssignedTasks() {
        List<Task> tasks = Collections.singletonList(task);
        Page<Task> taskPage = new PageImpl<>(tasks);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(taskRepository.findByAssignee(eq(testUser), any(Pageable.class))).thenReturn(taskPage);
        
        Page<TaskResponse> result = taskService.getTasksAssignedToMe(null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(task.getId(), result.getContent().get(0).getId());
    }

    @Test
    void updateTaskStatus_AsAuthorized_ShouldUpdateStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        TaskResponse result = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);
        
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void assignTask_AsAuthorized_ShouldAssignTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        TaskResponse result = taskService.assignTask(1L, 3L);
        
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
}