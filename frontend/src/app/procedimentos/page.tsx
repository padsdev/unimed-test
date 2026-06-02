"use client";

import { useQuery } from "@tanstack/react-query";

import { SimpleTable } from "@/components/clinical/simple-table";
import { SectionFrame } from "@/components/clinical/section-frame";
import { clinicalServices } from "@/services/clinical-services";

export default function ProcedimentosPage() {
  const { data = [] } = useQuery({
    queryKey: ["procedimentos"],
    queryFn: clinicalServices.listProcedimentos,
  });

  return (
    <div className="h-full min-h-0">
      <SectionFrame
        title="Procedimentos"
        description="Catalogo de procedimentos com valores e vinculo ao atendimento para consolidacao de historico."
      >
        <SimpleTable
          headers={["ID", "Atendimento", "Nome", "Valor (R$)"]}
          rows={data.map((item) => [item.id, item.atendimentoId, item.nome, item.valor.toFixed(2)])}
        />
      </SectionFrame>
    </div>
  );
}
