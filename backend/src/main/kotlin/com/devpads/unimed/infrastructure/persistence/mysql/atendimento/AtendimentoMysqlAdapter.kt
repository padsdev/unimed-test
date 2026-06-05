package com.devpads.unimed.infrastructure.persistence.mysql.atendimento

import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

@Repository
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true")
class AtendimentoMysqlAdapter(
    private val mysqlDataSource: DataSource,
) : AtendimentoRepositoryPort {

    private val allowedSortFields = setOf("id", "paciente_id", "data_atendimento", "medico")

    override fun findById(id: Long): Atendimento? {
        val sql = "SELECT id, paciente_id, data_atendimento, medico, observacoes FROM atendimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) mapRow(rs) else null
                }
            }
        }
    }

    override fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, pacienteId: Long?): PagedResult<Atendimento> {
        val safeField = if (sortField in allowedSortFields) sortField else "data_atendimento"
        val safeDir = if (sortDirection.lowercase() == "desc") "DESC" else "ASC"
        val offset = page * size
        val whereClause = if (pacienteId != null) " WHERE paciente_id = ?" else ""

        val countSql = "SELECT COUNT(*) FROM atendimentos$whereClause"
        val totalItems: Long = mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(countSql).use { stmt ->
                if (pacienteId != null) stmt.setLong(1, pacienteId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }
        }

        val dataSql = "SELECT id, paciente_id, data_atendimento, medico, observacoes FROM atendimentos$whereClause ORDER BY $safeField $safeDir LIMIT ? OFFSET ?"
        val items = mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(dataSql).use { stmt ->
                var idx = 1
                if (pacienteId != null) { stmt.setLong(idx, pacienteId); idx++ }
                stmt.setInt(idx, size)
                stmt.setInt(idx + 1, offset)
                stmt.executeQuery().use { rs ->
                    val list = mutableListOf<Atendimento>()
                    while (rs.next()) {
                        list.add(mapRow(rs))
                    }
                    list
                }
            }
        }

        val totalPages = if (size > 0) ((totalItems + size - 1) / size).toInt() else 0

        return PagedResult(items, page, size, totalItems, totalPages)
    }

    override fun save(atendimento: Atendimento): Atendimento {
        val localDateTime = atendimento.dataAtendimento.atZone(ZoneOffset.UTC).toLocalDateTime()

        if (atendimento.id == null) {
            val sql = "INSERT INTO atendimentos (paciente_id, data_atendimento, medico, observacoes) VALUES (?, ?, ?, ?)"
            mysqlDataSource.connection.use { conn ->
                conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setLong(1, atendimento.pacienteId)
                    stmt.setObject(2, localDateTime)
                    stmt.setString(3, atendimento.medico)
                    stmt.setString(4, atendimento.observacoes)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { keys ->
                        keys.next()
                        return Atendimento(
                            id = keys.getLong(1),
                            pacienteId = atendimento.pacienteId,
                            dataAtendimento = atendimento.dataAtendimento,
                            medico = atendimento.medico,
                            observacoes = atendimento.observacoes,
                        )
                    }
                }
            }
        } else {
            val sql = "UPDATE atendimentos SET paciente_id = ?, data_atendimento = ?, medico = ?, observacoes = ? WHERE id = ?"
            mysqlDataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, atendimento.pacienteId)
                    stmt.setObject(2, localDateTime)
                    stmt.setString(3, atendimento.medico)
                    stmt.setString(4, atendimento.observacoes)
                    stmt.setLong(5, atendimento.id)
                    stmt.executeUpdate()
                    return atendimento
                }
            }
        }
    }

    override fun deleteById(id: Long) {
        val sql = "DELETE FROM atendimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeUpdate()
            }
        }
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM atendimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) rs.getInt(1) > 0 else false
                }
            }
        }
    }

    private fun mapRow(rs: ResultSet): Atendimento {
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
}