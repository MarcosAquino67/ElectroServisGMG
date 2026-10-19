; ============================================================================
;  Instalador de ElectroServis G.M.G.  (Inno Setup 6)
;
;  Antes hay que armar el ejecutable:
;      powershell -ExecutionPolicy Bypass -File instalador\build-instalador.ps1
;
;  Compilar este script:
;      & "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe" instalador\ElectroServis.iss
;  (Inno Setup 6 se instala con:  winget install JRSoftware.InnoSetup)
;
;  Resultado:  instalador\Output\ElectroServisGMG-Setup.exe
;
;  El instalador pide 2 cosas:
;    1) los datos del MySQL de esa PC (usuario, contrasena, servidor, puerto y
;       donde esta mysql.exe) para crear la base electroservis_app;
;    2) el usuario con el que se entra al sistema (nombre, usuario, clave y rol).
;
;  Instalacion desatendida (opcional):
;      ElectroServisGMG-Setup.exe /VERYSILENT /SUPPRESSMSGBOXES ^
;          /mysqlclave=2307 /sisnombre="Marcos Aquino" /sisusuario=marcos /sisclave=1234
;  Los nombres de los parametros son los de la lista P() de mas abajo.
; ============================================================================

#define MiNombre  "ElectroServis G.M.G."
#define MiCarpeta "ElectroServisGMG"
#define MiExe     "ElectroServisGMG.exe"
#define MiVersion "1.1"

