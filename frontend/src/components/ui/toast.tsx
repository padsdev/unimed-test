"use client";

import { createContext, useCallback, useContext, useState } from "react";

export interface ToastMessage {
  id: string;
  type: "success" | "error" | "info";
  title: string;
  description?: string;
}

interface ToastContextValue {
  toasts: ToastMessage[];
  addToast: (toast: Omit<ToastMessage, "id">) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const addToast = useCallback((toast: Omit<ToastMessage, "id">) => {
    const id = crypto.randomUUID();
    setToasts((prev) => [...prev, { ...toast, id }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, addToast, removeToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="alert"
            className={`animate-in slide-in-from-right flex w-80 items-start gap-3 rounded-xl border p-4 shadow-lg ${
              toast.type === "success"
                ? "border-[color:var(--success-500)]/30 bg-[color:var(--surface-1)]"
                : toast.type === "error"
                  ? "border-[color:var(--danger-500)]/30 bg-[color:var(--surface-1)]"
                  : "border-[color:var(--brand-500)]/30 bg-[color:var(--surface-1)]"
            }`}
          >
            <div className="flex-1">
              <p className="text-sm font-semibold text-[color:var(--text-900)]">{toast.title}</p>
              {toast.description && (
                <p className="mt-0.5 text-xs text-[color:var(--text-500)]">{toast.description}</p>
              )}
            </div>
            <button
              onClick={() => removeToast(toast.id)}
              className="text-xs text-[color:var(--text-400)] hover:text-[color:var(--text-700)]"
            >
              ✕
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within ToastProvider");
  return ctx;
}
