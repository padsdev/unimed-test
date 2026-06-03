package com.devpads.unimed.infrastructure.persistence.postgres.paciente

import org.springframework.data.jpa.repository.JpaRepository

interface PacienteJpaRepository : JpaRepository<PacienteEntity, Long>
