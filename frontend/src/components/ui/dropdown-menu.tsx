import { useRef, useState, useEffect } from "react";

import { cn } from "@/lib/utils";

interface DropdownItem {
  label: string;
  onClick: () => void;
  variant?: "default" | "danger";
}

interface DropdownMenuProps {
  items: DropdownItem[];
  trigger: React.ReactNode;
  align?: "left" | "right";
}

export function DropdownMenu({ items, trigger, align = "right" }: DropdownMenuProps) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    if (open) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  return (
    <div className="relative inline-block" ref={menuRef}>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="cursor-pointer rounded-lg p-1 text-[color:var(--text-400)] hover:bg-[color:var(--surface-2)] hover:text-[color:var(--text-700)]"
      >
        {trigger}
      </button>
      {open && (
        <div
          className={cn(
            "absolute z-50 mt-1 min-w-36 rounded-xl border border-border bg-card py-1 shadow-lg",
            align === "right" ? "right-0" : "left-0",
          )}
        >
          {items.map((item, i) => (
            <button
              key={i}
              type="button"
              onClick={() => { item.onClick(); setOpen(false); }}
              className={cn(
                "flex w-full cursor-pointer items-center px-3 py-2 text-left text-sm transition-colors",
                item.variant === "danger"
                  ? "text-[color:var(--danger-500)] hover:bg-[color:var(--danger-500)]/10"
                  : "text-[color:var(--text-700)] hover:bg-[color:var(--surface-2)]",
              )}
            >
              {item.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
