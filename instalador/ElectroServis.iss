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
; ============================================================================

#define MiNombre  "ElectroServis G.M.G."
#define MiCarpeta "ElectroServisGMG"
#define MiExe     "ElectroServisGMG.exe"
#define MiVersion "1.0"

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
; Es la version LIMPIA para el instalador: estructura + usuario admin + emisor,
; sin productos, clientes, ventas, compras ni ordenes.
Source: "..\sql\crear_bd_instalador.sql"; DestDir: "{app}\sql"; Flags: ignoreversion

[Dirs]
Name: "{app}\imagenes_productos"
Name: "{app}\respaldos"
Name: "{app}\reportes_generados"

[Icons]
Name: "{group}\{#MiNombre}"; Filename: "{app}\{#MiExe}"; WorkingDir: "{app}"; IconFilename: "{app}\{#MiExe}"
Name: "{group}\Desinstalar {#MiNombre}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MiNombre}"; Filename: "{app}\{#MiExe}"; WorkingDir: "{app}"; IconFilename: "{app}\{#MiExe}"; Tasks: desktopicon

[UninstallDelete]
; Archivos que el sistema crea al usarse y que Inno no registra solo.
Type: files; Name: "{app}\conexion.properties"
Type: files; Name: "{app}\tema.cfg"
Type: filesandordirs; Name: "{app}\reportes_generados"

[Run]
Filename: "{app}\{#MiExe}"; Description: "Abrir {#MiNombre} ahora"; Flags: nowait postinstall skipifsilent

[Code]
var
  PaginaMysql: TWizardPage;
  EditUsuario: TEdit;
  EditClave: TPasswordEdit;
  EditHost: TEdit;

{ Mensaje que no aparece si la instalacion es silenciosa. }
procedure Avisar(const Texto: String; Tipo: TMsgBoxType);
begin
  Log(Texto);
  if not WizardSilent then
    MsgBox(Texto, Tipo, MB_OK);
end;

procedure InitializeWizard;
var
  Lbl: TLabel;
begin
  PaginaMysql := CreateCustomPage(wpSelectDir, 'Base de datos MySQL',
    'Con estos datos el instalador crea la base de datos electroservis_app y ' +
    'los deja guardados para que el sistema se conecte solo.');

  Lbl := TLabel.Create(PaginaMysql);
  Lbl.Parent := PaginaMysql.Surface;
  Lbl.Left := 0;
  Lbl.Top := 0;
  Lbl.Caption := 'Usuario de MySQL:';
  Lbl.AutoSize := True;

  EditUsuario := TEdit.Create(PaginaMysql);
  EditUsuario.Parent := PaginaMysql.Surface;
  EditUsuario.Left := 0;
  EditUsuario.Top := Lbl.Top + Lbl.Height + 6;
  EditUsuario.Width := PaginaMysql.SurfaceWidth;
  EditUsuario.Text := 'root';

  Lbl := TLabel.Create(PaginaMysql);
  Lbl.Parent := PaginaMysql.Surface;
  Lbl.Left := 0;
  Lbl.Top := EditUsuario.Top + EditUsuario.Height + 16;
  Lbl.Caption := 'Contrasena de MySQL:';
  Lbl.AutoSize := True;

  EditClave := TPasswordEdit.Create(PaginaMysql);
  EditClave.Parent := PaginaMysql.Surface;
  EditClave.Left := 0;
  EditClave.Top := Lbl.Top + Lbl.Height + 6;
  EditClave.Width := PaginaMysql.SurfaceWidth;
  EditClave.Text := '2307';

  Lbl := TLabel.Create(PaginaMysql);
  Lbl.Parent := PaginaMysql.Surface;
  Lbl.Left := 0;
  Lbl.Top := EditClave.Top + EditClave.Height + 16;
  Lbl.Caption := 'Servidor (host) de MySQL:';
  Lbl.AutoSize := True;

  EditHost := TEdit.Create(PaginaMysql);
  EditHost.Parent := PaginaMysql.Surface;
  EditHost.Left := 0;
  EditHost.Top := Lbl.Top + Lbl.Height + 6;
  EditHost.Width := PaginaMysql.SurfaceWidth;
  EditHost.Text := 'localhost';
end;

