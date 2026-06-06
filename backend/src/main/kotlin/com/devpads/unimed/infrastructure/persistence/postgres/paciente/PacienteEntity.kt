package com.devpads.unimed.infrastructure.persistence.postgres.paciente

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate

@Entity
@Table(name = "pacientes")
data class PacienteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nome: String,

    @Column(length = 11, columnDefinition = "char(11)")
    @JdbcTypeCode(SqlTypes.CHAR)
    val cpf: String,

    @Column(name = "data_nascimento")
    val dataNascimento: LocalDate,

    val telefone: String,

    val email: String,
)
