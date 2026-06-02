export function SimpleTable({
  headers,
  rows,
}: {
  headers: string[];
  rows: (string | number)[][];
}) {
  return (
    <div className="max-h-[420px] overflow-auto rounded-xl border border-[color:var(--border-200)]">
      <table className="w-full min-w-[560px] border-collapse">
        <thead className="bg-[color:var(--surface-2)] text-left text-sm text-[color:var(--text-700)]">
          <tr>
            {headers.map((header) => (
              <th key={header} className="px-3 py-2.5 font-semibold">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex} className="border-t border-[color:var(--border-200)] text-sm text-[color:var(--text-700)]">
              {row.map((cell, cellIndex) => (
                <td key={cellIndex} className="px-3 py-2.5">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
