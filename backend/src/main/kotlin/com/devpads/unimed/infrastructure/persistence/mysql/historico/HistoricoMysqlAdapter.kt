package com.devpads.unimed.infrastructure.persistence.mysql.historico

import com.devpads.unimed.application.historico.port.out.HistoricoRepositoryPort
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

@Repository
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true")
class HistoricoMysqlAdapter(
    private val mysqlDataSource: DataSource,
) : HistoricoRepositoryPort {

    override fun findAtendimentosByPacienteId(pacienteId: Long, sortDirection: String): List<Atendimento> {
        val safeDir = if (sortDirection.lowercase() == "asc") "ASC" else "DESC"
        val sql = "SELECT id, paciente_id, data_atendimento, medico, observacoes FROM atendimentos WHERE paciente_id = ? ORDER BY data_atendimento $safeDir, id $safeDir"
        return mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, pacienteId)
                stmt.executeQuery().use { rs ->
                    val list = mutableListOf<Atendimento>()
                    while (rs.next()) {
                        list.add(mapRowAtendimento(rs))
                    }
                    list
                }
            }
        }
    }

    override fun findProcedimentosByAtendimentoIds(atendimentoIds: List<Long>): List<Procedimento> {
        if (atendimentoIds.isEmpty()) return emptyList()

        val placeholders = atendimentoIds.joinToString(", ") { "?" }
        val sql = "SELECT id, atendimento_id, nome, valor FROM procedimentos WHERE atendimento_id IN ($placeholders) ORDER BY id"
        return mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                atendimentoIds.forEachIndexed { index, id ->
                    stmt.setLong(index + 1, id)
                }
                stmt.executeQuery().use { rs ->
                    val list = mutableListOf<Procedimento>()
                    while (rs.next()) {
                        list.add(mapRowProcedimento(rs))
                    }
                    list
                }
            }
        }
    }

    private fun mapRowAtendimento(rs: ResultSet): Atendimento {
        val localDateTime = rs.getObject("data_atendimento", LocalDateTime::class.java)
        val instant = localDateTime.atZone(ZoneOffset.UTC).toInstant()
        return Atendimento(
            id = rs.getLong("id"),
            pacienteId = rs.getLong("paciente_id"),
            dataAtendimento = instant,
            medico = rs.getString("medico"),
            observacoes = rs.getString("observacoes"),
        )
    }

    private fun mapRowProcedimento(rs: ResultSet): Procedimento {
        val valor = rs.getBigDecimal("valor") ?: BigDecimal.ZERO
        return Procedimento(
            id = rs.getLong("id"),
            atendimentoId = rs.getLong("atendimento_id"),
            nome = rs.getString("nome"),
            valor = valor,
        )
    }
}
