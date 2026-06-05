package com.devpads.unimed.infrastructure.web.procedimento

import com.devpads.unimed.application.procedimento.service.CreateProcedimentoCommand
import com.devpads.unimed.application.procedimento.service.ProcedimentoService
import com.devpads.unimed.application.procedimento.service.UpdateProcedimentoCommand
import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.infrastructure.web.procedimento.dto.CreateProcedimentoRequest
import com.devpads.unimed.infrastructure.web.procedimento.dto.ProcedimentoResponse
import com.devpads.unimed.infrastructure.web.procedimento.dto.UpdateProcedimentoRequest
import com.devpads.unimed.infrastructure.web.procedimento.dto.toResponse
import com.devpads.unimed.infrastructure.web.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/procedimentos")
@Tag(name = "Procedimentos")
class ProcedimentoController(
    private val procedimentoService: ProcedimentoService,
) {

    @GetMapping
    fun listProcedimentos(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "nome,asc") sort: List<String>,
    ): PagedResponse<ProcedimentoResponse> {
        if (page < 0) {
            throw BadRequestException("page must be greater than or equal to 0")
        }
        if (size < 1) {
            throw BadRequestException("size must be greater than or equal to 1")
        }

        val (sortField, sortDirection) = parseSort(sort.firstOrNull() ?: "nome,asc")
        val result = procedimentoService.findAll(page, size, sortField, sortDirection)

        return PagedResponse(
            items = result.items.map { it.toResponse() },
            page = result.page,
            size = result.size,
            totalItems = result.totalItems,
            totalPages = result.totalPages,
        )
    }

    @GetMapping("/{id}")
    fun getProcedimentoById(@PathVariable id: Long): ProcedimentoResponse {
        val procedimento = procedimentoService.findById(id)
            ?: throw NotFoundException("Procedimento com id=$id não encontrado")
        return procedimento.toResponse()
    }

    @PostMapping
    fun createProcedimento(@Valid @RequestBody request: CreateProcedimentoRequest): ProcedimentoResponse {
        val command = CreateProcedimentoCommand(
            atendimentoId = request.atendimentoId,
            nome = request.nome,
            valor = request.valor,
        )
        val procedimento = procedimentoService.create(command)
        return procedimento.toResponse()
    }

    @PutMapping("/{id}")
    fun updateProcedimento(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProcedimentoRequest,
    ): ProcedimentoResponse {
        val command = UpdateProcedimentoCommand(
            atendimentoId = request.atendimentoId,
            nome = request.nome,
            valor = request.valor,
        )
        val procedimento = procedimentoService.update(id, command)
        return procedimento.toResponse()
    }

    @DeleteMapping("/{id}")
    fun deleteProcedimento(@PathVariable id: Long) {
        procedimentoService.delete(id)
    }

    @GetMapping("/por-atendimento/{atendimentoId}")
    fun listProcedimentosByAtendimento(@PathVariable atendimentoId: Long): List<ProcedimentoResponse> {
        return procedimentoService.findByAtendimentoId(atendimentoId).map { it.toResponse() }
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