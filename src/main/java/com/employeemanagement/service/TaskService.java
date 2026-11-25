package com.employeemanagement.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.PortalDtos.TaskDto;
import com.employeemanagement.dto.PortalDtos.TaskRequest;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.PortalEnums.TaskStatus;
import com.employeemanagement.entity.Role;
import com.employeemanagement.entity.Task;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.TaskRepository;

/**
 * Anyone can create tasks for themselves; managers, HR and admins can also assign tasks to others.
 * The assignee or the creator can change a task's status; the creator or an admin can delete it.
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final EmployeeRepository employees;
    private final CurrentUserService currentUser;

    public TaskService(TaskRepository tasks, EmployeeRepository employees, CurrentUserService currentUser) {
        this.tasks = tasks;
        this.employees = employees;
        this.currentUser = currentUser;
    }

    public List<TaskDto> mine() {
        return tasks.findAssignedTo(currentUser.currentEmployeeId()).stream().map(TaskService::toDto).toList();
    }

    public List<TaskDto> assignedByMe() {
        return tasks.findCreatedForOthers(currentUser.currentEmployeeId()).stream().map(TaskService::toDto).toList();
    }

    @Transactional
    public TaskDto create(TaskRequest request) {
        Employee me = currentUser.currentEmployee();
        Employee assignee = me;
        if (request.assigneeId() != null && !request.assigneeId().equals(me.getId())) {
            if (!currentUser.hasAnyRole(Role.ADMIN, Role.HR, Role.MANAGER)) {
                throw ApiException.forbidden("Only managers, HR and admins can assign tasks to other people");
            }
            assignee = employees.findById(request.assigneeId())
                    .orElseThrow(() -> ApiException.badRequest("Employee " + request.assigneeId() + " does not exist"));
        }
        if (request.dueDate() != null && request.dueDate().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("The due date can't be in the past");
        }

        Task t = new Task();
        t.setTitle(request.title().trim());
        t.setDescription(DocumentService.blankToNull(request.description()));
        t.setPriority(request.priority());
        t.setDueDate(request.dueDate());
        t.setAssignee(assignee);
        t.setCreatedBy(me);
        return toDto(tasks.save(t));
    }

    @Transactional
    public TaskDto updateStatus(Integer id, TaskStatus status) {
        Task t = find(id);
        int me = currentUser.currentEmployeeId();
        boolean involved = t.getAssignee().getId() == me
                || (t.getCreatedBy() != null && t.getCreatedBy().getId() == me);
        if (!involved) {
            throw ApiException.notFound("Task", id);
        }
        t.changeStatus(status);
        return toDto(t);
    }

    @Transactional
    public void delete(Integer id) {
        Task t = find(id);
        boolean creator = t.getCreatedBy() != null && t.getCreatedBy().getId() == currentUser.currentEmployeeId();
        if (!creator && !currentUser.hasAnyRole(Role.ADMIN)) {
            throw ApiException.forbidden("Only the person who created this task can delete it");
        }
        tasks.delete(t);
    }

    private Task find(Integer id) {
        return tasks.findById(id).orElseThrow(() -> ApiException.notFound("Task", id));
    }

    private static TaskDto toDto(Task t) {
        Employee by = t.getCreatedBy();
        boolean overdue = t.getStatus() != TaskStatus.DONE && t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDate.now());
        return new TaskDto(t.getId(), t.getTitle(), t.getDescription(), t.getPriority(), t.getStatus(),
                t.getDueDate(), overdue, t.getCreatedAt(), t.getCompletedAt(),
                t.getAssignee().getId(), t.getAssignee().getFullName(),
                by == null ? null : by.getId(), by == null ? null : by.getFullName());
    }
}
