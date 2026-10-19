# ⚡ ElectroServis G.M.G.

Sistema de escritorio para la administración de un taller de **servicio técnico** y **venta de equipos informáticos**: clientes, proveedores, inventario, órdenes de servicio, compras, ventas, caja, reportes e impresión de documentos (factura, ticket y etiquetas).

---

## 📌 ¿Qué es este proyecto?

Es el **proyecto de laboratorio de 3º año de BTI**. La idea fue hacer un sistema real que se pueda usar en un taller/negocio de informática, aplicando lo aprendido: interfaz gráfica de escritorio, base de datos relacional, reportes en PDF y control de stock.

El sistema se usa en el local **ELECTROSERVIS G.M.G.** (Caaguazú, Paraguay), así que los datos del emisor (RUC, timbrado, dirección) y los formatos de factura y ticket están hechos a medida.

> 📖 ¿Lo querés usar sin entrar al código? Mirá el **[Manual de usuario](MANUAL_DE_USUARIO.md)**: explica pantalla por pantalla cómo cargar clientes, vender, hacer una orden de servicio y sacar un respaldo.

### ¿Qué problema resuelve?

Un taller de informática normalmente anota todo en cuaderno: qué equipo entró, qué arreglo se le hizo, cuánto se cobró, qué repuestos se usaron y cuánta plata hay en caja. Este sistema reemplaza ese cuaderno:

- Registra los **equipos que entran** al taller y el estado de cada reparación.
- Maneja el **stock** de productos (repuestos y equipos para la venta) con descuento automático al vender.
- Emite **factura electrónica** y **ticket** con los datos reales de la empresa.
- Controla la **caja** (apertura, cierre, ingresos y egresos).
- Genera **reportes** en PDF para revisar cómo va el negocio.

---

## ✨ Módulos del sistema

