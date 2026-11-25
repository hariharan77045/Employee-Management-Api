package com.employeemanagement.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employeemanagement.dto.PortalDtos.LibraryItemDto;
import com.employeemanagement.dto.PortalDtos.LibraryItemRequest;
import com.employeemanagement.dto.PortalDtos.LoanDto;
import com.employeemanagement.entity.LibraryItem;
import com.employeemanagement.entity.LibraryLoan;
import com.employeemanagement.exception.ApiException;
import com.employeemanagement.repository.LibraryItemRepository;
import com.employeemanagement.repository.LibraryLoanRepository;

/** Catalogue with live availability; employees borrow up to 3 items at a time for 14 days. */
@Service
@Transactional(readOnly = true)
public class LibraryService {

    static final int LOAN_DAYS = 14;
    static final int MAX_ACTIVE_LOANS = 3;

    private final LibraryItemRepository items;
    private final LibraryLoanRepository loans;
    private final CurrentUserService currentUser;

    public LibraryService(LibraryItemRepository items, LibraryLoanRepository loans, CurrentUserService currentUser) {
        this.items = items;
        this.loans = loans;
        this.currentUser = currentUser;
    }

    public List<LibraryItemDto> search(String query, String category) {
        String term = query == null ? "" : query.trim().toLowerCase();
        String pattern = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";

        Map<Integer, Long> out = new HashMap<>();
        for (Object[] row : loans.countActiveLoansByItem()) {
            out.put((Integer) row[0], (Long) row[1]);
        }
        Set<Integer> mine = new HashSet<>(loans.findActiveItemIds(currentUser.currentEmployeeId()));

        return items.search(pattern, category == null ? "" : category.trim()).stream()
                .map(i -> toDto(i, out.getOrDefault(i.getId(), 0L), mine.contains(i.getId())))
                .toList();
    }

    public List<String> categories() {
        return items.findCategories();
    }

    public List<LoanDto> myLoans() {
        return loans.findByEmployeeWithItem(currentUser.currentEmployeeId()).stream()
                .map(LibraryService::toDto)
                .toList();
    }

    @Transactional
    public LoanDto borrow(Integer itemId) {
        int me = currentUser.currentEmployeeId();
        // Row lock: concurrent borrows of the same item wait here, so the count below stays correct.
        LibraryItem item = items.findByIdForUpdate(itemId)
                .orElseThrow(() -> ApiException.notFound("Library item", itemId));

        if (loans.existsByItemIdAndEmployeeIdAndReturnedOnIsNull(itemId, me)) {
            throw ApiException.conflict("You already have \"" + item.getTitle() + "\"");
        }
        if (loans.countByEmployeeIdAndReturnedOnIsNull(me) >= MAX_ACTIVE_LOANS) {
            throw ApiException.conflict("You can borrow up to " + MAX_ACTIVE_LOANS
                    + " items at a time. Return one first.");
        }
        if (loans.countByItemIdAndReturnedOnIsNull(itemId) >= item.getTotalCopies()) {
            throw ApiException.conflict("All copies of \"" + item.getTitle() + "\" are out right now");
        }

        LibraryLoan loan = new LibraryLoan();
        loan.setItem(item);
        loan.setEmployee(currentUser.currentEmployee());
        loan.setBorrowedOn(LocalDate.now());
        loan.setDueOn(LocalDate.now().plusDays(LOAN_DAYS));
        return toDto(loans.save(loan));
    }

    @Transactional
    public LoanDto giveBack(Integer loanId) {
        LibraryLoan loan = loans.findById(loanId).orElseThrow(() -> ApiException.notFound("Loan", loanId));
        if (loan.getEmployee().getId() != currentUser.currentEmployeeId()) {
            throw ApiException.notFound("Loan", loanId);
        }
        if (loan.getReturnedOn() != null) {
            throw ApiException.conflict("This item has already been returned");
        }
        loan.setReturnedOn(LocalDate.now());
        return toDto(loan);
    }

    @Transactional
    public LibraryItemDto addItem(LibraryItemRequest request) {
        LibraryItem i = new LibraryItem();
        i.setTitle(request.title().trim());
        i.setAuthor(DocumentService.blankToNull(request.author()));
        i.setCategory(request.category().trim());
        i.setFormat(request.format());
        i.setDescription(DocumentService.blankToNull(request.description()));
        i.setTotalCopies(request.totalCopies());
        return toDto(items.save(i), 0, false);
    }

    private static LibraryItemDto toDto(LibraryItem i, long out, boolean mine) {
        int available = (int) Math.max(0, i.getTotalCopies() - out);
        return new LibraryItemDto(i.getId(), i.getTitle(), i.getAuthor(), i.getCategory(), i.getFormat(),
                i.getDescription(), i.getTotalCopies(), available, mine);
    }

    private static LoanDto toDto(LibraryLoan l) {
        LibraryItem i = l.getItem();
        boolean overdue = l.getReturnedOn() == null && l.getDueOn().isBefore(LocalDate.now());
        return new LoanDto(l.getId(), i.getId(), i.getTitle(), i.getAuthor(), i.getFormat(),
                l.getBorrowedOn(), l.getDueOn(), l.getReturnedOn(), overdue);
    }
}
