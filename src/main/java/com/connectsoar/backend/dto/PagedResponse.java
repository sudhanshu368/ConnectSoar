package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PagedResponse<T> {

    private List<T> items;
    private PaginationMeta pagination;

    public PagedResponse() {
    }

    public PagedResponse(List<T> items, PaginationMeta pagination) {
        this.items = items;
        this.pagination = pagination;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> items;
        private PaginationMeta pagination;

        public Builder<T> items(List<T> items) { this.items = items; return this; }
        public Builder<T> pagination(PaginationMeta pagination) { this.pagination = pagination; return this; }

        public PagedResponse<T> build() {
            return new PagedResponse<>(items, pagination);
        }
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public PaginationMeta getPagination() { return pagination; }
    public void setPagination(PaginationMeta pagination) { this.pagination = pagination; }

    public static class PaginationMeta {
        private int page;
        private int limit;
        private long total;

        @JsonProperty("total_pages")
        private int totalPages;

        public PaginationMeta() {
        }

        public PaginationMeta(int page, int limit, long total, int totalPages) {
            this.page = page;
            this.limit = limit;
            this.total = total;
            this.totalPages = totalPages;
        }

        public static PaginationMetaBuilder builder() {
            return new PaginationMetaBuilder();
        }

        public static class PaginationMetaBuilder {
            private int page;
            private int limit;
            private long total;
            private int totalPages;

            public PaginationMetaBuilder page(int page) { this.page = page; return this; }
            public PaginationMetaBuilder limit(int limit) { this.limit = limit; return this; }
            public PaginationMetaBuilder total(long total) { this.total = total; return this; }
            public PaginationMetaBuilder totalPages(int totalPages) { this.totalPages = totalPages; return this; }

            public PaginationMeta build() {
                return new PaginationMeta(page, limit, total, totalPages);
            }
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
