@echo off
rem ===========================================================================
rem  ElectroServis G.M.G. - Crear o reparar la base de datos
rem
rem  Lo usa el instalador y queda tambien como acceso directo en el Menu Inicio:
rem  sirve para volver a crear la base electroservis_app si fallo la instalacion,
rem  si se reinstalo MySQL o si aparecio el aviso "no se pudo conectar".
rem
rem  Toma los datos de conexion del archivo conexion.properties que escribio el
rem  instalador (usuario, clave, servidor y puerto de MySQL). Si no existe, usa
rem  root / 2307 / localhost / 3306.
rem
rem  No borra nada: solo crea lo que falte (las tablas y el usuario del instalador).
rem ===========================================================================
setlocal EnableExtensions
title ElectroServis G.M.G. - Crear o reparar la base de datos
cd /d "%~dp0"

echo ===========================================================================
echo   ElectroServis G.M.G. - Crear o reparar la base de datos
echo ===========================================================================
echo.
echo   Este programa crea (o vuelve a crear) la base de datos
echo   "electroservis_app" y deja cargado el usuario del sistema que se
echo   eligio en el instalador.
echo.
echo   No borra lo que ya este cargado: solo crea las tablas que falten.
echo.
pause

set "CFG=%~dp0conexion.properties"
set "SQL_BD=%~dp0sql\crear_bd_instalador.sql"
set "SQL_USUARIO=%~dp0sql\usuario_sistema.sql"

set "MYSQL="
set "MYUSU=root"
set "MYCLAVE=2307"
set "MYHOST=localhost"
set "MYPUERTO=3306"

if exist "%CFG%" for /f "usebackq tokens=1,* delims==" %%A in ("%CFG%") do call :Leer "%%A" "%%B"

call :BuscarMysql

if not defined MYSQL (
  echo   [!] No se encontro mysql.exe en esta PC.
  echo       Instala MySQL Server 8 y volve a ejecutar este archivo.
  goto :Fin
)
if not exist "%SQL_BD%" (
  echo   [!] No se encontro el script:
  echo       %SQL_BD%
  goto :Fin
)

echo   Servidor : %MYHOST%:%MYPUERTO%
echo   Usuario  : %MYUSU%
echo   Cliente  : %MYSQL%
echo.
echo   Creando la estructura de la base...
set "MYSQL_PWD=%MYCLAVE%"
"%MYSQL%" -u "%MYUSU%" -h "%MYHOST%" -P "%MYPUERTO%" --protocol=TCP --connect-timeout=10 < "%SQL_BD%"
if errorlevel 1 goto :Fallo

if exist "%SQL_USUARIO%" (
  echo   Cargando el usuario del sistema...
  "%MYSQL%" -u "%MYUSU%" -h "%MYHOST%" -P "%MYPUERTO%" --protocol=TCP --connect-timeout=10 < "%SQL_USUARIO%"
  if errorlevel 1 goto :Fallo
)

echo.
echo   [OK] La base electroservis_app esta lista.
echo        Ya podes abrir el sistema desde el acceso directo.
goto :Fin

:Fallo
echo.
echo   [X] MySQL devolvio un error (mira el mensaje de arriba).
echo.
echo   Error 1045 -> el usuario o la contrasena de MySQL no son correctos.
echo   Error 2003 -> el servidor MySQL no esta iniciado. Inicialo desde
echo                 Servicios de Windows (services.msc) y proba de nuevo.
echo   Si cambiaste la clave de MySQL, editala en:
echo       %CFG%
echo   o reinstala el sistema para que vuelva a escribirla.

:Fin
set "MYSQL_PWD="
echo.
pause
exit /b

rem ---------------------------------------------------------------------------
rem  Guarda los datos de conexion que lee del conexion.properties
rem ---------------------------------------------------------------------------
:Leer
if /i "%~1"=="usuario" set "MYUSU=%~2"
if /i "%~1"=="clave"   set "MYCLAVE=%~2"
if /i "%~1"=="host"    set "MYHOST=%~2"
if /i "%~1"=="puerto"  set "MYPUERTO=%~2"
exit /b

rem ---------------------------------------------------------------------------
rem  Busca mysql.exe (MySQL, MariaDB, XAMPP, WAMP o el PATH)
rem ---------------------------------------------------------------------------
:BuscarMysql
for %%P in (
  "%ProgramFiles%\MySQL\MySQL Server 9.0\bin\mysql.exe"
  "%ProgramFiles%\MySQL\MySQL Server 8.4\bin\mysql.exe"
  "%ProgramFiles%\MySQL\MySQL Server 8.1\bin\mysql.exe"
  "%ProgramFiles%\MySQL\MySQL Server 8.0\bin\mysql.exe"
  "%ProgramFiles%\MariaDB 10.4\bin\mysql.exe"
  "%ProgramFiles%\MariaDB 10.11\bin\mysql.exe"
  "%SystemDrive%\xampp\mysql\bin\mysql.exe"
) do if not defined MYSQL if exist "%%~P" set "MYSQL=%%~P"
if not defined MYSQL for /f "delims=" %%P in ('where mysql 2^>nul') do if not defined MYSQL set "MYSQL=%%P"
if defined MYSQL if not exist "%MYSQL%" set "MYSQL="
exit /b
