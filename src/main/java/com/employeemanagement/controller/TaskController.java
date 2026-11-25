package com.employeemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.PortalDtos.TaskDto;
import com.employeemanagement.dto.PortalDtos.TaskRequest;
import com.employeemanagement.dto.PortalDtos.TaskStatusUpdate;
import com.employeemanagement.service.TaskService;

import jakarta.validation.Valid;

/** Who may do what is decided per task in TaskService (assignee, creator, role). */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/mine")
    public List<TaskDto> mine() {
        return service.mine();
    }

    @GetMapping("/assigned-by-me")
    public List<TaskDto> assignedByMe() {
        return service.assignedByMe();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@Valid @RequestBody TaskRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/status")
    public TaskDto updateStatus(@PathVariable Integer id, @Valid @RequestBody TaskStatusUpdate body) {
        return service.updateStatus(id, body.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
