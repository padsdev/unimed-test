"use client";

import { useQuery } from "@tanstack/react-query";

import { SimpleTable } from "@/components/clinical/simple-table";
import { SectionFrame } from "@/components/clinical/section-frame";
import { clinicalServices } from "@/services/clinical-services";

export default function PacientesPage() {
  const { data = [] } = useQuery({
    queryKey: ["pacientes"],
    queryFn: clinicalServices.listPacientes,
  });

  return (
    <div className="h-full min-h-0">
      <SectionFrame
        title="Pacientes"
        description="Cadastro assistencial em PostgreSQL com prioridade para leitura clara e operacao segura."
      >
        <SimpleTable
          headers={["ID", "Nome", "CPF", "Telefone", "Email"]}
          rows={data.map((item) => [item.id, item.nome, item.cpf, item.telefone, item.email])}
        />
      </SectionFrame>
    </div>
  );
}
