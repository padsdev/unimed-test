package com.devpads.unimed.infrastructure.persistence.mysql.procedimento

import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.domain.procedimento.model.Procedimento
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.math.BigDecimal
import javax.sql.DataSource

@Repository
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true")
class ProcedimentoMysqlAdapter(
    private val mysqlDataSource: DataSource,
) : ProcedimentoRepositoryPort {

    private val allowedSortFields = setOf("id", "atendimento_id", "nome", "valor")

    override fun findById(id: Long): Procedimento? {
        val sql = "SELECT id, atendimento_id, nome, valor FROM procedimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) mapRow(rs) else null
                }
            }
        }
    }

    override fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, atendimentoId: Long?): PagedResult<Procedimento> {
        val safeField = if (sortField in allowedSortFields) sortField else "nome"
        val safeDir = if (sortDirection.lowercase() == "desc") "DESC" else "ASC"
        val offset = page * size
        val whereClause = if (atendimentoId != null) " WHERE atendimento_id = ?" else ""

        val countSql = "SELECT COUNT(*) FROM procedimentos$whereClause"
        val totalItems: Long = mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(countSql).use { stmt ->
                if (atendimentoId != null) stmt.setLong(1, atendimentoId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }
        }

        val dataSql = "SELECT id, atendimento_id, nome, valor FROM procedimentos$whereClause ORDER BY $safeField $safeDir LIMIT ? OFFSET ?"
        val items = mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(dataSql).use { stmt ->
                var idx = 1
                if (atendimentoId != null) { stmt.setLong(idx, atendimentoId); idx++ }
                stmt.setInt(idx, size)
                stmt.setInt(idx + 1, offset)
                stmt.executeQuery().use { rs ->
                    val list = mutableListOf<Procedimento>()
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

    override fun save(procedimento: Procedimento): Procedimento {
        if (procedimento.id == null) {
            val sql = "INSERT INTO procedimentos (atendimento_id, nome, valor) VALUES (?, ?, ?)"
            mysqlDataSource.connection.use { conn ->
                conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setLong(1, procedimento.atendimentoId)
                    stmt.setString(2, procedimento.nome)
                    stmt.setBigDecimal(3, procedimento.valor)
                    stmt.executeUpdate()
                    stmt.generatedKeys.use { keys ->
                        keys.next()
                        return Procedimento(
                            id = keys.getLong(1),
                            atendimentoId = procedimento.atendimentoId,
                            nome = procedimento.nome,
                            valor = procedimento.valor,
                        )
                    }
                }
            }
        } else {
            val sql = "UPDATE procedimentos SET atendimento_id = ?, nome = ?, valor = ? WHERE id = ?"
            mysqlDataSource.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, procedimento.atendimentoId)
                    stmt.setString(2, procedimento.nome)
                    stmt.setBigDecimal(3, procedimento.valor)
                    stmt.setLong(4, procedimento.id)
                    stmt.executeUpdate()
                    return procedimento
                }
            }
        }
    }

    override fun deleteById(id: Long) {
        val sql = "DELETE FROM procedimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeUpdate()
            }
        }
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM procedimentos WHERE id = ?"
        mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, id)
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) rs.getInt(1) > 0 else false
                }
            }
        }
    }

    override fun findByAtendimentoId(atendimentoId: Long): List<Procedimento> {
        val sql = "SELECT id, atendimento_id, nome, valor FROM procedimentos WHERE atendimento_id = ? ORDER BY id"
        return mysqlDataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, atendimentoId)
                stmt.executeQuery().use { rs ->
                    val list = mutableListOf<Procedimento>()
                    while (rs.next()) {
                        list.add(mapRow(rs))
                    }
                    list
                }
            }
        }
    }

    private fun mapRow(rs: ResultSet): Procedimento {
        val valor = rs.getBigDecimal("valor") ?: BigDecimal.ZERO
        return Procedimento(
            id = rs.getLong("id"),
            atendimentoId = rs.getLong("atendimento_id"),
            nome = rs.getString("nome"),
            valor = valor,
        )
    }
}