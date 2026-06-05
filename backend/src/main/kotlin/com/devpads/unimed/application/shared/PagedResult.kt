package com.devpads.unimed.application.shared

data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)