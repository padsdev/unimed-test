#!/usr/bin/env bash
set -euo pipefail

API_BASE="http://localhost:8080/api"
PASS=0
FAIL=0

assert() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    echo "  PASS: $label (HTTP $actual)"
    ((PASS++))
  else
    echo "  FAIL: $label (expected $expected, got $actual)"
    ((FAIL++))
  fi
}

assert_contains() {
  local label="$1" expected="$2" output="$3"
  if echo "$output" | grep -q "$expected"; then
    echo "  PASS: $label"
    ((PASS++))
  else
    echo "  FAIL: $label (expected to contain '$expected')"
    ((FAIL++))
  fi
}

echo "===== Smoke Test ====="
echo ""

echo "--- Build and start ---"
if ! docker compose up --build -d 2>/dev/null; then
  echo "  WARN: docker compose up failed (common on restricted Linux hosts)."
  echo "  Run 'docker compose up --build -d' manually and retry."
  exit 1
fi

echo ""
echo "--- Wait for backend (up to 120s) ---"
for i in $(seq 1 24); do
  if curl -sf "$API_BASE/pacientes?page=0&size=1" > /dev/null 2>&1; then
    echo "  Backend ready after ${i}x5s"
    break
  fi
  if [ "$i" -eq 24 ]; then
    echo "  FAIL: backend not ready after 120s"
    docker compose down -v
    exit 1
  fi
  sleep 5
done

echo ""
echo "--- 1. Health / list pacientes ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/pacientes?page=0&size=1")
assert "GET /pacientes" "200" "$STATUS"

echo ""
echo "--- 2. Create paciente ---"
CREATE_RESP=$(curl -sf -w "%{http_code}" -o /tmp/smoke_create.json -X POST \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Teste","cpf":"00000000001","dataNascimento":"1990-05-20","telefone":"11999999999","email":"maria@teste.com"}' \
  "$API_BASE/pacientes")
assert "POST /pacientes" "201" "$CREATE_RESP"
PACIENTE_ID=$(python3 -c "import json; print(json.load(open('/tmp/smoke_create.json'))['id'])" 2>/dev/null || echo "0")

echo ""
echo "--- 3. Get paciente by id ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/pacientes/$PACIENTE_ID")
assert "GET /pacientes/{id}" "200" "$STATUS"

echo ""
echo "--- 4. Create atendimento ---"
DATA_ATENDIMENTO="2026-06-01T10:00:00Z"
ATEND_RESP=$(curl -sf -w "%{http_code}" -o /tmp/smoke_atend.json -X POST \
  -H "Content-Type: application/json" \
  -d "{\"pacienteId\":$PACIENTE_ID,\"dataAtendimento\":\"$DATA_ATENDIMENTO\",\"medico\":\"Dr. Carlos\",\"observacoes\":\"Consulta\"}" \
  "$API_BASE/atendimentos")
assert "POST /atendimentos" "201" "$ATEND_RESP"

echo ""
echo "--- 5. List atendimentos ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/atendimentos?page=0&size=10")
assert "GET /atendimentos" "200" "$STATUS"

echo ""
echo "--- 6. Create procedimento ---"
ATEND_ID=$(python3 -c "import json; print(json.load(open('/tmp/smoke_atend.json'))['id'])" 2>/dev/null || echo "0")
PROC_RESP=$(curl -sf -w "%{http_code}" -o /tmp/smoke_proc.json -X POST \
  -H "Content-Type: application/json" \
  -d "{\"atendimentoId\":$ATEND_ID,\"nome\":\"Eletrocardiograma\",\"valor\":150.0}" \
  "$API_BASE/procedimentos")
assert "POST /procedimentos" "201" "$PROC_RESP"

echo ""
echo "--- 7. Get historico ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/pacientes/$PACIENTE_ID/historico")
assert "GET /pacientes/{id}/historico" "200" "$STATUS"
HIST_OUT=$(curl -sf "$API_BASE/pacientes/$PACIENTE_ID/historico" 2>/dev/null || echo "")
assert_contains "historico contains paciente id" "\"id\": $PACIENTE_ID" "$HIST_OUT"

echo ""
echo "--- 8. Delete paciente (should fail 409 if vinculos exist) ---"
if [ "$PACIENTE_ID" != "0" ]; then
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_BASE/pacientes/$PACIENTE_ID")
  if [ "$STATUS" = "409" ] || [ "$STATUS" = "404" ]; then
    echo "  PASS: DELETE /pacientes/{id} returned $STATUS (expected 409/404)"
    ((PASS++))
  else
    echo "  FAIL: DELETE /pacientes/{id} (expected 409/404, got $STATUS)"
    ((FAIL++))
  fi
else
  echo "  SKIP: DELETE /pacientes/{id} (no paciente created)"
fi

echo ""
echo "--- Cleanup ---"
docker compose down -v

echo ""
echo "===== Results: $PASS passed, $FAIL failed ====="
exit $FAIL
