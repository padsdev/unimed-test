package com.devpads.unimed.infrastructure.web.paciente

import com.devpads.unimed.application.paciente.service.CreatePacienteCommand
import com.devpads.unimed.application.paciente.service.PacienteService
import com.devpads.unimed.application.paciente.service.UpdatePacienteCommand
import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.infrastructure.web.paciente.dto.CreatePacienteRequest
import com.devpads.unimed.infrastructure.web.paciente.dto.PacienteResponse
import com.devpads.unimed.infrastructure.web.paciente.dto.UpdatePacienteRequest
import com.devpads.unimed.infrastructure.web.paciente.dto.toResponse
import com.devpads.unimed.infrastructure.web.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes")
class PacienteController(
    private val pacienteService: PacienteService,
) {

    @GetMapping
    fun listPacientes(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "nome,asc") sort: String,
    ): PagedResponse<PacienteResponse> {
        if (page < 0) {
            throw BadRequestException("page must be greater than or equal to 0")
        }
        if (size < 1) {
            throw BadRequestException("size must be greater than or equal to 1")
        }

        val pageable = PageRequest.of(page, size, toSort(sort))
        val pacientesPage = pacienteService.findAll(pageable)

        return PagedResponse(
            items = pacientesPage.content.map { it.toResponse() },
            page = pacientesPage.number,
            size = pacientesPage.size,
            totalItems = pacientesPage.totalElements,
            totalPages = pacientesPage.totalPages,
        )
    }

    @GetMapping("/{id}")
    fun getPacienteById(@PathVariable id: Long): PacienteResponse {
        val paciente = pacienteService.findById(id)
            ?: throw NotFoundException("Paciente com id=$id nao encontrado")

        return paciente.toResponse()
    }

    @PostMapping
    fun createPaciente(@Valid @RequestBody request: CreatePacienteRequest): PacienteResponse {
        val command = CreatePacienteCommand(
            nome = request.nome,
            cpf = request.cpf,
            dataNascimento = LocalDate.parse(request.dataNascimento),
            telefone = request.telefone,
            email = request.email,
        )
        val paciente = pacienteService.create(command)
        return paciente.toResponse()
    }

    @PutMapping("/{id}")
    fun updatePaciente(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePacienteRequest,
    ): PacienteResponse {
        val command = UpdatePacienteCommand(
            nome = request.nome,
            dataNascimento = LocalDate.parse(request.dataNascimento),
            telefone = request.telefone,
            email = request.email,
        )
        val paciente = pacienteService.update(id, command)
        return paciente.toResponse()
    }

    @DeleteMapping("/{id}")
    fun deletePaciente(@PathVariable id: Long) {
        pacienteService.delete(id)
    }

    private fun toSort(value: String): Sort {
        val parts = value.split(",", limit = 2)
        val property = parts[0].trim()
        if (property.isBlank()) {
            throw BadRequestException("sort field cannot be empty")
        }

        val direction = parts.getOrElse(1) { "asc" }.trim().lowercase()
        val order = when (direction) {
            "asc" -> Sort.Order.asc(property)
            "desc" -> Sort.Order.desc(property)
            else -> throw BadRequestException("sort direction must be asc or desc")
        }

        return Sort.by(order)
    }
}
