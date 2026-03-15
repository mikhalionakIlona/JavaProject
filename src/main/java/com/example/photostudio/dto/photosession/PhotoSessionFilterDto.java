package com.example.photostudio.dto.photosession;

import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PhotoSessionFilterDto(
        String clientName,
        String photographerName,
        String phone,
        @Min(value = 0, message = "Номер страницы должен быть неотрицательным") Integer page,
        @Min(value = 1, message = "Размер страницы должен быть не менее 1") Integer size,
        String sortBy,
        String sortDirection
) {
    public PhotoSessionFilterDto {
        page = page != null ? page : 0;
        size = size != null ? size : 10;
        sortBy = sortBy != null ? sortBy : "date";
        sortDirection = sortDirection != null ? sortDirection : "DESC";
    }

    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientName;
        private String photographerName;
        private String phone;
        private Integer page = 0;
        private Integer size = 10;
        private String sortBy = "date";
        private String sortDirection = "DESC";

        public Builder clientName(String clientName) {
            this.clientName = clientName; return this; }
        public Builder photographerName(String photographerName) {
            this.photographerName = photographerName; return this; }
        public Builder phone(String phone) {
            this.phone = phone; return this; }
        public Builder page(Integer page) {
            this.page = page != null ? page : 0; return this; }
        public Builder size(Integer size) {
            this.size = size != null ? size : 10; return this; }
        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy != null ? sortBy : "date"; return this; }
        public Builder sortDirection(String sortDirection) {
            this.sortDirection = sortDirection != null ? sortDirection : "DESC"; return this; }

        public PhotoSessionFilterDto build() {
            return new PhotoSessionFilterDto(
                    clientName, photographerName, phone, page, size, sortBy, sortDirection
            );
        }
    }
}