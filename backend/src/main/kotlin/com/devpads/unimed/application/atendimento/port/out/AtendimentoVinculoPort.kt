package com.devpads.unimed.application.atendimento.port.out

interface AtendimentoVinculoPort {
    fun existsByPacienteId(pacienteId: Long): Boolean
}
