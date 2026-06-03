package com.devpads.unimed.infrastructure.persistence.postgres.paciente

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "pacientes")
data class PacienteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nome: String,

    val cpf: String,

    @Column(name = "data_nascimento")
    val dataNascimento: LocalDate,

    val telefone: String,

    val email: String,

    val status: String = "ativo",
)
