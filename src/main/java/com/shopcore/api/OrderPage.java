package com.shopcore.api;

import java.util.List;

/** nextCursor == null means this is the last page. */
public record OrderPage(List<OrderResponse> orders, String nextCursor) {
}