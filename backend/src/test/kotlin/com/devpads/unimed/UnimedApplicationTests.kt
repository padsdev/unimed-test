package com.devpads.unimed

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
	properties = [
		"infra.mysql.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
	],
)
class UnimedApplicationTests {

	@MockitoBean
	lateinit var pacienteRepositoryPort: PacienteRepositoryPort

	@MockitoBean
	lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

	@MockitoBean
	lateinit var atendimentoRepositoryPort: AtendimentoRepositoryPort

	@MockitoBean
	lateinit var procedimentoRepositoryPort: ProcedimentoRepositoryPort

	@Test
	fun contextLoads() {
	}

}
