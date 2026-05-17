package com.example.NewToDo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses
 * Wraps the content and pagination metadata
 * 
 * @param <T> The type of content in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;           // The actual data
    private int pageNumber;            // Current page number (0-indexed)
    private int pageSize;              // Number of items per page
    private long totalElements;        // Total number of items across all pages
    private int totalPages;            // Total number of pages
    private boolean first;             // Is this the first page?
    private boolean last;              // Is this the last page?
    private boolean hasNext;           // Is there a next page?
    private boolean hasPrevious;       // Is there a previous page?
    
    /**
     * Factory method to create PageResponse from Spring's Page object
     * 
     * @param page Spring Data Page object
     * @param <T> Type of content
     * @return PageResponse with all metadata
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}

// Made with Bob
