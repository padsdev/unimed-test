package com.devpads.unimed.infrastructure.web.atendimento

import com.devpads.unimed.application.atendimento.service.AtendimentoService
import com.devpads.unimed.application.atendimento.service.CreateAtendimentoCommand
import com.devpads.unimed.application.atendimento.service.UpdateAtendimentoCommand
import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.infrastructure.web.atendimento.dto.AtendimentoResponse
import com.devpads.unimed.infrastructure.web.atendimento.dto.CreateAtendimentoRequest
import com.devpads.unimed.infrastructure.web.atendimento.dto.UpdateAtendimentoRequest
import com.devpads.unimed.infrastructure.web.atendimento.dto.toResponse
import com.devpads.unimed.infrastructure.web.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/atendimentos")
@Tag(name = "Atendimentos")
class AtendimentoController(
    private val atendimentoService: AtendimentoService,
) {

    @GetMapping
    fun listAtendimentos(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "data_atendimento,desc") sort: List<String>,
    ): PagedResponse<AtendimentoResponse> {
        if (page < 0) {
            throw BadRequestException("page must be greater than or equal to 0")
        }
        if (size < 1) {
            throw BadRequestException("size must be greater than or equal to 1")
        }

        val (sortField, sortDirection) = parseSort(sort.firstOrNull() ?: "data_atendimento,desc")
        val result = atendimentoService.findAll(page, size, sortField, sortDirection)

        return PagedResponse(
            items = result.items.map { it.toResponse() },
            page = result.page,
            size = result.size,
            totalItems = result.totalItems,
            totalPages = result.totalPages,
        )
    }

    @GetMapping("/{id}")
    fun getAtendimentoById(@PathVariable id: Long): AtendimentoResponse {
        val atendimento = atendimentoService.findById(id)
            ?: throw NotFoundException("Atendimento com id=$id não encontrado")
        return atendimento.toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAtendimento(@Valid @RequestBody request: CreateAtendimentoRequest): AtendimentoResponse {
        val command = CreateAtendimentoCommand(
            pacienteId = request.pacienteId,
            dataAtendimento = Instant.parse(request.dataAtendimento),
            medico = request.medico,
            observacoes = request.observacoes,
        )
        val atendimento = atendimentoService.create(command)
        return atendimento.toResponse()
    }

    @PutMapping("/{id}")
    fun updateAtendimento(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAtendimentoRequest,
    ): AtendimentoResponse {
        val command = UpdateAtendimentoCommand(
            pacienteId = request.pacienteId,
            dataAtendimento = Instant.parse(request.dataAtendimento),
            medico = request.medico,
            observacoes = request.observacoes,
        )
        val atendimento = atendimentoService.update(id, command)
        return atendimento.toResponse()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAtendimento(@PathVariable id: Long) {
        atendimentoService.delete(id)
    }

    private fun parseSort(sort: String): Pair<String, String> {
        val parts = sort.split(",", limit = 2)
        val field = parts[0].trim()
        if (field.isBlank()) {
            throw BadRequestException("sort field cannot be empty")
        }
        val direction = parts.getOrElse(1) { "asc" }.trim().lowercase()
        return field to direction
    }
}