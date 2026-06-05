package com.devpads.unimed.infrastructure.web.error

import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnimedException
import com.devpads.unimed.application.shared.exception.UnimedViolation
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI
import java.time.DateTimeException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnimedException::class)
    fun handleUnimedException(ex: UnimedException, request: HttpServletRequest): ProblemDetail {
        val status = when (ex) {
            is BadRequestException -> HttpStatus.BAD_REQUEST
            is NotFoundException -> HttpStatus.NOT_FOUND
            is ConflictException -> HttpStatus.CONFLICT
            is UnprocessableEntityException -> HttpStatus.UNPROCESSABLE_ENTITY
            else -> HttpStatus.BAD_REQUEST
        }

        return buildProblem(
            status = status,
            detail = ex.message ?: defaultDetail(status),
            code = ex.errorCode,
            request = request,
            details = ex.violations.map { it.toProblemFieldDetail() },
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class, ConstraintViolationException::class)
    fun handleValidationExceptions(ex: Exception, request: HttpServletRequest): ProblemDetail {
        val details = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult.fieldErrors.map {
                ProblemFieldDetail(
                    field = it.field,
                    message = it.defaultMessage ?: "Invalid value",
                    code = it.code?.lowercase() ?: "invalid",
                )
            }

            is BindException -> ex.bindingResult.fieldErrors.map {
                ProblemFieldDetail(
                    field = it.field,
                    message = it.defaultMessage ?: "Invalid value",
                    code = it.code?.lowercase() ?: "invalid",
                )
            }

            is ConstraintViolationException -> ex.constraintViolations.map {
                ProblemFieldDetail(
                    field = it.propertyPath.toString(),
                    message = it.message,
                    code = "invalid",
                )
            }

            else -> emptyList()
        }

        return buildProblem(
            status = HttpStatus.UNPROCESSABLE_ENTITY,
            detail = "Validation failed",
            code = "validation_error",
            request = request,
            details = details,
        )
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        HttpMessageNotReadableException::class,
        DateTimeException::class,
    )
    fun handleBadRequestExceptions(ex: Exception, request: HttpServletRequest): ProblemDetail {
        return buildProblem(
            status = HttpStatus.BAD_REQUEST,
            detail = ex.message ?: defaultDetail(HttpStatus.BAD_REQUEST),
            code = "bad_request",
            request = request,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(request: HttpServletRequest): ProblemDetail {
        return buildProblem(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            detail = defaultDetail(HttpStatus.INTERNAL_SERVER_ERROR),
            code = "internal_server_error",
            request = request,
        )
    }

    private fun buildProblem(
        status: HttpStatus,
        detail: String,
        code: String,
        request: HttpServletRequest,
        details: List<ProblemFieldDetail> = emptyList(),
    ): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.title = status.reasonPhrase
        problem.type = URI.create("https://unimed.dev/problems/$code")
        problem.instance = URI.create(request.requestURI)
        problem.setProperty("details", details)
        return problem
    }

    private fun defaultDetail(status: HttpStatus): String {
        return when (status) {
            HttpStatus.BAD_REQUEST -> "Request is invalid"
            HttpStatus.NOT_FOUND -> "Resource not found"
            HttpStatus.CONFLICT -> "Resource conflict"
            HttpStatus.UNPROCESSABLE_ENTITY -> "Validation failed"
            else -> "Unexpected server error"
        }
    }

    private fun UnimedViolation.toProblemFieldDetail(): ProblemFieldDetail {
        return ProblemFieldDetail(
            field = field,
            message = message,
            code = code,
        )
    }

    private data class ProblemFieldDetail(
        val field: String?,
        val message: String,
        val code: String,
    )
}
