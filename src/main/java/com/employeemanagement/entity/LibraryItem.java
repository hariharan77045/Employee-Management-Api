package com.employeemanagement.entity;

import com.employeemanagement.entity.PortalEnums.LibraryFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_item")
public class LibraryItem {

    @Id
    @Column(name = "itemid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "category", nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private LibraryFormat format;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "totalcopies", nullable = false)
    private int totalCopies;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LibraryFormat getFormat() { return format; }
    public void setFormat(LibraryFormat format) { this.format = format; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
}
