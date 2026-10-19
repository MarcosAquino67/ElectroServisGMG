# ============================================================================
#  Arma el ejecutable de ElectroServis G.M.G. con jpackage.
#
#  Pasos: compila con Maven -> genera el icono .ico desde el logo ->
#         arma una carpeta autocontenida (con su propio Java) en dist\.
#
#  Uso:  powershell -ExecutionPolicy Bypass -File instalador\build-instalador.ps1
#
#  Si el JDK no esta en la ruta de abajo, podes pasar otra con la variable
#  de entorno ELECTROSERVIS_JDK.
# ============================================================================

$ErrorActionPreference = 'Stop'

$inst = $PSScriptRoot
$raiz = Split-Path -Parent $inst

$jdkRaiz = if ($env:ELECTROSERVIS_JDK) { $env:ELECTROSERVIS_JDK } else { 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot' }
$jdk    = Join-Path $jdkRaiz 'bin'
$env:JAVA_HOME = $jdkRaiz
$mvn    = if ($env:ELECTROSERVIS_MVN) { $env:ELECTROSERVIS_MVN } else { 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' }

if (-not (Test-Path (Join-Path $jdk 'jpackage.exe'))) { throw "No se encontro jpackage en $jdk (revisa ELECTROSERVIS_JDK)" }
if (-not (Test-Path $mvn)) { throw "No se encontro Maven en $mvn (revisa ELECTROSERVIS_MVN)" }

Push-Location $raiz
try {
    Write-Host '== 1) Compilando y empaquetando con Maven ==' -ForegroundColor Cyan
    & $mvn -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven fallo' }

    $jar = Get-ChildItem (Join-Path $raiz 'target') -Filter '*.jar' |
           Where-Object { $_.Name -notlike '*sources*' -and $_.Name -notlike '*javadoc*' } |
           Select-Object -First 1
    if (-not $jar) { throw 'No se encontro el .jar en target\' }

    Copy-Item $jar.FullName (Join-Path $raiz 'target\app\') -Force
    Write-Host "   jar: $($jar.Name)"

    Write-Host '== 2) Generando el icono (.ico) desde el logo ==' -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path (Join-Path $inst 'build') | Out-Null
    & (Join-Path $jdk 'javac.exe') -encoding UTF-8 -d (Join-Path $inst 'build') (Join-Path $inst 'GenerarIcono.java')
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar GenerarIcono.java' }
    & (Join-Path $jdk 'java.exe') '-Dfile.encoding=UTF-8' -cp (Join-Path $inst 'build') GenerarIcono `
        (Join-Path $raiz 'src\main\resources\iconos\logo_empresa.jpg') (Join-Path $inst 'electroservis.ico')
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el icono' }

    Write-Host '== 3) Armando el ejecutable con jpackage ==' -ForegroundColor Cyan
    $dist = Join-Path $raiz 'dist'
    if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }

    & (Join-Path $jdk 'jpackage.exe') --type app-image `
        --name 'ElectroServisGMG' `
        --input (Join-Path $raiz 'target\app') `
        --main-jar $jar.Name `
        --main-class 'com.electroservisgmg.ElectroServisGMG' `
        --icon (Join-Path $inst 'electroservis.ico') `
        --app-version '1.2' `
        --vendor 'ElectroServis G.M.G.' `
        --description 'Sistema de gestion para taller de servicio tecnico y venta de equipos' `
        --copyright 'ElectroServis G.M.G. - 3ro BTI 2026' `
        --java-options '-Dfile.encoding=UTF-8' `
        --dest $dist
    if ($LASTEXITCODE -ne 0) { throw 'jpackage fallo' }

    # Carpetas que el sistema usa y escribe estando instalado.
    foreach ($c in @('imagenes_productos', 'respaldos', 'reportes_generados')) {
        New-Item -ItemType Directory -Force -Path (Join-Path $dist "ElectroServisGMG\$c") | Out-Null
    }

    Write-Host ''
    Write-Host "Listo -> $(Join-Path $dist 'ElectroServisGMG\ElectroServisGMG.exe')" -ForegroundColor Green
    Write-Host 'Ahora compila el instalador con Inno Setup (ISCC.exe):' -ForegroundColor Green
    Write-Host '  & "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe" instalador\ElectroServis.iss' -ForegroundColor Green
}
finally {
    Pop-Location
}
