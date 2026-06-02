"use client";

import { useQuery } from "@tanstack/react-query";

import { SimpleTable } from "@/components/clinical/simple-table";
import { SectionFrame } from "@/components/clinical/section-frame";
import { clinicalServices } from "@/services/clinical-services";

export default function AtendimentosPage() {
  const { data = [] } = useQuery({
    queryKey: ["atendimentos"],
    queryFn: clinicalServices.listAtendimentos,
  });

  return (
    <div className="h-full min-h-0">
      <SectionFrame
        title="Atendimentos"
        description="Registro operacional em MySQL (JDBC no backend) com timeline de consultas e observacoes clinicas."
      >
        <SimpleTable
          headers={["ID", "Paciente", "Data", "Medico", "Observacoes"]}
          rows={data.map((item) => [item.id, item.pacienteId, item.dataAtendimento, item.medico, item.observacoes])}
        />
      </SectionFrame>
    </div>
  );
}
