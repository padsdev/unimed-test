import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { clinicalServices } from "@/services/clinical-services";
import type { AtendimentosFilters, CriarAtendimentoRequest, AtualizarAtendimentoRequest } from "@/types/domain";

export function useListAtendimentos(filters: AtendimentosFilters) {
  return useQuery({
    queryKey: ["atendimentos", filters],
    queryFn: () => clinicalServices.atendimentos.list(filters),
  });
}

export function useAtendimento(id: number | null) {
  return useQuery({
    queryKey: ["atendimentos", id],
    queryFn: () => clinicalServices.atendimentos.getById(id!),
    enabled: id !== null,
  });
}

export function useCriarAtendimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CriarAtendimentoRequest) => clinicalServices.atendimentos.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["atendimentos"] }); },
  });
}

export function useAtualizarAtendimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: AtualizarAtendimentoRequest }) =>
      clinicalServices.atendimentos.update(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["atendimentos"] }); },
  });
}

export function useExcluirAtendimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => clinicalServices.atendimentos.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["atendimentos"] }); },
  });
}
