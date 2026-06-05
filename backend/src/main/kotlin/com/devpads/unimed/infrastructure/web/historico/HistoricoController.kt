package com.devpads.unimed.infrastructure.web.historico

import com.devpads.unimed.application.shared.exception.BadRequestException
import com.devpads.unimed.application.historico.service.HistoricoService
import com.devpads.unimed.infrastructure.web.historico.dto.HistoricoPacienteResponse
import com.devpads.unimed.infrastructure.web.historico.dto.toComProcedimentosResponse
import com.devpads.unimed.infrastructure.web.paciente.dto.toResponse
import com.devpads.unimed.infrastructure.web.procedimento.dto.toResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/historico")
@Tag(name = "Histórico", description = "Histórico consolidado do paciente")
class HistoricoController(
    private val historicoService: HistoricoService,
) {

    @GetMapping
    @Operation(summary = "Obter histórico consolidado do paciente")
    fun getHistorico(
        @PathVariable pacienteId: Long,
        @RequestParam(defaultValue = "desc")
        @Parameter(description = "Ordenação: asc ou desc")
        sortOrder: String,
    ): HistoricoPacienteResponse {
        if (sortOrder.lowercase() !in listOf("asc", "desc")) {
            throw BadRequestException("sortOrder must be 'asc' or 'desc'")
        }

        val historicoData = historicoService.getHistorico(pacienteId, sortOrder)

        if (historicoData.atendimentos.isEmpty()) {
            return HistoricoPacienteResponse(
                paciente = historicoData.paciente.toResponse(),
                atendimentos = emptyList(),
            )
        }

        val procedimentosGrouped = historicoData.procedimentos.groupBy { it.atendimentoId }

        val atendimentosComProcedimentos = historicoData.atendimentos.map { atendimento ->
            atendimento.toComProcedimentosResponse(
                procedimentos = (procedimentosGrouped[atendimento.id] ?: emptyList()).map { it.toResponse() }
            )
        }

        return HistoricoPacienteResponse(
            paciente = historicoData.paciente.toResponse(),
            atendimentos = atendimentosComProcedimentos,
        )
    }
}
