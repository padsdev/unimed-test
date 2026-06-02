import { useMemo, useRef, useState, useEffect } from "react";

interface ComboboxOption {
  value: number;
  label: string;
  subtitle?: string;
}

interface ComboboxProps {
  options: ComboboxOption[];
  value: number | null;
  onChange: (value: number | null) => void;
  placeholder?: string;
  emptyMessage?: string;
}

export function Combobox({ options, value, onChange, placeholder = "Selecione...", emptyMessage = "Nenhum resultado" }: ComboboxProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  const filtered = useMemo(() => {
    if (!query) return options;
    const lower = query.toLowerCase();
    return options.filter((o) => o.label.toLowerCase().includes(lower));
  }, [options, query]);

  const selected = options.find((o) => o.value === value);

  return (
    <div className="relative" ref={containerRef}>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="flex w-full cursor-pointer items-center justify-between rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-left text-sm text-[color:var(--text-700)] hover:border-[color:var(--brand-500)]"
      >
        <span className={selected ? "" : "text-[color:var(--text-400)]"}>
          {selected ? selected.label : placeholder}
        </span>
        <span className="text-xs text-[color:var(--text-400)]">▾</span>
      </button>

      {open && (
        <div className="absolute z-50 mt-1 w-full rounded-xl border border-border bg-card shadow-lg">
          <div className="border-b border-border p-2">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Pesquisar..."
              className="w-full rounded-lg border border-border bg-[color:var(--surface-2)] px-3 py-1.5 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
              autoFocus
            />
          </div>
          <div className="max-h-52 overflow-auto py-1">
            {filtered.length === 0 ? (
              <p className="px-3 py-2 text-sm text-[color:var(--text-400)]">{emptyMessage}</p>
            ) : (
              filtered.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => { onChange(option.value); setOpen(false); setQuery(""); }}
                  className={`flex w-full cursor-pointer flex-col px-3 py-2 text-left text-sm hover:bg-[color:var(--surface-2)] ${
                    option.value === value ? "bg-[color:var(--brand-500)]/10 font-medium text-[color:var(--brand-700)]" : "text-[color:var(--text-700)]"
                  }`}
                >
                  <span>{option.label}</span>
                  {option.subtitle && (
                    <span className="text-xs text-[color:var(--text-400)]">{option.subtitle}</span>
                  )}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