{ Busca mysql.exe en las instalaciones tipicas de MySQL. }
function BuscarMysql(): String;
var
  i: Integer;
  rutas: array[0..5] of String;
begin
  rutas[0] := ExpandConstant('{pf}\MySQL\MySQL Server 8.0\bin\mysql.exe');
  rutas[1] := ExpandConstant('{pf}\MySQL\MySQL Server 8.4\bin\mysql.exe');
  rutas[2] := ExpandConstant('{pf}\MySQL\MySQL Server 9.0\bin\mysql.exe');
  rutas[3] := ExpandConstant('{pf}\MySQL\MySQL Server 8.1\bin\mysql.exe');
  rutas[4] := ExpandConstant('{pf}\MySQL\MySQL Server 5.7\bin\mysql.exe');
  rutas[5] := ExpandConstant('{pf32}\MySQL\MySQL Server 8.0\bin\mysql.exe');
  Result := '';
  for i := 0 to 5 do
    if FileExists(rutas[i]) then begin Result := rutas[i]; Exit; end;
end;

{ Corre crear_bd_app.sql y deja escrito conexion.properties. Devuelve True si anduvo. }
function CrearBaseDeDatos(): Boolean;
var
  mysql, ini, script, comando: String;
  codigo: Integer;
  lineas: TStringList;
begin
  Result := False;

  mysql := BuscarMysql();
  if mysql = '' then begin
    Avisar('No se encontro MySQL instalado en esta PC.' + #13#10 +
           'El sistema quedo instalado igual, pero la base hay que crearla a mano:' + #13#10 +
           'instala MySQL Server 8 y ejecuta el archivo sql\crear_bd_app.sql.',
           mbError);
    Exit;
  end;
  Log('mysql.exe = ' + mysql);

  script := ExpandConstant('{app}\sql\crear_bd_instalador.sql');
  StringChangeEx(script, '\', '/', True);

  { La clave va en un archivo aparte para no pasarla por la linea de comandos. }
  ini := ExpandConstant('{tmp}\esgmg_mysql.ini');
  lineas := TStringList.Create;
  try
    lineas.Add('[client]');
    lineas.Add('user=' + EditUsuario.Text);
    lineas.Add('password=' + EditClave.Text);
    lineas.SaveToFile(ini);
  finally
    lineas.Free;
  end;

  comando := '/c ""' + mysql + '" --defaults-extra-file="' + ini + '" --host=' + EditHost.Text +
             ' < "' + script + '""';
  Log('Ejecutando: cmd ' + comando);
  if not Exec('cmd.exe', comando, '', SW_HIDE, ewWaitUntilTerminated, codigo) then begin
    Avisar('No se pudo ejecutar mysql.exe.', mbError);
    Exit;
  end;
  Log('mysql devolvio el codigo ' + IntToStr(codigo));
  if codigo <> 0 then begin
    Avisar('MySQL devolvio un error (codigo ' + IntToStr(codigo) + ').' + #13#10 +
           'Revisa el usuario y la contrasena. La base no se creo.', mbError);
    Exit;
  end;

  { Deja la conexion guardada al lado del ejecutable. }
  lineas := TStringList.Create;
  try
    lineas.Add('# Generado por el instalador de ElectroServis G.M.G.');
    lineas.Add('host=' + EditHost.Text);
    lineas.Add('puerto=3306');
    lineas.Add('base=electroservis_app');
    lineas.Add('usuario=' + EditUsuario.Text);
    lineas.Add('clave=' + EditClave.Text);
    lineas.SaveToFile(ExpandConstant('{app}\conexion.properties'));
  finally
    lineas.Free;
  end;

  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then begin
    if CrearBaseDeDatos() then
      Avisar('La base de datos electroservis_app quedo lista.' + #13#10 +
             'Entra con el usuario: Marcos   contrasena: 2307', mbInformation)
    else
      Avisar('El sistema se instalo, pero la base de datos no se pudo crear.' + #13#10 +
             'Mira la seccion de requisitos del README.', mbError);
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  { Se deja la base de datos y los respaldos: el usuario decide si los borra. }
  if CurUninstallStep = usPostUninstall then
    Log('La base de datos electroservis_app y la carpeta de respaldos no se borraron.');
end;
