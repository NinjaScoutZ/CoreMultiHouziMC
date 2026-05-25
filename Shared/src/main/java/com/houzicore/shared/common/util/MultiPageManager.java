package com.houzicore.shared.common.util;

import java.util.List;
import java.util.ArrayList;

/**
 * A generic pagination utility for partitioning large datasets across multiple UI pages.
 */
public class MultiPageManager<T> {

    private final List<T> _items;
    private final int _itemsPerPage;
    private int _currentPage = 1;

    public MultiPageManager(List<T> items, int itemsPerPage) {
        if (items != null) {
            _items = new ArrayList<>(items);
        } else {
            _items = new ArrayList<>();
        }
        _itemsPerPage = itemsPerPage;
    }

    public List<T> getPage(int page) {
        _currentPage = Math.max(1, Math.min(page, getTotalPages()));
        int start = (_currentPage - 1) * _itemsPerPage;
        int end = Math.min(start + _itemsPerPage, _items.size());
        
        if (start > end || start >= _items.size()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(_items.subList(start, end));
    }

    public List<T> getCurrentPage() {
        return getPage(_currentPage);
    }

    public boolean nextPage() {
        if (hasNextPage()) {
            _currentPage++;
            return true;
        }
        return false;
    }

    public boolean previousPage() {
        if (hasPreviousPage()) {
            _currentPage--;
            return true;
        }
        return false;
    }

    public boolean hasNextPage() {
        return _currentPage < getTotalPages();
    }

    public boolean hasPreviousPage() {
        return _currentPage > 1;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) _items.size() / _itemsPerPage));
    }

    public int getCurrentPageIndex() {
        return _currentPage;
    }

    public int getTotalItems() {
        return _items.size();
    }

    // ═══════════════════════════════════════════════════════════════
    // Extended methods — ported from HypixelSkyBlock PaginationList
    // ═══════════════════════════════════════════════════════════════

    /** Batch add items. */
    public void addAll(java.util.Collection<T> items) {
        if (items != null) _items.addAll(items);
    }

    /** Add a single item. */
    public void add(T item) {
        _items.add(item);
    }

    /** Filter items by predicate (removes matching items). Useful for search. */
    public void removeIf(java.util.function.Predicate<T> filter) {
        _items.removeIf(filter);
    }

    /** Alias for getTotalPages() — consistency with Hypixel PaginationList naming. */
    public int getPageCount() {
        return getTotalPages();
    }

    /** Stream support for functional operations. */
    public java.util.stream.Stream<T> stream() {
        return _items.stream();
    }

    /** Check if empty. */
    public boolean isEmpty() {
        return _items.isEmpty();
    }

    /** Total item count. Alias for getTotalItems(). */
    public int size() {
        return _items.size();
    }
}
