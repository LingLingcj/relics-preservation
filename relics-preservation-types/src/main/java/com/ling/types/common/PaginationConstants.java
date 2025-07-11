package com.ling.types.common;

/**
 * 分页常量
 * @Author: LingRJ
 */
public final class PaginationConstants {

    private PaginationConstants() {}

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    public static int normalizePage(int page) {
        return Math.max(page, DEFAULT_PAGE);
    }

    public static int normalizePageSize(int size) {
        return Math.max(Math.min(size, MAX_PAGE_SIZE), MIN_PAGE_SIZE);
    }

    public static int calculateOffset(int page, int size) {
        return (normalizePage(page) - 1) * normalizePageSize(size);
    }
}
