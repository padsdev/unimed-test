import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { clinicalServices } from "@/services/clinical-services";
import type { CriarPacienteRequest, AtualizarPacienteRequest, PacientesFilters } from "@/types/domain";

export function useListPacientes(filters: PacientesFilters) {
  return useQuery({
    queryKey: ["pacientes", filters],
    queryFn: () => clinicalServices.pacientes.list(filters),
  });
}

export function usePaciente(id: number | null) {
  return useQuery({
    queryKey: ["pacientes", id],
    queryFn: () => clinicalServices.pacientes.getById(id!),
    enabled: id !== null,
  });
}

export function useCriarPaciente() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CriarPacienteRequest) => clinicalServices.pacientes.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["pacientes"] }); },
  });
}

export function useAtualizarPaciente() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: AtualizarPacienteRequest }) =>
      clinicalServices.pacientes.update(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["pacientes"] }); },
  });
}

export function useExcluirPaciente() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => clinicalServices.pacientes.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["pacientes"] }); },
  });
}
