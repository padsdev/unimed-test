import { useQuery } from "@tanstack/react-query";

import { clinicalServices } from "@/services/clinical-services";

export function useClinicalOverview() {
  const pacientes = useQuery({
    queryKey: ["pacientes"],
    queryFn: clinicalServices.listPacientes,
  });
  const atendimentos = useQuery({
    queryKey: ["atendimentos"],
    queryFn: clinicalServices.listAtendimentos,
  });
  const procedimentos = useQuery({
    queryKey: ["procedimentos"],
    queryFn: clinicalServices.listProcedimentos,
  });

  return {
    pacientes,
    atendimentos,
    procedimentos,
    isLoading: pacientes.isLoading || atendimentos.isLoading || procedimentos.isLoading,
    isError: pacientes.isError || atendimentos.isError || procedimentos.isError,
  };
}
