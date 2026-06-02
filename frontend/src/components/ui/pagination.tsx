import { cn } from "@/lib/utils";

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: (number | "ellipsis")[] = [];
  const maxVisible = 5;

  if (totalPages <= maxVisible + 2) {
    for (let i = 0; i < totalPages; i++) pages.push(i);
  } else {
    pages.push(0);
    const start = Math.max(1, page - 1);
    const end = Math.min(totalPages - 2, page + 1);
    if (start > 1) pages.push("ellipsis");
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < totalPages - 2) pages.push("ellipsis");
    pages.push(totalPages - 1);
  }

  return (
    <nav className="flex items-center justify-center gap-1" aria-label="Paginação">
      <button
        type="button"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        className="cursor-pointer rounded-lg px-2 py-1.5 text-sm text-[color:var(--text-500)] hover:bg-[color:var(--surface-2)] disabled:opacity-30 disabled:cursor-not-allowed"
      >
        Anterior
      </button>
      {pages.map((p, i) =>
        p === "ellipsis" ? (
          <span key={`e-${i}`} className="px-1 text-sm text-[color:var(--text-400)]">...</span>
        ) : (
          <button
            key={p}
            type="button"
            onClick={() => onPageChange(p)}
            className={cn(
              "cursor-pointer rounded-lg px-2.5 py-1.5 text-sm font-medium transition-colors",
              p === page
                ? "bg-[color:var(--brand-500)] text-white"
                : "text-[color:var(--text-600)] hover:bg-[color:var(--surface-2)]",
            )}
          >
            {p + 1}
          </button>
        ),
      )}
      <button
        type="button"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        className="cursor-pointer rounded-lg px-2 py-1.5 text-sm text-[color:var(--text-500)] hover:bg-[color:var(--surface-2)] disabled:opacity-30 disabled:cursor-not-allowed"
      >
        Próximo
      </button>
    </nav>
  );
}
