package com.devpads.unimed.application.shared.exception

open class UnimedException(
    message: String,
    val errorCode: String,
    val violations: List<UnimedViolation> = emptyList(),
) : RuntimeException(message)

data class UnimedViolation(
    val field: String?,
    val message: String,
    val code: String,
)

class BadRequestException(
    message: String,
    code: String = "bad_request",
    violations: List<UnimedViolation> = emptyList(),
) : UnimedException(message, code, violations)

class NotFoundException(
    message: String,
    code: String = "not_found",
    violations: List<UnimedViolation> = emptyList(),
) : UnimedException(message, code, violations)

class ConflictException(
    message: String,
    code: String = "conflict",
    violations: List<UnimedViolation> = emptyList(),
) : UnimedException(message, code, violations)

class UnprocessableEntityException(
    message: String,
    code: String = "validation_error",
    violations: List<UnimedViolation> = emptyList(),
) : UnimedException(message, code, violations)