| Módulo | Qué hace |
|---|---|
| 🔐 **Login** | Valida usuario y contraseña contra la base. Solo deja entrar si el usuario está `ACTIVO`. Permite cambiar entre tema oscuro y claro (se recuerda). |
| 🏠 **Dashboard** | Pantalla de inicio con 4 tarjetas: equipos ingresados, a retirar, terminados y stock total, más la lista de productos. |
| 👥 **Clientes** | Alta, edición, baja y búsqueda de clientes (nombre, apellido, cédula, teléfono, dirección, email). |
| 🚚 **Proveedores** | Igual que clientes, con RUC y datos de contacto. |
| 📦 **Inventario (Productos)** | Productos con código interno, **código de barras**, nombre, stock, **stock mínimo**, precio de compra/venta, **IVA (10 %, 5 % o exenta)**, proveedor, descripción y foto. |
| 🔧 **Equipos** | Equipos de los clientes (tipo, marca, modelo, número de serie) para asociarlos a una orden de servicio. |
| 🧾 **Órdenes de servicio** | Carga del equipo, falla reportada, estado (`PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `ENTREGADO`), presupuesto, **repuestos usados** (se descuentan del stock) y comprobante imprimible. |
| 🛒 **Compras** | Compra a proveedores con detalle de productos. **Suma stock** y asienta el egreso en la caja. |
| 💰 **Ventas** | Venta con detalle de productos, cliente, fecha, forma de pago y condición. **Descuenta stock**, asienta el ingreso en caja, permite imprimir **ticket** o **factura electrónica** y **anular** una venta. |
| 📊 **Caja** | Apertura y cierre de caja con monto inicial/final y todos los movimientos (ingresos y egresos). |
| 🖨️ **Reportes** | Reportes en PDF: inventario, clientes, ventas por rango de fechas, ticket de una venta, comprobante de orden y factura electrónica. Se abren solos al generarse. |
| ⚙️ **Ajustes** | Datos del emisor (razón social, nombre comercial, RUC + DV, dirección, ciudad, teléfono, timbrado y su vigencia, establecimiento, punto de expedición, rango de facturación y **logo**) y **respaldo / restauración** de la base. |
| 👤 **Usuarios** | Alta, edición y baja de usuarios, con rol y estado. **Solo lo ve un administrador.** |

### 🔎 Funciones que vale la pena mirar

- **Lector de código de barras** (en Ventas y Compras): se escanea el producto, se escribe el código en un campo exclusivo y al dar *Enter* el producto entra solo al detalle. Si se escanea dos veces el mismo producto con el mismo precio, **suma la cantidad** en el mismo renglón en vez de duplicarlo.
- **Generador de códigos EAN-13** (en Inventario): si un producto no trae código de fábrica, el botón *Generar* le crea uno válido (prefijo **784** = Paraguay) y el sistema te muestra la **vista previa** de cómo va a quedar.
- **Etiquetas para imprimir**: hoja A4 con **24 etiquetas** (3 columnas × 8 filas) con nombre, precio, código y código de barras dibujado. El dibujo del EAN-13 está hecho a mano, sin librerías externas.
- **Ventana de vuelto**: al cobrar en **efectivo** se abre sola una calculadora que muestra el total, va descontando lo que paga el cliente y avisa en verde el **vuelto** o en rojo lo que **falta**. Tiene botones rápidos (Justo, +10.000, +20.000, +50.000, +100.000) y se cierra con *Enter* o *Esc*. También se puede abrir a mano con el botón **Vuelto**.
- **Factura electrónica**: respeta el diseño real (colores, datos del emisor, logo, línea `CDC:`), calcula el **CDC** (Código de Control) y lo guarda en la base. El número de factura es correlativo y no se repite aunque se reimprima.
- **Verificación de stock**: no se puede vender más cantidad de la que hay; si no alcanza, avisa y no registra la venta.
- **Anular una venta**: con el botón *Anular* la venta **no se borra**: queda marcada como `ANULADA` (en la lista se ve en rojo), **devuelve el stock** de los productos y deja un **egreso en la caja**. Así el número de factura nunca se repite y queda el registro de que existió.
- **Aviso de reposición (stock mínimo)**: a cada producto se le puede cargar la cantidad a la que hay que reponer. Los que llegan a ese mínimo se ven **en rojo** en el inventario y en el panel principal, que además muestra cuántos productos hay que reponer.
- **Repuestos en las órdenes de servicio**: en *Órdenes de servicio* el botón **Repuestos usados** deja cargar qué productos del stock se usaron en la reparación. Al agregarlos se **descuenta el stock** y el precio se **suma al costo** de la orden (y al quitarlos, se devuelve al stock y se resta del costo).
- **Respaldo y restauración**: desde *Ajustes* se guarda toda la base en un archivo **`.sql`** (sin necesitar `mysqldump`) y se puede volver a cargar un respaldo anterior. La restauración va en una transacción: si algo falla, no se cambia nada.
- **Respaldo automático al cerrar**: si hay una sesión abierta y se cierra el sistema, guarda solo un respaldo de la base en `respaldos/` (con la fecha y hora en el nombre) y muestra un aviso con la ruta del archivo. Para no llenar la carpeta, **conserva solo los 10 respaldos más nuevos** (sean automáticos o manuales) y borra los demás.
- **Contraseñas con hash**: las claves no se guardan en texto plano sino como **SHA-256**. Las claves viejas se siguen aceptando y se convierten solas a hash la primera vez que ese usuario inicia sesión.
- **Permisos por rol**: hay dos roles, `ADMINISTRADOR` y `USUARIO`. Las pantallas de **Usuarios** y **Ajustes** quedan deshabilitadas para los usuarios comunes (y un administrador no puede sacarse el rol ni borrarse a sí mismo).
- **Controles de duplicados**: no deja cargar dos clientes con la misma cédula ni dos proveedores con el mismo RUC (comparando sin puntos ni guiones).

---

## 🧰 Tecnologías

| Cosa | Qué usa |
|---|---|
| Lenguaje | **Java 17** |
| Interfaz | **Java Swing** con **FlatLaf** (tema oscuro/claro moderno) |
| Formularios | **NetBeans Matisse** (`.form` + `AbsoluteLayout`) + MigLayout |
| Base de datos | **MySQL 8** |
| Acceso a datos | **JDBC** (mysql-connector-j), sin frameworks |
| Reportes | **JasperReports 6.20** → PDF |
| Fechas | **JCalendar** (JDateChooser) |
| Compilación | **Maven** |

---

## ✅ Requisitos

1. **JDK 17** (por ejemplo Eclipse Temurin / Adoptium).
2. **MySQL Server 8** (se puede instalar con MySQL Workbench).
3. **Apache NetBeans** (recomendado, porque los formularios están hechos en el diseñador) — o cualquier IDE con soporte Maven.
4. Conexión a internet **solo la primera vez**, para que Maven descargue las librerías.

---

## 🚀 Cómo dejarlo andando

### 1) Crear la base de datos

Desde la carpeta del proyecto, con la consola de MySQL:

```bash
mysql -u root -p < sql/crear_bd_app.sql
mysql -u root -p < sql/insertar_usuario.sql
```

- `crear_bd_app.sql` → crea la base **`electroservis_app`** con todas las tablas.
- `insertar_usuario.sql` → carga el usuario administrador para poder entrar.

### 2) (Opcional) Datos del emisor y actualizaciones

Estos scripts ya están aplicados en la base de la escuela, pero si arrancás de cero te sirven para dejar la factura y el ticket completos:

```bash
mysql -u root -p electroservis_app < sql/datos_empresa_real.sql
mysql -u root -p electroservis_app < sql/actualizar_factura_electronica.sql
mysql -u root -p electroservis_app < sql/actualizar_codigo_barras.sql
mysql -u root -p electroservis_app < sql/actualizar_anular_venta.sql
mysql -u root -p electroservis_app < sql/actualizar_stock_minimo.sql
mysql -u root -p electroservis_app < sql/actualizar_repuestos_orden.sql
```

> ⚠️ Son scripts de actualización: si los corrés dos veces, MySQL avisa que la columna (o la tabla) ya existe. No rompe nada, se ignora el aviso.
>
> 💡 En **PowerShell** no funciona el `<`, hay que usar: `Get-Content "sql\actualizar_stock_minimo.sql" -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root electroservis_app`

### 3) Configurar la conexión

En `src/main/java/com/electroservisgmg/conexion/Conexion.java` hay que poner **tu** usuario y contraseña de MySQL:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/electroservis_app?...";
private static final String USER = "root";
private static final String PASS = "tu_contraseña";
```

> Es lo primero que hay que revisar si el login se queda pensando o tira error de conexión.

### 4) Ejecutar

**Opción A — NetBeans (la más fácil):**

1. *File → Open Project* y elegir la carpeta del proyecto (detecta el `pom.xml`).
2. Botón **Run** (F6). Se abre la pantalla de Login.

**Opción B — línea de comandos:**

```bash
mvn compile exec:java -Dexec.mainClass=com.electroservisgmg.ElectroServisGMG
```

---

## 💾 Instalación con el instalador (.exe)

Si no querés armarlo a mano, el proyecto trae un **instalador para Windows** que deja todo listo de una sola vez: copia el sistema (con su propio Java adentro, no hace falta tener JDK), crea los accesos directos y **arma la base de datos sola**.

**Archivo:** `instalador/Output/ElectroServisGMG-Setup.exe`

**Qué hace al ejecutarlo:**

1. Pide la **contraseña del `root` de MySQL** (por defecto `2307`) y, opcionalmente, la carpeta de instalación.
2. Instala en **`%LOCALAPPDATA%\ElectroServisGMG`** (no necesita permisos de administrador).
3. Crea los accesos directos en el **Menú Inicio** y en el **Escritorio**, con el ícono del logo del negocio.
4. Busca MySQL y **crea la base** (`electroservis_app`) corriendo el script `sql/crear_bd_instalador.sql`: crea las 15 tablas y carga solo lo mínimo para arrancar (usuario administrador, datos del emisor y la caja inicial). **La instalación queda vacía de movimiento** (sin productos, clientes, ventas, compras ni órdenes). Si no encuentra MySQL, avisa.
5. Escribe un archivo **`conexion.properties`** al lado del ejecutable con la clave que se le indicó, para que el sistema se conecte sin tocar el código.
6. Deja un **desinstalador** normal (Aparece y Funciones de Windows). Al desinstalar **no borra la base** ni la carpeta `respaldos/` (queda por si querés volver a instalar); sí quita los accesos directos y el resto del programa.

**Requisito:** tener **MySQL Server 8** instalado (el instalador no trae el servidor; solo crea y carga la base).

### Cómo se genera (para el equipo)

Los scripts para armarlo están en `instalador/` y necesitan **jpackage** (viene en el JDK 17) + **Inno Setup 6**:

```powershell
# 0) si no tenes Inno Setup, instalalo (una sola vez)
winget install JRSoftware.InnoSetup

# 1) compila, arma el ícono y el ejecutable autocontenido con jpackage
powershell -ExecutionPolicy Bypass -File instalador\build-instalador.ps1

# 2) compila el instalador con Inno Setup
#    (ISCC.exe queda en "%LOCALAPPDATA%\Programs\Inno Setup 6" o en "C:\Program Files (x86)\Inno Setup 6")
& "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe" instalador\ElectroServis.iss
```

El resultado queda en `instalador\Output\ElectroServisGMG-Setup.exe`.

---

## 🔑 Usuario de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `Marcos` | `2307` | ADMINISTRADOR |

### Roles

- **`ADMINISTRADOR`**: ve todo (incluidos *Usuarios* y *Ajustes*).
- **`USUARIO`**: usa el sistema completo menos *Usuarios* y *Ajustes* (esos botones quedan apagados en el menú).

Los usuarios nuevos se crean como `USUARIO` por defecto, y los usuarios que ya existían sin rol cargado se toman como administradores (así la actualización de la base no deja a nadie afuera).

---

## 🗂️ Estructura del proyecto

```
ElectroServisGMG/
├── pom.xml                      → dependencias y configuración de Maven
├── README.md                    → este archivo
├── MANUAL_DE_USUARIO.md         → manual paso a paso para usar el sistema
├── sql/                         → scripts de base de datos
│   ├── crear_bd_app.sql             (crea la base y las tablas)
│   ├── crear_bd_instalador.sql      (igual, versión limpia: la usa el instalador)
│   ├── insertar_usuario.sql         (usuario administrador inicial)
│   ├── datos_empresa_real.sql       (datos del emisor para factura/ticket)
│   ├── actualizar_factura_electronica.sql
│   ├── actualizar_codigo_barras.sql
│   ├── actualizar_anular_venta.sql      (columna estado de la venta)
│   ├── actualizar_stock_minimo.sql      (aviso de reposición)
│   └── actualizar_repuestos_orden.sql   (repuestos usados en una orden)
├── imagenes_productos/          → fotos de los productos de ejemplo
├── respaldos/                   → carpetas de respaldos .sql (no va a git)
├── instalador/                  → scripts para armar el instalador .exe (jpackage + Inno Setup)
└── src/main/
    ├── java/com/electroservisgmg/
    │   ├── ElectroServisGMG.java    → main (arranca el login)
    │   ├── clases/                  → las "cosas" del sistema (Producto, Venta, Cliente, ...)
    │   ├── conexion/                → Conexion.java (JDBC a MySQL)
    │   ├── formularios/             → todas las pantallas + UiTheme (colores y estilos)
    │   ├── metodos/                 → la lógica y el SQL de cada cosa
    │   └── reportes/                → ReportesMetodos.java (arma los PDF)
    └── resources/
        ├── iconos/                  → iconos y logo de la empresa
        └── reportes/                → plantillas .jrxml de JasperReports
```

### ¿Cómo está organizado el código?

Se usan **tres capas**, para que cada cosa esté en un solo lugar:

1. **`clases/`** → los objetos del sistema. Solo guardan datos (`Producto`, `Cliente`, `Venta`...). Ninguno sabe de MySQL ni de pantallas.
2. **`metodos/`** → la lógica y el acceso a datos (`ProductoMetodos`, `VentaMetodos`...). Acá están los `INSERT`, `SELECT`, `UPDATE` y las reglas (por ejemplo, descontar stock al vender).
3. **`formularios/`** → las pantallas (Swing). No hacen SQL: le piden todo a `metodos/`.

Y aparte:

- **`reportes/`** → arma e imprime los documentos con JasperReports.
- **`UiTheme`** → centraliza los colores, las fuentes y los formatos (por ejemplo `UiTheme.gs(225000)` devuelve `Gs. 225.000`). **Todo el diseño visual sale de ahí**, así que si hay que cambiar un color, se cambia en un solo archivo.

---

## 🗄️ Base de datos

Base: **`electroservis_app`** (15 tablas)

| Tabla | Guarda |
|---|---|
| `usuario` | usuarios del sistema (con rol y estado; la clave va con hash SHA-256) |
| `cliente` | clientes |
| `proveedor` | proveedores |
| `producto` | productos (stock, stock mínimo, precios, IVA, código de barras, foto) |
| `tipo_equipo` | tipos de equipo (celular, notebook, TV, ...) |
| `equipo` | equipos de los clientes |
| `ordenservicio` | órdenes del taller |
| `orden_repuesto` | repuestos (productos del stock) usados en cada orden |
| `venta` / `detalleventa` | cabecera y renglones de cada venta (`venta.estado`: `REGISTRADA` o `ANULADA`) |
| `compra` / `detallecompra` | cabecera y renglones de cada compra |
| `caja` / `caja_movimiento` | apertura/cierre de caja y sus movimientos |
| `empresa` | datos del emisor para la factura y el ticket |

---

## 🖨️ Documentos que imprime

| Documento | Formato |
|---|---|
| **Factura electrónica** | Media carta, con datos del emisor, logo, cliente, renglones con IVA por producto y línea `CDC:` |
| **Ticket de venta** | Rollo de **80 mm**, encabezado `ELECTROSERVIS G.M.G.`, con IVA chico debajo del total |
| **Comprobante de orden** | Original y duplicado para el taller |
| **Etiquetas de productos** | Hoja A4, 24 etiquetas por hoja (66 × 34 mm) |
| **Reportes** | Inventario, clientes y ventas por rango de fechas |

Los PDF se generan en la carpeta `reportes_generados/` y se abren automáticamente.

---

## 🧑‍💻 Notas para trabajar en equipo

### Sobre los formularios (¡importante!)

Las pantallas están hechas en el **diseñador de NetBeans (Matisse)**. En los archivos `.java` vas a ver bloques así:

```java
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
...
// </editor-fold>//GEN-END:initComponents
```

**Ese bloque lo genera NetBeans solo: no se toca a mano**, porque se borra cada vez que se guarda el diseño. Todo el código propio va **afuera** de ese bloque (los `addActionListener`, los cálculos, el SQL, etc.).

Y el diseño se cambia en la pestaña **Design** del `.form`, no escribiendo coordenadas a mano.

### Cuidado con estos archivos

- **No tocar** `nbproject/private/`, `target/` ni `tema.cfg` (son archivos locales, ya están en `.gitignore`).
- Los reportes `.jrxml` van en `src/main/resources/reportes/` (no en la carpeta de Java).
- Si agregás una librería, hacelo desde `pom.xml` para que a todos nos baje lo mismo.

### Convenciones que se usaron

- Nombres de clases en **singular y en español** (`Producto`, `Cliente`, `VentaMetodos`).
- Un formulario por pantalla, prefijo `Form` (`FormVentas`, `FormProductos`...).
- Los montos son `Double` y se muestran siempre con punto de miles: `Gs. 225.000`.

---

## ⚠️ Limitaciones conocidas

Cosas que quedaron para más adelante (sirven para seguir practicando):

1. **La conexión a MySQL sale de un archivo `conexion.properties`** que está al lado del ejecutable (así lo escribe el instalador con la clave que le pusiste). Si el archivo no existe, `Conexion.java` usa `root` / `2307` como valor por defecto (el de la PC de la escuela).
2. **El respaldo guarda los datos, no la estructura**: alcanza para volver atrás en un sistema ya instalado (restaura sobre las mismas tablas). Para armar la base desde cero se usa `crear_bd_app.sql`.
3. **La factura electrónica no está conectada a SIFEN/ekuatía**: imprime el formato con un CDC generado internamente, pero no se envía ni se firma digitalmente.
4. **La tabla `cliente` no tiene columna `RUC`**: la factura usa la cédula del cliente. **La consulta de RUC por internet** (para autocompletar el nombre desde el RUC) quedó sin implementar.
5. **El lector de código de barras está en Ventas y Compras**: en las otras pantallas el código se carga a mano.
6. **El vuelto no se guarda en la base**: la ventana de vuelto es una calculadora (la venta guarda el total y la forma de pago).
7. Los formularios usan posiciones fijas (AbsoluteLayout), así que la ventana no se adapta sola si se achica mucho (las pantallas están pensadas para 1100 × 700 aprox.).

---

## 👨‍🎓 Autores

- **Marcos Aquino**
- **Matías Bogado**

Proyecto de laboratorio — **3º año de BTI — 2026**
