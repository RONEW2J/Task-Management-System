package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.request.TaskRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskResponse taskResponse;
    private TaskRequest taskRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setRoles(Set.of("ROLE_USER"));

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(TaskStatus.OPEN);
        taskResponse.setPriority(TaskPriority.MEDIUM);
        taskResponse.setAuthor(userResponse);
        taskResponse.setAssignee(userResponse);
        taskResponse.setCreatedAt(LocalDateTime.now());
        taskResponse.setUpdatedAt(LocalDateTime.now());
        taskResponse.setDueDate(LocalDateTime.now().plusDays(7));

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setPriority(TaskPriority.MEDIUM);
        taskRequest.setAssigneeId(1L);
        taskRequest.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    @WithMockUser
    void getAllTasks_ShouldReturnTaskList() throws Exception {
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> taskPage = new PageImpl<>(tasks);

        when(taskService.getAllTasks(any(), any(), any(Pageable.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(taskResponse.getId()));
    }

    @Test
    @WithMockUser
    void getTaskById_ShouldReturnTask() throws Exception {
        when(taskService.getTaskById(anyLong())).thenReturn(taskResponse);

        mockMvc.perform(get("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponse.getId()))
                .andExpect(jsonPath("$.title").value(taskResponse.getTitle()));
    }

    @Test
    @WithMockUser
    void createTask_ShouldReturnCreatedTask() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponse.getId()))
                .andExpect(jsonPath("$.title").value(taskResponse.getTitle()));
    }

    @Test
    @WithMockUser
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        when(taskService.updateTask(anyLong(), any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponse.getId()))
                .andExpect(jsonPath("$.title").value(taskResponse.getTitle()));
    }

    @Test
    @WithMockUser
    void deleteTask_ShouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(anyLong());

        mockMvc.perform(delete("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMyTasks_ShouldReturnTaskList() throws Exception {
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> taskPage = new PageImpl<>(tasks);

        when(taskService.getMyTasks(any(), any(), any(Pageable.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/my-tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(taskResponse.getId()));
    }

    @Test
    @WithMockUser
    void getTasksAssignedToMe_ShouldReturnTaskList() throws Exception {
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> taskPage = new PageImpl<>(tasks);

        when(taskService.getTasksAssignedToMe(any(), any(), any(Pageable.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/assigned-to-me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(taskResponse.getId()));
    }

    @Test
    @WithMockUser
    void updateTaskStatus_ShouldReturnUpdatedTask() throws Exception {
        when(taskService.updateTaskStatus(anyLong(), any(TaskStatus.class))).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tasks/1/status")
                .param("status", "IN_PROGRESS")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponse.getId()));
    }

    @Test
    @WithMockUser
    void assignTask_ShouldReturnUpdatedTask() throws Exception {
        when(taskService.assignTask(anyLong(), anyLong())).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tasks/1/assign")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskResponse.getId()));
    }
}