[Setup]
AppId={{9E2C41B7-6A54-4F0D-B3E8-1C7A8D2F5B90}
AppName={#MiNombre}
AppVersion={#MiVersion}
AppVerName={#MiNombre} {#MiVersion}
AppPublisher=ElectroServis G.M.G.
AppPublisherURL=https://github.com/MarcosAquino67/ElectroServisGMG
DefaultDirName={localappdata}\{#MiCarpeta}
DefaultGroupName={#MiNombre}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir=Output
OutputBaseFilename=ElectroServisGMG-Setup
SetupIconFile=electroservis.ico
UninstallDisplayIcon={app}\{#MiExe}
UninstallDisplayName={#MiNombre}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear un acceso directo en el Escritorio"; GroupDescription: "Accesos directos:"

[Files]
; Todo el ejecutable autocontenido (lleva su propio Java adentro).
Source: "..\dist\{#MiCarpeta}\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs ignoreversion
; El script que crea la base de datos.
; Es la version LIMPIA para el instalador: estructura + usuario de rescate + emisor,
; sin productos, clientes, ventas, compras ni ordenes.
Source: "..\sql\crear_bd_instalador.sql"; DestDir: "{app}\sql"; Flags: ignoreversion
; Acceso para volver a crear la base si hizo falta (queda en el Menu Inicio).
Source: "CrearBaseDeDatos.bat"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\imagenes_productos"
Name: "{app}\respaldos"
Name: "{app}\reportes_generados"

[Icons]
Name: "{group}\{#MiNombre}"; Filename: "{app}\{#MiExe}"; WorkingDir: "{app}"; IconFilename: "{app}\{#MiExe}"
Name: "{group}\Crear o reparar la base de datos"; Filename: "{app}\CrearBaseDeDatos.bat"; WorkingDir: "{app}"; IconFilename: "{app}\{#MiExe}"
Name: "{group}\Desinstalar {#MiNombre}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MiNombre}"; Filename: "{app}\{#MiExe}"; WorkingDir: "{app}"; IconFilename: "{app}\{#MiExe}"; Tasks: desktopicon

[InstallDelete]
; Al instalar encima de una version anterior, Inno no borra los archivos que ya
; no se copian: los dejaba tirados. El jar del sistema lleva la version en el
; nombre, asi que borramos los viejos antes de copiar el nuevo.
Type: files; Name: "{app}\app\ElectroServisGMG-*.jar"

[UninstallDelete]
; Archivos que el sistema (o el instalador) crea al usarse y que Inno no registra solo.
Type: files; Name: "{app}\conexion.properties"
Type: files; Name: "{app}\tema.cfg"
Type: files; Name: "{app}\sql\usuario_sistema.sql"
Type: filesandordirs; Name: "{app}\reportes_generados"

[Run]
; Si la base no se pudo crear no abrimos el sistema: primero hay que crearla
; con el acceso "Crear o reparar la base de datos".
Filename: "{app}\{#MiExe}"; Description: "Abrir {#MiNombre} ahora"; Flags: nowait postinstall skipifsilent; Check: PuedeAbrirSistema

[Code]

{ --------------------------------------------------------------- constantes }

const
  { Sal del sistema (igual que Seguridad.java: el hash tiene que coincidir). }
  SAL_CLAVE = 'electroservisgmg:';
  { Usuario que deja siempre el script de la base, para no quedar afuera. }
  RESCATE_USUARIO = 'Marcos';
  RESCATE_CLAVE   = '2307';

{ -------------------------------------------------------------------- estado }

var
  PaginaMysql: TWizardPage;
  EditMysqlUsuario: TEdit;
  EditMysqlClave: TPasswordEdit;
  EditMysqlHost: TEdit;
  EditMysqlPuerto: TEdit;
  EditMysqlExe: TEdit;
  BotonBuscarMysql: TNewButton;

  PaginaSistema: TWizardPage;
  EditSisNombre: TEdit;
  EditSisUsuario: TEdit;
  EditSisClave: TPasswordEdit;
  EditSisClave2: TPasswordEdit;
  ComboSisRol: TNewComboBox;

  MysqlEncontrado: String;      { archivo mysql.exe que se va a usar }
  ClaveSistemaHash: String;     { clave del usuario del sistema en SHA-256 (o '') }
  AvisoSistema: String;         { aviso no fatal (ej: el usuario no se pudo cargar) }
  BaseDeDatosLista: Boolean;    { lo usa el [Run] para no abrir el sistema si fallo }
  ProbandoConexion: Boolean;    { evita que dos pruebas de conexion se pisen }

{ --------------------------------------------------------------- utilitarias }

{ Valor de un parametro de la linea de comandos (/nombre=valor) o el defecto. }
function P(const Nombre, Defecto: String): String;
begin
  Result := ExpandConstant('{param:' + Nombre + '|' + Defecto + '}');
end;

function ArchTmp(const Nombre: String): String;
begin
  Result := ExpandConstant('{tmp}\' + Nombre);
end;

function LeerTexto(const Ruta: String): String;
var
  a: AnsiString;
begin
  Result := '';
  if FileExists(Ruta) then
    if LoadStringFromFile(Ruta, a) then
      Result := Trim(String(a));
end;

{ Recorta el texto para que un error largo entre en un aviso. }
function Recortar(const Texto: String; Max: Integer): String;
begin
  Result := Trim(Texto);
  if Length(Result) > Max then
    Result := Copy(Result, 1, Max) + ' [...]';
end;

{ Cita el texto para adentro de un script de PowerShell (comillas simples). }
function PsCita(const Texto: String): String;
var
  t: String;
begin
  t := Texto;
  StringChangeEx(t, '''', '''''', True);
  Result := '''' + t + '''';
end;

{ Cita el texto para adentro de un texto de SQL (un string entre comillas). }
function SqlCita(const Texto: String): String;
var
  t: String;
begin
  t := Texto;
  StringChangeEx(t, '\', '\\', True);
  StringChangeEx(t, '''', '''''', True);
  Result := '''' + t + '''';
end;

{ Guarda un texto en UTF-8 SIN BOM (asi lo leen MySQL y Java sin problemas). }
function GuardarUtf8(const Ruta: String; Lineas: TArrayOfString): Boolean;
begin
  Result := SaveStringsToUTF8FileWithoutBOM(Ruta, Lineas, False);
  if not Result then
    Log('No se pudo escribir ' + Ruta);
end;

{ Mensaje que no aparece si la instalacion es silenciosa. }
procedure Avisar(const Texto: String; Tipo: TMsgBoxType);
begin
  Log(Texto);
  if not WizardSilent then
    MsgBox(Texto, Tipo, MB_OK);
end;

{ ------------------------------------------------------------------ PowerShell }

{ Corre un script de PowerShell armado al vuelo (archivo temporal). Devuelve
  True si el proceso arranco; el codigo de salida queda en Codigo. Si la PC no
  tiene PowerShell no pasa nada: el que llama se arregla sin el resultado. }
function CorrerPowerShell(const Cuerpo: String; var Codigo: Integer): Boolean;
var
  Lineas: TArrayOfString;
  script: String;
begin
  script := ArchTmp('esgmg_script.ps1');
  SetArrayLength(Lineas, 1);
  Lineas[0] := Cuerpo;
  GuardarUtf8(script, Lineas);
  Result := Exec('powershell.exe',
    '-NoProfile -NonInteractive -ExecutionPolicy Bypass -File "' + script + '"',
    '', SW_HIDE, ewWaitUntilTerminated, Codigo);
  if not Result then
    Log('No se pudo ejecutar PowerShell.');
end;

{ Nombres y estado de los servicios de MySQL/MariaDB de esta PC ('' si no hay). }
function ServiciosMysql(): String;
var
  Codigo: Integer;
  salida: String;
begin
  Result := '';
  salida := ArchTmp('esgmg_servicios.txt');
  DeleteFile(salida);
  if CorrerPowerShell(
       'Get-Service -ErrorAction SilentlyContinue |' + #13#10 +
       '  Where-Object { $_.Name -match ''mysql|maria'' -or $_.DisplayName -match ''mysql|maria'' } |' + #13#10 +
       '  ForEach-Object { $_.Name + '' ('' + $_.Status + '')'' } |' + #13#10 +
       '  Set-Content -LiteralPath ' + PsCita(salida) + ' -Encoding ASCII', Codigo) then
    if Codigo = 0 then
      Result := LeerTexto(salida);
end;

{ Hash SHA-256 con la sal del sistema, calculado con PowerShell (devuelve '' si
  no se pudo). Se usa el mismo formato que Seguridad.java. }
function HashSha256(const Texto: String): String;
var
  Lineas: TArrayOfString;
  Codigo: Integer;
  archivo, salida: String;
begin
  Result := '';
  archivo := ArchTmp('esgmg_clave.txt');
  salida := ArchTmp('esgmg_hash.txt');
  DeleteFile(salida);

  SetArrayLength(Lineas, 1);
  Lineas[0] := Texto;
  GuardarUtf8(archivo, Lineas);

  if CorrerPowerShell(
       '$clave = (Get-Content -LiteralPath ' + PsCita(archivo) + ' -Raw -Encoding UTF8).TrimEnd("`r","`n")' + #13#10 +
       '$bytes = [System.Text.Encoding]::UTF8.GetBytes(' + PsCita(SAL_CLAVE) + ' + $clave)' + #13#10 +
       '$sha   = [System.Security.Cryptography.SHA256]::Create()' + #13#10 +
       '$hex   = -join ($sha.ComputeHash($bytes) | ForEach-Object { $_.ToString("x2") })' + #13#10 +
       'Set-Content -LiteralPath ' + PsCita(salida) + ' -Value $hex -Encoding ASCII', Codigo) then
    if Codigo = 0 then
      Result := LeerTexto(salida);

  DeleteFile(archivo);
  if Length(Result) <> 64 then
    Result := '';
  if Result = '' then
    Log('No se pudo calcular el hash de la clave (se guarda en texto plano).');
end;

{ ------------------------------------------------------------------- MySQL }

function MysqlEnElPath(): String;
var
  Codigo, corte: Integer;
  archivo, texto: String;
begin
  Result := '';
  archivo := ArchTmp('esgmg_where.txt');
  DeleteFile(archivo);
  Exec('cmd.exe', '/c where mysql > "' + archivo + '" 2>nul', '', SW_HIDE,
       ewWaitUntilTerminated, Codigo);
  texto := LeerTexto(archivo);
  corte := Pos(#10, texto);
  if corte > 0 then
    texto := Copy(texto, 1, corte - 1);
  texto := Trim(texto);
  if FileExists(texto) then
    Result := texto;
end;

{ Busca mysql.exe en las instalaciones tipicas y despues en el PATH. }
function BuscarMysql(): String;
var
  i: Integer;
  exactas: array[0..3] of String;
  comodines: array[0..6] of String;
  rec: TFindRec;
  base, posible: String;
begin
  Result := '';

  { Rutas fijas: XAMPP y las versiones mas comunes. }
  exactas[0] := ExpandConstant('{sd}\xampp\mysql\bin\mysql.exe');
  exactas[1] := ExpandConstant('{pf}\MySQL\MySQL Server 8.0\bin\mysql.exe');
  exactas[2] := ExpandConstant('{pf}\MySQL\MySQL Server 8.4\bin\mysql.exe');
  exactas[3] := ExpandConstant('{pf32}\MySQL\MySQL Server 8.0\bin\mysql.exe');
  for i := 0 to 3 do
    if FileExists(exactas[i]) then begin
      Result := exactas[i];
      Exit;
    end;

  { Carpetas con la version adentro: "MySQL Server *", "MariaDB *", WAMP... }
  comodines[0] := ExpandConstant('{pf}\MySQL\MySQL Server *');
  comodines[1] := ExpandConstant('{pf32}\MySQL\MySQL Server *');
  comodines[2] := ExpandConstant('{pf}\MariaDB *');
  comodines[3] := ExpandConstant('{pf32}\MariaDB *');
  comodines[4] := ExpandConstant('{sd}\MariaDB *');
  comodines[5] := ExpandConstant('{sd}\wamp64\bin\mysql\*');
  comodines[6] := ExpandConstant('{sd}\wamp\bin\mysql\*');
  for i := 0 to 6 do begin
    if FindFirst(comodines[i], rec) then begin
      repeat
        base := Copy(comodines[i], 1, Length(comodines[i]) - 1) + rec.Name;
        posible := base + '\bin\mysql.exe';
        if FileExists(posible) then begin
          FindClose(rec);
          Result := posible;
          Exit;
        end;
        posible := base + '\bin\mariadb.exe';
        if FileExists(posible) then begin
          FindClose(rec);
          Result := posible;
          Exit;
        end;
      until not FindNext(rec);
      FindClose(rec);
    end;
  end;

  { Ultimo intento: lo que este en el PATH. }
  Result := MysqlEnElPath();
end;

{ El mysql.exe que se va a usar: lo que escribio el usuario si existe, si no
  el que se detecta solo. }
function MysqlAUsar(): String;
var
  escrito: String;
begin
  escrito := Trim(EditMysqlExe.Text);
  if (escrito <> '') and FileExists(escrito) then begin
    Result := escrito;
    Exit;
  end;
  Result := BuscarMysql();
  if Result <> '' then
    EditMysqlExe.Text := Result;
end;

{ Archivo .ini con los datos de conexion: asi la clave no viaja por la linea
  de comandos (y queda todo en un solo lugar). }
function ArchivoIniMysql(): String;
var
  usuario, linea: String;
  Lineas: TArrayOfString;
begin
  Result := ArchTmp('esgmg_mysql.ini');
  usuario := Trim(EditMysqlUsuario.Text);
  if usuario = '' then
    usuario := 'root';
  linea := Trim(EditMysqlHost.Text);
  if linea = '' then
    linea := 'localhost';

  SetArrayLength(Lineas, 8);
  Lineas[0] := '[client]';
  Lineas[1] := 'user=' + usuario;
  Lineas[2] := 'password=' + EditMysqlClave.Text;
  Lineas[3] := 'host=' + linea;
  Lineas[4] := 'port=' + Trim(EditMysqlPuerto.Text);
  Lineas[5] := 'protocol=TCP';
  Lineas[6] := 'connect-timeout=10';
  Lineas[7] := '';
  GuardarUtf8(Result, Lineas);
end;

{ Corre mysql.exe. Si ArchivoSql tiene algo lo ejecuta por entrada estandar
  (para los scripts .sql); si no, corre la Sentencia con -e. Devuelve True si
  mysql arranco. La salida y el error reales quedan en Salida y Error. }
function CorrerMysql(const ArchivoSql, Sentencia, ArchivoIni: String;
                     var Codigo: Integer; var Salida, Error: String): Boolean;
var
  orden, sal, err: String;
begin
  Result := False;
  Salida := '';
  Error := '';
  Codigo := -1;

  sal := ArchTmp('esgmg_out.txt');
  err := ArchTmp('esgmg_err.txt');
  DeleteFile(sal);
  DeleteFile(err);

  orden := '"' + MysqlEncontrado + '" --defaults-extra-file="' + ArchivoIni + '"';
  if Sentencia <> '' then
    orden := orden + ' --execute="' + Sentencia + '"'
  else
    orden := orden + ' < "' + ArchivoSql + '"';
  orden := orden + ' > "' + sal + '" 2> "' + err + '"';

  { cmd necesita las comillas de afuera para no comerse las de adentro. }
  if not Exec('cmd.exe', '/c "' + orden + '"', '', SW_HIDE, ewWaitUntilTerminated, Codigo) then begin
    Error := 'No se pudo ejecutar cmd.exe.';
    Exit;
  end;

  Result := True;
  Salida := LeerTexto(sal);
  Error := LeerTexto(err);
  if (Codigo <> 0) and (Error = '') then
    Error := Salida;
  Log('mysql (codigo ' + IntToStr(Codigo) + '): ' + Recortar(Error, 400));
end;

{ Explica el error tipico de MySQL con lo que hay que revisar. }
function PistaError(const Error: String): String;
var
  servicios: String;
begin
  Result := '';
  if Pos('2003', Error) > 0 then begin
    servicios := ServiciosMysql();
    Result := 'Error 2003 (no se puede conectar): el servidor MySQL no esta ' +
              'atendiendo en ' + Trim(EditMysqlHost.Text) + ':' + Trim(EditMysqlPuerto.Text) + '.' + #13#10 +
              'Revisa que el servicio de MySQL este iniciado (Servicios de Windows) y el puerto.';
    if servicios = '' then
      Result := Result + #13#10 + 'En esta PC no se encontro ningun servicio de MySQL.'
    else
      Result := Result + #13#10 + 'Servicios encontrados: ' + servicios;
  end;
  if Pos('2059', Error) > 0 then
    Result := 'Error 2059: MySQL no acepto el tipo de autenticacion de este cliente.' + #13#10 +
              'Usa el mysql.exe de la misma version que instalaste (boton Buscar...).';
  if Pos('1045', Error) > 0 then
    Result := 'Error 1045 (acceso denegado): el usuario o la contrasena de MySQL no son correctos.';
  if Pos('1049', Error) > 0 then
    Result := 'Error 1049: la base electroservis_app no existe todavia.';
  if Result = '' then
    Result := 'Revisa el usuario, la contrasena, el servidor y el puerto de MySQL.';
end;

{ ------------------------------------------------------------------ paginas }

function Etiqueta(const Pagina: TWizardPage; Izq, Arriba: Integer; const Texto: String): TLabel;
begin
  Result := TLabel.Create(Pagina);
  Result.Parent := Pagina.Surface;
  Result.Left := Izq;
  Result.Top := Arriba;
  Result.Caption := Texto;
  Result.AutoSize := True;
end;

function Caja(const Pagina: TWizardPage; Izq, Arriba, Ancho: Integer; const Valor: String): TEdit;
begin
  Result := TEdit.Create(Pagina);
  Result.Parent := Pagina.Surface;
  Result.Left := Izq;
  Result.Top := Arriba;
  Result.Width := Ancho;
  Result.Text := Valor;
end;

function CajaClave(const Pagina: TWizardPage; Izq, Arriba, Ancho: Integer; const Valor: String): TPasswordEdit;
begin
  Result := TPasswordEdit.Create(Pagina);
  Result.Parent := Pagina.Surface;
  Result.Left := Izq;
  Result.Top := Arriba;
  Result.Width := Ancho;
  Result.Text := Valor;
end;

{ ------------------------------------------------------------------ usuario }

function RolSistema(): String;
begin
  if ComboSisRol.ItemIndex = 1 then
    Result := 'USUARIO'
  else
    Result := 'ADMINISTRADOR';
end;

{ La clave se guarda con hash SHA-256 (como en el sistema). Si no se pudo
  calcular el hash se guarda como la escribio el usuario: el sistema la
  convierte sola en el primer inicio de sesion. }
function ClaveSistema(): String;
begin
  if Length(ClaveSistemaHash) = 64 then
    Result := ClaveSistemaHash
  else
    Result := EditSisClave.Text;
end;

{ Texto que explica por que no se puede seguir con los datos del usuario. }
function ProblemaUsuarioSistema(): String;
begin
  Result := '';
  if Trim(EditSisNombre.Text) = '' then
    Result := 'Escribi el nombre y apellido.'
  else if Trim(EditSisUsuario.Text) = '' then
    Result := 'Escribi el nombre de usuario (con ese entras al sistema).'
  else if Length(EditSisClave.Text) < 4 then
    Result := 'La contrasena tiene que tener al menos 4 caracteres.'
  else if EditSisClave.Text <> EditSisClave2.Text then
    Result := 'Las dos contrasenas no coinciden.';
end;

function DatosUsuarioSistemaValidos(): Boolean;
begin
  Result := ProblemaUsuarioSistema() = '';
end;

{ El INSERT del usuario del sistema (se usa ahora y queda guardado para poder
  reponerlo despues con "Crear o reparar la base de datos"). }
function InsertUsuarioSistema(): String;
var
  nom, usr, clv, rol: String;
begin
  nom := SqlCita(Trim(EditSisNombre.Text));
  usr := SqlCita(Trim(EditSisUsuario.Text));
  clv := SqlCita(ClaveSistema());
  rol := SqlCita(RolSistema());
  Result := 'INSERT INTO usuario (nombre, usuario, clave, rol, estado) ' +
            'VALUES (' + nom + ', ' + usr + ', ' + clv + ', ' + rol + ', ''ACTIVO'') ' +
            'ON DUPLICATE KEY UPDATE nombre=' + nom + ', clave=' + clv +
            ', rol=' + rol + ', estado=''ACTIVO'';';
end;

{ Escribe el script del usuario del sistema (UTF-8, sin BOM). }
function GuardarSqlUsuario(const Ruta: String): Boolean;
var
  Lineas: TArrayOfString;
begin
  SetArrayLength(Lineas, 4);
  Lineas[0] := '-- Usuario del sistema cargado por el instalador de ElectroServis G.M.G.';
  Lineas[1] := '-- (la clave ya viene con hash SHA-256, igual que en el sistema)';
  Lineas[2] := 'USE electroservis_app;';
  Lineas[3] := 'SET NAMES utf8mb4;' + #13#10 + InsertUsuarioSistema();
  Result := GuardarUtf8(Ruta, Lineas);
end;

{ ------------------------------------------------------------- instalacion }

{ Deja la conexion guardada al lado del ejecutable. Se escribe SIEMPRE, aunque
  la base no se pueda crear: asi el sistema ya queda apuntando al servidor que
  se le indico. }
procedure EscribirConexion();
var
  Lineas: TArrayOfString;
begin
  if Pos('\', EditMysqlClave.Text) > 0 then
    Log('Ojo: la contrasena de MySQL tiene una barra invertida y el archivo de ' +
        'propiedades de Java la trata como caracter especial.');

  SetArrayLength(Lineas, 8);
  Lineas[0] := '# Generado por el instalador de ElectroServis G.M.G.';
  Lineas[1] := '# Si cambias la clave de MySQL, editala aca o reinstala el sistema.';
  Lineas[2] := 'host=' + Trim(EditMysqlHost.Text);
  Lineas[3] := 'puerto=' + Trim(EditMysqlPuerto.Text);
  Lineas[4] := 'base=electroservis_app';
  Lineas[5] := 'usuario=' + Trim(EditMysqlUsuario.Text);
  Lineas[6] := 'clave=' + EditMysqlClave.Text;
  Lineas[7] := '';
  if not GuardarUtf8(ExpandConstant('{app}\conexion.properties'), Lineas) then
    Avisar('No se pudo escribir el archivo conexion.properties.', mbError);
end;

{ Prueba la conexion y, si anda, crea la base y el usuario del sistema.
  Detalle explica el problema real de MySQL si algo falla. }
function IntentarCrearBase(var Detalle: String): Boolean;
var
  ini, salida, error: String;
  Codigo: Integer;
begin
  Result := False;
  Detalle := '';
  AvisoSistema := '';
  ini := ArchivoIniMysql();

  { 1) Probamos usuario, clave, servidor y puerto antes de tocar nada. }
  if not CorrerMysql('', 'SELECT VERSION();', ini, Codigo, salida, error) then begin
    Detalle := 'No se pudo ejecutar mysql.exe:' + #13#10 + MysqlEncontrado;
    Exit;
  end;
  if Codigo <> 0 then begin
    Detalle := 'MySQL no acepto la conexion:' + #13#10 + Recortar(error, 600)
               + #13#10 + #13#10 + PistaError(error);
    Exit;
  end;
  Log('Conexion de prueba OK: ' + Recortar(salida, 120));

  { 2) La estructura y los datos minimos (script limpio del instalador). }
  if not CorrerMysql(ExpandConstant('{app}\sql\crear_bd_instalador.sql'), '',
                     ini, Codigo, salida, error) then begin
    Detalle := 'No se pudo ejecutar el script de la base.';
    Exit;
  end;
  if Codigo <> 0 then begin
    Detalle := 'MySQL devolvio un error al crear la base:' + #13#10 +
               Recortar(error, 700) + #13#10#13#10 + PistaError(error);
    Exit;
  end;

  { 3) El usuario del sistema que se cargo en el instalador. }
  if DatosUsuarioSistemaValidos() then begin
    if not CorrerMysql(ArchTmp('esgmg_usuario.sql'), '', ini, Codigo, salida, error) then begin
      AvisoSistema := 'No se pudo ejecutar el script del usuario del sistema.';
      Result := True;
      Exit;
    end;
    if Codigo <> 0 then
      AvisoSistema := 'La base se creo, pero el usuario del sistema no se pudo cargar:'
                      + #13#10 + Recortar(error, 400) + #13#10
                      + 'Igual podes entrar con ' + RESCATE_USUARIO + ' / ' + RESCATE_CLAVE + '.';
  end else
    AvisoSistema := 'No se creo ningun usuario del sistema (los datos estaban ' +
                    'incompletos): se entra con ' + RESCATE_USUARIO + ' / ' + RESCATE_CLAVE + '.';

  Result := True;
end;

{ Crea la base (con reintento) y avisa como entrar al sistema. }
procedure CrearBaseDeDatos();
var
  Detalle, Texto: String;
  intentos: Integer;
begin
  EscribirConexion();

  MysqlEncontrado := MysqlAUsar();
  if MysqlEncontrado = '' then begin
    Avisar('No se encontro MySQL instalado en esta PC.' + #13#10 +
           'El sistema quedo instalado igual, pero la base hay que crearla a mano:' + #13#10 +
           'instala MySQL Server 8 y usa el acceso "Crear o reparar la base de datos" ' +
           'del Menu Inicio (o el script sql\crear_bd_instalador.sql).', mbError);
    Exit;
  end;
  Log('mysql.exe = ' + MysqlEncontrado);

  if DatosUsuarioSistemaValidos() then begin
    ClaveSistemaHash := HashSha256(EditSisClave.Text);
    GuardarSqlUsuario(ExpandConstant('{app}\sql\usuario_sistema.sql'));
    GuardarSqlUsuario(ArchTmp('esgmg_usuario.sql'));
  end;

  intentos := 0;
  while True do begin
    intentos := intentos + 1;
    if IntentarCrearBase(Detalle) then begin
      BaseDeDatosLista := True;
      Texto := 'La base de datos electroservis_app quedo lista.';
      if DatosUsuarioSistemaValidos() then
        Texto := Texto + #13#10#13#10 +
                 'Entra al sistema con:' + #13#10 +
                 '   Usuario: ' + Trim(EditSisUsuario.Text) + #13#10 +
                 '   Rol: ' + RolSistema() + #13#10 +
                 '   Contrasena: la que escribiste en el instalador' + #13#10#13#10 +
                 'Usuario de rescate: ' + RESCATE_USUARIO + ' / ' + RESCATE_CLAVE;
      if AvisoSistema <> '' then
        Texto := Texto + #13#10#13#10 + 'Aviso: ' + AvisoSistema;
      Avisar(Texto, mbInformation);
      Exit;
    end;

    Avisar('No se pudo crear la base de datos.' + #13#10#13#10 + Detalle, mbError);

    { Reintentar sirve cuando el problema fue que MySQL estaba arrancando o que
      el servicio estaba detenido y alguien lo inicio en el medio. }
    if WizardSilent then
      Exit;
    if (intentos >= 5) or
       (MsgBox('¿Queres intentar de nuevo con los mismos datos?' + #13#10 +
               '(por ejemplo, si recien iniciaste el servicio de MySQL)' + #13#10#13#10 +
               'Si decis No, la base queda sin crear y podes crearla despues con el ' +
               'acceso "Crear o reparar la base de datos" del Menu Inicio.',
               mbConfirmation, MB_RETRYCANCEL) <> IDRETRY) then
      Exit;
  end;
end;

function PuedeAbrirSistema(): Boolean;
begin
  Result := BaseDeDatosLista;
end;

{ --------------------------------------------------------------- boton Buscar }

{ Abre el dialogo de Windows para elegir mysql.exe y lo pone en la caja. }
procedure ClickBuscarMysql(Sender: TObject);
var
  Codigo: Integer;
  salida, elegido: String;
begin
  salida := ArchTmp('esgmg_mysql_path.txt');
  DeleteFile(salida);
  if not CorrerPowerShell(
       'Add-Type -AssemblyName System.Windows.Forms' + #13#10 +
       '$d = New-Object System.Windows.Forms.OpenFileDialog' + #13#10 +
       '$d.Title  = ''Elegi el archivo mysql.exe''' + #13#10 +
       '$d.Filter = ''mysql.exe|mysql.exe|Programas (*.exe)|*.exe|Todos los archivos (*.*)|*.*''' + #13#10 +
       'if ($d.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) {' + #13#10 +
       '  Set-Content -LiteralPath ' + PsCita(salida) + ' -Value $d.FileName -Encoding ASCII' + #13#10 +
       '}', Codigo) then
    Exit;
  elegido := LeerTexto(salida);
  if elegido <> '' then
    EditMysqlExe.Text := elegido;
end;

{ ---------------------------------------------------------------- asistente }

procedure InitializeWizard;
var
  Lbl: TLabel;
begin
  { --- Pagina 1: los datos del MySQL de esta PC --- }
  PaginaMysql := CreateCustomPage(wpSelectDir, 'Base de datos MySQL',
    'Con estos datos el instalador crea la base electroservis_app y los deja ' +
    'guardados para que el sistema se conecte solo.');

  Lbl := Etiqueta(PaginaMysql, 0, 0, 'Usuario de MySQL:');
  EditMysqlUsuario := Caja(PaginaMysql, 0, Lbl.Top + Lbl.Height + 4,
    PaginaMysql.SurfaceWidth, P('mysqlusuario', 'root'));

  Lbl := Etiqueta(PaginaMysql, 0, EditMysqlUsuario.Top + EditMysqlUsuario.Height + 12,
    'Contrasena de MySQL:');
  EditMysqlClave := CajaClave(PaginaMysql, 0, Lbl.Top + Lbl.Height + 4,
    PaginaMysql.SurfaceWidth, P('mysqlclave', '2307'));

  Lbl := Etiqueta(PaginaMysql, 0, EditMysqlClave.Top + EditMysqlClave.Height + 12,
    'Servidor de MySQL:');
  EditMysqlHost := Caja(PaginaMysql, 0, Lbl.Top + Lbl.Height + 4,
    PaginaMysql.SurfaceWidth - 150, P('mysqlhost', 'localhost'));

  Lbl := Etiqueta(PaginaMysql, PaginaMysql.SurfaceWidth - 140, EditMysqlClave.Top +
    EditMysqlClave.Height + 12, 'Puerto:');
  EditMysqlPuerto := Caja(PaginaMysql, PaginaMysql.SurfaceWidth - 140, EditMysqlHost.Top,
    140, P('mysqlpuerto', '3306'));

  Lbl := Etiqueta(PaginaMysql, 0, EditMysqlHost.Top + EditMysqlHost.Height + 12,
    'Programa mysql.exe (se detecta solo):');
  EditMysqlExe := Caja(PaginaMysql, 0, Lbl.Top + Lbl.Height + 4,
    PaginaMysql.SurfaceWidth - 92, P('mysqlexe', BuscarMysql()));

  BotonBuscarMysql := TNewButton.Create(PaginaMysql);
  BotonBuscarMysql.Parent := PaginaMysql.Surface;
  BotonBuscarMysql.Left := PaginaMysql.SurfaceWidth - 86;
  BotonBuscarMysql.Top := EditMysqlExe.Top - 2;
  BotonBuscarMysql.Width := 86;
  BotonBuscarMysql.Height := EditMysqlExe.Height + 4;
  BotonBuscarMysql.Caption := 'Buscar...';
  BotonBuscarMysql.OnClick := @ClickBuscarMysql;

  PaginaMysql.Surface.Height := EditMysqlExe.Top + EditMysqlExe.Height + 12;

  { --- Pagina 2: el usuario con el que se entra al sistema --- }
  PaginaSistema := CreateCustomPage(PaginaMysql.ID, 'Usuario del sistema',
    'Con estos datos entras al sistema (el instalador lo crea en la base). ' +
    'Tambien queda un usuario de rescate: ' + RESCATE_USUARIO + ' / ' + RESCATE_CLAVE + '.');

  Lbl := Etiqueta(PaginaSistema, 0, 0, 'Nombre y apellido:');
  EditSisNombre := Caja(PaginaSistema, 0, Lbl.Top + Lbl.Height + 4,
    PaginaSistema.SurfaceWidth, P('sisnombre', 'Administrador'));

  Lbl := Etiqueta(PaginaSistema, 0, EditSisNombre.Top + EditSisNombre.Height + 12,
    'Nombre de usuario:');
  EditSisUsuario := Caja(PaginaSistema, 0, Lbl.Top + Lbl.Height + 4,
    PaginaSistema.SurfaceWidth, P('sisusuario', 'admin'));

  Lbl := Etiqueta(PaginaSistema, 0, EditSisUsuario.Top + EditSisUsuario.Height + 12,
    'Contrasena (minimo 4 caracteres):');
  EditSisClave := CajaClave(PaginaSistema, 0, Lbl.Top + Lbl.Height + 4,
    (PaginaSistema.SurfaceWidth - 12) div 2, P('sisclave', ''));

  Lbl := Etiqueta(PaginaSistema, (PaginaSistema.SurfaceWidth + 12) div 2,
    EditSisUsuario.Top + EditSisUsuario.Height + 12, 'Repetir contrasena:');
  EditSisClave2 := CajaClave(PaginaSistema, (PaginaSistema.SurfaceWidth + 12) div 2,
    EditSisClave.Top, (PaginaSistema.SurfaceWidth - 12) div 2, P('sisclave', ''));

  Lbl := Etiqueta(PaginaSistema, 0, EditSisClave.Top + EditSisClave.Height + 12, 'Rol:');
  ComboSisRol := TNewComboBox.Create(PaginaSistema);
  ComboSisRol.Parent := PaginaSistema.Surface;
  ComboSisRol.Left := 0;
  ComboSisRol.Top := Lbl.Top + Lbl.Height + 4;
  ComboSisRol.Width := 200;
  ComboSisRol.Style := csDropDownList;
  ComboSisRol.Items.Add('ADMINISTRADOR');
  ComboSisRol.Items.Add('USUARIO');
  if CompareText(P('sisrol', 'ADMINISTRADOR'), 'USUARIO') = 0 then
    ComboSisRol.ItemIndex := 1
  else
    ComboSisRol.ItemIndex := 0;

  Lbl := Etiqueta(PaginaSistema, 210, ComboSisRol.Top + 2,
    'El ADMINISTRADOR entra a todo; el USUARIO no ve Usuarios ni Ajustes.');

  PaginaSistema.Surface.Height := ComboSisRol.Top + ComboSisRol.Height + 12;
end;

{ Al apretar Siguiente se revisan los datos ANTES de copiar nada: si MySQL no
  contesta, el error real aparece ahi y se puede corregir sin reinstalar. }
function NextButtonClick(CurPageID: Integer): Boolean;
var
  ini, salida, error: String;
  Codigo: Integer;
begin
  Result := True;
  if WizardSilent then
    Exit;

  { Si ya hay una prueba de conexion en curso (por ejemplo, si apretaron
    Siguiente dos veces) no dejamos entrar de nuevo: se pisarian los archivos
    temporales que usa la prueba. }
  if ProbandoConexion then begin
    Result := False;
    Exit;
  end;
  ProbandoConexion := True;
  try
    if CurPageID = PaginaMysql.ID then begin
      if Trim(EditMysqlUsuario.Text) = '' then begin
        MsgBox('Escribi el usuario de MySQL (normalmente root).', mbError, MB_OK);
        Result := False;
        Exit;
      end;

      MysqlEncontrado := MysqlAUsar();
      if MysqlEncontrado = '' then begin
        Result := MsgBox('No se encontro mysql.exe en esta PC.' + #13#10 +
          'Podes escribir la ruta a mano o usar el boton Buscar....' + #13#10#13#10 +
          'Si tu PC no tiene MySQL, el sistema se instala igual: despues se crea la ' +
          'base con el acceso "Crear o reparar la base de datos".' + #13#10#13#10 +
          '¿Queres continuar igual?', mbError, MB_OKCANCEL) = IDOK;
        Exit;
      end;
      Log('Probando la conexion con ' + MysqlEncontrado);

      ini := ArchivoIniMysql();
      if not CorrerMysql('', 'SELECT VERSION();', ini, Codigo, salida, error) then begin
        Result := MsgBox('No se pudo ejecutar mysql.exe:' + #13#10 + MysqlEncontrado
          + #13#10 + '¿Queres continuar igual?', mbError, MB_OKCANCEL) = IDOK;
        Exit;
      end;

      if Codigo <> 0 then
        Result := MsgBox('No se pudo conectar a MySQL.' + #13#10#13#10 +
          Recortar(error, 600) + #13#10#13#10 + PistaError(error) + #13#10#13#10 +
          'Corregi los datos y apreta Siguiente otra vez. Si decis Sí, seguimos igual ' +
          '(la base se puede crear despues con el acceso "Crear o reparar la base de datos").',
          mbError, MB_OKCANCEL) = IDOK
      else
        Log('Conexion de prueba OK: ' + Recortar(salida, 120));
    end;

    if CurPageID = PaginaSistema.ID then begin
      if not DatosUsuarioSistemaValidos() then begin
        MsgBox(ProblemaUsuarioSistema(), mbError, MB_OK);
        Result := False;
        Exit;
      end;
      if CompareText(Trim(EditSisUsuario.Text), RESCATE_USUARIO) = 0 then
        Result := MsgBox('Estas usando el nombre del usuario de rescate (' +
          RESCATE_USUARIO + ').' + #13#10 +
          'Se va a cambiar la contrasena de ese usuario y ya no va a servir ' +
          RESCATE_USUARIO + ' / ' + RESCATE_CLAVE + ' para entrar.' + #13#10#13#10 +
          '¿Seguis con ese nombre?', mbConfirmation, MB_OKCANCEL) = IDOK;
    end;
  finally
    ProbandoConexion := False;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
    CrearBaseDeDatos();
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  { Se deja la base de datos y los respaldos: el usuario decide si los borra. }
  if CurUninstallStep = usPostUninstall then
    Log('La base de datos electroservis_app y la carpeta de respaldos no se borraron.');
end;
