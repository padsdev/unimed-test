package com.devpads.unimed.infrastructure.persistence.mysql.atendimento

import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.infrastructure.config.MysqlJdbcConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true")
class AtendimentoVinculoMysqlAdapter(
    private val mysqlJdbcConnectionFactory: MysqlJdbcConnectionFactory,
) : AtendimentoVinculoPort {

    override fun existsByPacienteId(pacienteId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM atendimentos WHERE paciente_id = ?"
        mysqlJdbcConnectionFactory.getConnection().use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, pacienteId)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) rs.getInt(1) > 0 else false
                }
            }
        }
    }
}
