package com.devpads.unimed.infrastructure.web.shared.dto

data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)
