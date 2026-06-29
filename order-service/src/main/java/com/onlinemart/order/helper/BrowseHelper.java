package com.onlinemart.order.helper;

import com.onlinemart.order.dto.request.BrowseRequestDto;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class BrowseHelper {

    /**
     * Builds a JPA Specification from the filters array.
     * Filter format: [{ "categoryId": 1 }, { "status": true }]
     * Each map entry becomes an AND predicate.
     */
    public static <T> Specification<T> buildSpecification(List<Map<String, Object>> filters) {
        return (root, query, cb) -> {
            if (filters == null || filters.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (Map<String, Object> filterEntry : filters) {
                for (Map.Entry<String, Object> entry : filterEntry.entrySet()) {
                    String field = entry.getKey();
                    Object value = entry.getValue();

                    if (value == null) continue;

                    try {
                        Path<?> path = resolvePath(root, field);
                        predicates.add(buildEqualPredicate(cb, path, value));
                    } catch (IllegalArgumentException e) {
                        // Skip unknown fields gracefully
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds Pageable from the sort array.
     * Sort format: [{ "price": "asc" }, { "createdAt": "desc" }]
     */
    public static Pageable buildPageable(BrowseRequestDto req) {
        List<Sort.Order> orders = new ArrayList<>();

        if (req.getSort() != null) {
            for (Map<String, String> sortEntry : req.getSort()) {
                for (Map.Entry<String, String> entry : sortEntry.entrySet()) {
                    String field = entry.getKey();
                    Sort.Direction direction = "desc".equalsIgnoreCase(entry.getValue())
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, field));
                }
            }
        }

        Sort sort = orders.isEmpty() ? Sort.by("id").ascending() : Sort.by(orders);
        int pageSize = req.getLimit() > 0 ? req.getLimit() : req.getSize();

        return PageRequest.of(req.getPage(), pageSize, sort);
    }

    /**
     * Supports dot-notation for nested fields: "category.name"
     */
    @SuppressWarnings("unchecked")
    private static Path<Object> resolvePath(Root<?> root, String field) {
        String[] parts = field.split("\\.");
        Path<Object> path = (Path<Object>) root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = (Path<Object>) path.get(parts[i]);
        }
        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildEqualPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        // Handle numeric coercion (JSON numbers come as Integer/Long/Double)
        Class<?> javaType = path.getJavaType();

        if (javaType == Long.class && value instanceof Integer i) {
            return cb.equal(path, i.longValue());
        }
        if (javaType == Boolean.class && value instanceof Boolean b) {
            return cb.equal(path, b);
        }
        if (Enum.class.isAssignableFrom(javaType) && value instanceof String s) {
            return cb.equal(path, Enum.valueOf((Class<Enum>) javaType, s.toUpperCase()));
        }
        return cb.equal(path, value);
    }
}