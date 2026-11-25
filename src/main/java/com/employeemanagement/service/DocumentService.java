package com.employeemanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.PortalDtos.DocumentDetail;
import com.employeemanagement.dto.PortalDtos.DocumentRequest;
import com.employeemanagement.dto.PortalDtos.DocumentSummary;
import com.employeemanagement.entity.CompanyDocument;
import com.employeemanagement.entity.Role;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.CompanyDocumentRepository;
import com.employeemanagement.repository.EmployeeRepository;

/**
 * Everyone reads company-wide documents and their own personal ones. HR and admins can open any
 * document and publish or delete them.
 */
@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final CompanyDocumentRepository documents;
    private final EmployeeRepository employees;
    private final CurrentUserService currentUser;

    public DocumentService(CompanyDocumentRepository documents, EmployeeRepository employees,
                           CurrentUserService currentUser) {
        this.documents = documents;
        this.employees = employees;
        this.currentUser = currentUser;
    }

    public List<DocumentSummary> list() {
        return documents.findVisibleTo(currentUser.currentEmployeeId()).stream()
                .map(d -> new DocumentSummary(d.getId(), d.getTitle(), d.getCategory(), d.getDescription(),
                        d.getOwner() != null, d.getUploadedBy(), d.getUpdatedAt()))
                .toList();
    }

    public DocumentDetail get(Integer id) {
        CompanyDocument d = documents.findById(id).orElseThrow(() -> ApiException.notFound("Document", id));
        boolean mine = d.getOwner() == null || d.getOwner().getId() == currentUser.currentEmployeeId();
        if (!mine && !currentUser.hasAnyRole(Role.ADMIN, Role.HR)) {
            // Same answer as a missing document, so ids of other people's files aren't confirmed.
            throw ApiException.notFound("Document", id);
        }
        return toDetail(d);
    }

    @Transactional
    public DocumentDetail create(DocumentRequest request) {
        CompanyDocument d = new CompanyDocument();
        d.setTitle(request.title().trim());
        d.setCategory(request.category().trim());
        d.setDescription(blankToNull(request.description()));
        d.setContent(request.content().trim());
        if (request.ownerId() != null) {
            d.setOwner(employees.findById(request.ownerId())
                    .orElseThrow(() -> ApiException.badRequest("Employee " + request.ownerId() + " does not exist")));
        }
        d.setUploadedBy(currentUser.currentEmployee().getFullName());
        d.setUpdatedAt(LocalDateTime.now());
        return toDetail(documents.save(d));
    }

    @Transactional
    public void delete(Integer id) {
        if (!documents.existsById(id)) {
            throw ApiException.notFound("Document", id);
        }
        documents.deleteById(id);
    }

    private static DocumentDetail toDetail(CompanyDocument d) {
        return new DocumentDetail(d.getId(), d.getTitle(), d.getCategory(), d.getDescription(),
                d.getOwner() != null, d.getUploadedBy(), d.getUpdatedAt(), d.getContent());
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
