package com.devpads.unimed.infrastructure.web.error

import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnimedViolation
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

class GlobalExceptionHandlerTest {

    private lateinit var handler: GlobalExceptionHandler
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setup() {
        handler = GlobalExceptionHandler()
        request = mock {
            on { requestURI } doReturn "/api/pacientes/1"
        }
    }

    @Test
    fun `should return 400 for BadRequestException`() {
        val ex = BadRequestException("Invalid CPF format")
        val result = handler.handleUnimedException(ex, request)
        assertProblemDetail(result, HttpStatus.BAD_REQUEST, "Invalid CPF format", "bad_request")
    }

    @Test
    fun `should return 404 for NotFoundException`() {
        val ex = NotFoundException("Paciente not found")
        val result = handler.handleUnimedException(ex, request)
        assertProblemDetail(result, HttpStatus.NOT_FOUND, "Paciente not found", "not_found")
    }

    @Test
    fun `should return 409 for ConflictException`() {
        val ex = ConflictException("CPF already exists")
        val result = handler.handleUnimedException(ex, request)
        assertProblemDetail(result, HttpStatus.CONFLICT, "CPF already exists", "conflict")
    }

    @Test
    fun `should return 422 for UnprocessableEntityException`() {
        val ex = UnprocessableEntityException("Name is required")
        val result = handler.handleUnimedException(ex, request)
        assertProblemDetail(result, HttpStatus.UNPROCESSABLE_ENTITY, "Name is required", "validation_error")
    }

    @Test
    fun `should return 422 with violations for UnprocessableEntityException with violations`() {
        val violations = listOf(
            UnimedViolation(field = "nome", message = "Name is required", code = "required"),
        )
        val ex = UnprocessableEntityException("Validation failed", violations = violations)
        val result = handler.handleUnimedException(ex, request)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.status)
        assertEquals("Validation failed", result.detail)
        val details = result.properties!!["details"] as List<*>
        assertEquals(1, details.size)
    }

    @Test
    fun `should return 400 for IllegalArgumentException`() {
        val ex = IllegalArgumentException("Invalid argument")
        val result = handler.handleBadRequestExceptions(ex, request)
        assertProblemDetail(result, HttpStatus.BAD_REQUEST, "Invalid argument", "bad_request")
    }

    @Test
    fun `should return 500 for unexpected exception`() {
        val result = handler.handleUnexpectedException(request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.status)
        assertEquals("Unexpected server error", result.detail)
        assertEquals("https://unimed.dev/problems/internal_server_error", result.type.toString())
    }

    private fun assertProblemDetail(result: ProblemDetail, expectedStatus: HttpStatus, expectedDetail: String, expectedCode: String) {
        assertEquals(expectedStatus.value(), result.status)
        assertEquals(expectedDetail, result.detail)
        assertNotNull(result.type)
        assertTrue(result.type.toString().endsWith("/$expectedCode"), "Type should end with /$expectedCode but was ${result.type}")
        assertEquals("/api/pacientes/1", result.instance.toString())
    }
}
