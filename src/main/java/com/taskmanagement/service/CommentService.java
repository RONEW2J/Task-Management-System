package com.taskmanagement.service;

import com.taskmanagement.dto.request.CommentRequest;
import com.taskmanagement.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CommentService {
    List<CommentResponse> getCommentsByTaskId(Long taskId);
    Page<CommentResponse> getCommentsByTaskId(Long taskId, Pageable pageable);
    CommentResponse createComment(CommentRequest request);
    CommentResponse updateComment(Long commentId, CommentRequest request);
    void deleteComment(Long commentId);
    CommentResponse getCommentById(Long commentId);
}