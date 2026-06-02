import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { clinicalServices } from "@/services/clinical-services";
import type { ProcedimentosFilters, CriarProcedimentoRequest, AtualizarProcedimentoRequest } from "@/types/domain";

export function useListProcedimentos(filters: ProcedimentosFilters) {
  return useQuery({
    queryKey: ["procedimentos", filters],
    queryFn: () => clinicalServices.procedimentos.list(filters),
  });
}

export function useProcedimento(id: number | null) {
  return useQuery({
    queryKey: ["procedimentos", id],
    queryFn: () => clinicalServices.procedimentos.getById(id!),
    enabled: id !== null,
  });
}

export function useCriarProcedimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CriarProcedimentoRequest) => clinicalServices.procedimentos.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["procedimentos"] }); },
  });
}

export function useAtualizarProcedimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: AtualizarProcedimentoRequest }) =>
      clinicalServices.procedimentos.update(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["procedimentos"] }); },
  });
}

export function useExcluirProcedimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => clinicalServices.procedimentos.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["procedimentos"] }); },
  });
}
