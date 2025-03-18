package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.TaskPriority;
import com.taskmanagement.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Page<Task> findByAuthor(User author, Pageable pageable);
    Page<Task> findByAssignee(User assignee, Pageable pageable);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
    Page<Task> findByAuthorAndStatus(User author, TaskStatus status, Pageable pageable);
    Page<Task> findByAssigneeAndStatus(User assignee, TaskStatus status, Pageable pageable);
    Page<Task> findByAuthorAndStatusAndPriority(
        User author,
        TaskStatus status,
        TaskPriority priority,
        Pageable pageable
    );
    Page<Task> findByAssigneeAndStatusAndPriority(
        User assignee,
        TaskStatus status,
        TaskPriority priority,
        Pageable pageable
    );
    Page<Task> findAllByStatusAndPriority(
        TaskStatus status,
        TaskPriority priority,
        Pageable pageable
    );
}
