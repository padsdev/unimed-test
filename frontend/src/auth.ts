export interface SessionUser {
  id: string;
  nome: string;
  perfil: "assistencial" | "administrativo";
}

export async function getSessionUser(): Promise<SessionUser | null> {
  return {
    id: "mock-user",
    nome: "Operador Clinico",
    perfil: "assistencial",
  };
}
