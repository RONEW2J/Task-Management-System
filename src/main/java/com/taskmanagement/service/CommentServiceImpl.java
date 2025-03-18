package com.taskmanagement.service;

import com.taskmanagement.dto.request.CommentRequest;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.Comment;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.UserRole;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        checkCommentAccess(task);
        return commentRepository.findByTaskOrderByCreatedAtDesc(task).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        checkCommentAccess(comment.getTask());
        return convertToResponse(comment);
    }

    @Override
    public Page<CommentResponse> getCommentsByTaskId(Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        checkCommentAccess(task);
        return commentRepository.findByTask(task, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        checkCommentAccess(task);
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setUser(getCurrentUser());
        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        checkCommentOwnership(comment);
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return convertToResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        checkCommentOwnership(comment);
        commentRepository.delete(comment);
    }

    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        response.setUser(modelMapper.map(comment.getUser(), UserResponse.class));
        response.setTaskId(comment.getTask().getId());
        return response;
    }

    private void checkCommentAccess(Task task) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == UserRole.ROLE_ADMIN);
        boolean isAuthor = task.getAuthor().equals(currentUser);
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().equals(currentUser);
        
        if (!isAdmin && !isAuthor && !isAssignee) {
            throw new UnauthorizedException("Access denied");
        }
    }

    private void checkCommentOwnership(Comment comment) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == UserRole.ROLE_ADMIN);
        boolean isCommentAuthor = comment.getUser().equals(currentUser);
        boolean isTaskAuthor = comment.getTask().getAuthor().equals(currentUser);
        
        if (!isAdmin && !isCommentAuthor && !isTaskAuthor) {
            throw new UnauthorizedException("Access denied");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}