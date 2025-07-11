package com.ling.types.common;

/**
 * 分页工具类
 * @Author: LingRJ
 */
public final class PaginationUtils {

    private PaginationUtils() {}

    public static class PaginationParams {
        private final int page;
        private final int size;

        public PaginationParams(int page, int size) {
            this.page = PaginationConstants.normalizePage(page);
            this.size = PaginationConstants.normalizePageSize(size);
        }

        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getOffset() { return PaginationConstants.calculateOffset(page, size); }
    }

    public static PaginationParams validateAndNormalize(int page, int size) {
        return new PaginationParams(page, size);
    }
}
