package com.employeemanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeemanagement.entity.LibraryItem;

import jakarta.persistence.LockModeType;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Integer> {

    /** {@code pattern} is a lower-case LIKE pattern; an empty {@code category} means all categories. */
    @Query("""
            select i from LibraryItem i
            where (:category = '' or i.category = :category)
              and (lower(i.title) like :pattern or lower(coalesce(i.author, '')) like :pattern)
            order by i.title
            """)
    List<LibraryItem> search(@Param("pattern") String pattern, @Param("category") String category);

    @Query("select distinct i.category from LibraryItem i order by i.category")
    List<String> findCategories();

    /**
     * Locks the item row (SELECT ... FOR UPDATE) so two people can't borrow the last copy at once.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from LibraryItem i where i.id = :id")
    Optional<LibraryItem> findByIdForUpdate(@Param("id") Integer id);
}
