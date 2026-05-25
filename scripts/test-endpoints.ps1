$base = "http://localhost:8080/api"
$passed = 0
$failed = 0

function Test-Case {
    param($Name, $ScriptBlock)
    try {
        & $ScriptBlock
        Write-Host "[OK] $Name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $Name" -ForegroundColor Red
        Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) { Write-Host "  $($_.ErrorDetails.Message)" }
        $script:failed++
    }
}

Write-Host "`n=== PRUEBAS API BANK-SOFKA ===`n" -ForegroundColor Cyan

# --- CLIENTES ---
Test-Case "GET /clientes -> 200 y 3 registros PDF" {
    $r = Invoke-WebRequest -Uri "$base/clientes" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "Status $($r.StatusCode)" }
    $data = $r.Content | ConvertFrom-Json
    if ($data.Count -ne 3) { throw "Esperados 3 clientes, hay $($data.Count)" }
}

Test-Case "GET /clientes/1 -> Jose Lema" {
    $r = Invoke-WebRequest -Uri "$base/clientes/1" -UseBasicParsing
    $c = $r.Content | ConvertFrom-Json
    if ($c.nombre -ne "Jose Lema") { throw "Nombre $($c.nombre)" }
}

Test-Case "GET /clientes/999 -> 404" {
    try {
        Invoke-WebRequest -Uri "$base/clientes/999" -UseBasicParsing | Out-Null
        throw "Debia fallar"
    } catch {
        if ([int]$_.Exception.Response.StatusCode -ne 404) { throw "Status $($_.Exception.Response.StatusCode)" }
    }
}

Test-Case "POST /clientes -> 201" {
    $body = '{"nombre":"Test User","identificacion":"ID-TEST-999","contrasena":"1234","telefono":"3000000000"}'
    $r = Invoke-WebRequest -Uri "$base/clientes" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
    if ($r.StatusCode -ne 201) { throw "Status $($r.StatusCode)" }
    $script:testClienteId = ($r.Content | ConvertFrom-Json).clienteId
}

Test-Case "PUT /clientes/{id} -> 200" {
    $body = '{"nombre":"Test User Actualizado","identificacion":"ID-TEST-999","contrasena":"1234"}'
    $r = Invoke-WebRequest -Uri "$base/clientes/$($script:testClienteId)" -Method PUT -Body $body -ContentType "application/json" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "Status $($r.StatusCode)" }
}

# --- CUENTAS ---
Test-Case "GET /cuentas -> 200 y 4 cuentas" {
    $r = Invoke-WebRequest -Uri "$base/cuentas" -UseBasicParsing
    $data = $r.Content | ConvertFrom-Json
    if ($data.Count -ne 4) { throw "Esperadas 4 cuentas, hay $($data.Count)" }
}

Test-Case "GET /cuentas/1 -> 478758 saldo 1425" {
    $c = (Invoke-WebRequest -Uri "$base/cuentas/1" -UseBasicParsing).Content | ConvertFrom-Json
    if ($c.numeroCuenta -ne "478758") { throw "Cuenta $($c.numeroCuenta)" }
    if ([decimal]$c.saldoDisponible -ne 1425) { throw "Saldo $($c.saldoDisponible)" }
}

Test-Case "POST /cuentas -> 201 (585545 Jose PDF caso 3)" {
    $body = '{"numeroCuenta":"585545","tipoCuenta":"Corriente","saldoInicial":1000,"clienteId":1}'
    $r = Invoke-WebRequest -Uri "$base/cuentas" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
    if ($r.StatusCode -ne 201) { throw "Status $($r.StatusCode)" }
    $script:testCuentaId = ($r.Content | ConvertFrom-Json).id
}

# --- MOVIMIENTOS ---
Test-Case "GET /movimientos -> 200 y al menos 4" {
    $data = (Invoke-WebRequest -Uri "$base/movimientos" -UseBasicParsing).Content | ConvertFrom-Json
    if ($data.Count -lt 4) { throw "Movimientos $($data.Count)" }
}

Test-Case "GET /movimientos/2 -> deposito 600" {
    $m = (Invoke-WebRequest -Uri "$base/movimientos/2" -UseBasicParsing).Content | ConvertFrom-Json
    if ([decimal]$m.valor -ne 600) { throw "Valor $($m.valor)" }
}

Test-Case "POST /movimientos retiro OK -> 201 saldo 925" {
    $body = '{"cuentaId":1,"valor":-500}'
    $m = (Invoke-WebRequest -Uri "$base/movimientos" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing).Content | ConvertFrom-Json
    if ([decimal]$m.saldo -ne 925) { throw "Saldo $($m.saldo) esperado 925" }
}

Test-Case "POST /movimientos saldo insuficiente -> 400" {
    $body = '{"cuentaId":4,"valor":-100}'
    try {
        Invoke-WebRequest -Uri "$base/movimientos" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing | Out-Null
        throw "Debia fallar"
    } catch {
        if ([int]$_.Exception.Response.StatusCode -ne 400) { throw "Status $($_.Exception.Response.StatusCode)" }
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $err = $reader.ReadToEnd() | ConvertFrom-Json
        if ($err.mensaje -ne "Saldo no disponible") { throw "Mensaje $($err.mensaje)" }
    }
}

# --- REPORTES ---
Test-Case "GET /reportes Marianela Feb 2022 -> 2 filas PDF" {
    $r = Invoke-WebRequest -Uri "$base/reportes?fechaInicio=2022-02-01&fechaFin=2022-02-28&clienteId=2" -UseBasicParsing
    $data = $r.Content | ConvertFrom-Json
    if ($data.Count -ne 2) { throw "Esperadas 2 filas, hay $($data.Count)" }
    $nums = $data | ForEach-Object { $_.numeroCuenta }
    if ($nums -notcontains "225487" -or $nums -notcontains "496825") { throw "Cuentas $($nums -join ',')" }
}

# --- DELETE (borrado logico) ---
Test-Case "DELETE /clientes test -> 204" {
    $r = Invoke-WebRequest -Uri "$base/clientes/$($script:testClienteId)" -Method DELETE -UseBasicParsing
    if ($r.StatusCode -ne 204) { throw "Status $($r.StatusCode)" }
}

Test-Case "GET /clientes tras delete test -> sigue 3 PDF (test inactivo)" {
    $data = (Invoke-WebRequest -Uri "$base/clientes" -UseBasicParsing).Content | ConvertFrom-Json
    if ($data.Count -ne 3) { throw "Activos $($data.Count)" }
}

Write-Host "`n=== RESUMEN: $passed OK, $failed FAIL ===`n" -ForegroundColor Cyan
if ($failed -gt 0) { exit 1 }
