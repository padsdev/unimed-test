import { useEffect, useRef } from "react";

import { cn } from "@/lib/utils";

interface DialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "danger" | "default";
  loading?: boolean;
}

export function Dialog({
  open,
  onClose,
  onConfirm,
  title,
  description,
  confirmLabel = "Confirmar",
  cancelLabel = "Cancelar",
  variant = "default",
  loading = false,
}: DialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const el = dialogRef.current;
    if (!el) return;
    if (open && !el.open) {
      el.showModal();
    } else if (!open && el.open) {
      el.close();
    }
  }, [open]);

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      role="dialog"
      aria-modal="true"
      className="backdrop:bg-black/30 max-w-sm rounded-2xl border border-border bg-card p-6 shadow-xl"
      onClose={onClose}
      onClick={(e) => { if (e.target === dialogRef.current) onClose(); }}
    >
      <h2 className="text-lg font-bold text-[color:var(--text-900)]">{title}</h2>
      <p className="mt-2 text-sm text-[color:var(--text-600)]">{description}</p>
      <div className="mt-6 flex justify-end gap-3">
        <button
          onClick={onClose}
          disabled={loading}
          className="cursor-pointer rounded-lg border border-border bg-[color:var(--surface-2)] px-4 py-2 text-sm font-medium text-[color:var(--text-700)] hover:bg-[color:var(--surface-3)] disabled:opacity-50"
        >
          {cancelLabel}
        </button>
        <button
          onClick={onConfirm}
          disabled={loading}
          className={cn(
            "cursor-pointer rounded-lg px-4 py-2 text-sm font-medium text-white disabled:opacity-50",
            variant === "danger" ? "bg-[color:var(--danger-500)]" : "bg-[color:var(--brand-500)]",
          )}
        >
          {loading ? "Aguarde..." : confirmLabel}
        </button>
      </div>
    </dialog>
  );
}
