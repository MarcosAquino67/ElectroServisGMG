# ⚡ ElectroServis G.M.G.

Sistema de escritorio para la administración de un taller de **servicio técnico** y **venta de equipos informáticos**: clientes, proveedores, inventario, órdenes de servicio, compras, ventas, caja, reportes e impresión de documentos (factura, ticket y etiquetas).

---

## 📌 ¿Qué es este proyecto?

Es el **proyecto de laboratorio de 3º año de BTI**. La idea fue hacer un sistema real que se pueda usar en un taller/negocio de informática, aplicando lo aprendido: interfaz gráfica de escritorio, base de datos relacional, reportes en PDF y control de stock.

El sistema se usa en el local **ELECTROSERVIS G.M.G.** (Caaguazú, Paraguay), así que los datos del emisor (RUC, timbrado, dirección) y los formatos de factura y ticket están hechos a medida.

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
| 📦 **Inventario (Productos)** | Productos con código interno, **código de barras**, nombre, stock, precio de compra/venta, **IVA (10 %, 5 % o exenta)**, proveedor, descripción y foto. |
| 🔧 **Equipos** | Equipos de los clientes (tipo, marca, modelo, número de serie) para asociarlos a una orden de servicio. |
| 🧾 **Órdenes de servicio** | Carga del equipo, falla reportada, estado (`PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `ENTREGADO`), presupuesto y comprobante imprimible. |
| 🛒 **Compras** | Compra a proveedores con detalle de productos. **Suma stock** y asienta el egreso en la caja. |
| 💰 **Ventas** | Venta con detalle de productos, cliente, fecha, forma de pago y condición. **Descuenta stock**, asienta el ingreso en caja y permite imprimir **ticket** o **factura electrónica**. |
| 📊 **Caja** | Apertura y cierre de caja con monto inicial/final y todos los movimientos (ingresos y egresos). |
| 🖨️ **Reportes** | Reportes en PDF: inventario, clientes, ventas por rango de fechas, ticket de una venta, comprobante de orden y factura electrónica. Se abren solos al generarse. |
| ⚙️ **Ajustes** | Datos del emisor: razón social, nombre comercial, RUC + DV, dirección, ciudad, teléfono, timbrado y su vigencia, establecimiento, punto de expedición, rango de facturación y **logo**. |
| 👤 **Usuarios** | Alta, edición y baja de usuarios, con rol y estado. |

### 🔎 Funciones que vale la pena mirar

- **Lector de código de barras** (en Ventas y Compras): se escanea el producto, se escribe el código en un campo exclusivo y al dar *Enter* el producto entra solo al detalle. Si se escanea dos veces el mismo producto con el mismo precio, **suma la cantidad** en el mismo renglón en vez de duplicarlo.
- **Generador de códigos EAN-13** (en Inventario): si un producto no trae código de fábrica, el botón *Generar* le crea uno válido (prefijo **784** = Paraguay) y el sistema te muestra la **vista previa** de cómo va a quedar.
- **Etiquetas para imprimir**: hoja A4 con **24 etiquetas** (3 columnas × 8 filas) con nombre, precio, código y código de barras dibujado. El dibujo del EAN-13 está hecho a mano, sin librerías externas.
- **Ventana de vuelto**: al cobrar en **efectivo** se abre sola una calculadora que muestra el total, va descontando lo que paga el cliente y avisa en verde el **vuelto** o en rojo lo que **falta**. Tiene botones rápidos (Justo, +10.000, +20.000, +50.000, +100.000) y se cierra con *Enter* o *Esc*. También se puede abrir a mano con el botón **Vuelto**.
- **Factura electrónica**: respeta el diseño real (colores, datos del emisor, logo, línea `CDC:`), calcula el **CDC** (Código de Control) y lo guarda en la base. El número de factura es correlativo y no se repite aunque se reimprima.
- **Verificación de stock**: no se puede vender más cantidad de la que hay; si no alcanza, avisa y no registra la venta.

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
```

> ⚠️ Son scripts de actualización: si los corrés dos veces, MySQL avisa que la columna ya existe. No rompe nada, se ignora el aviso.

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

## 🔑 Usuario de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `Marcos` | `2307` | ADMINISTRADOR |

---

## 🗂️ Estructura del proyecto

```
ElectroServisGMG/
├── pom.xml                      → dependencias y configuración de Maven
├── README.md                    → este archivo
├── sql/                         → scripts de base de datos
│   ├── crear_bd_app.sql             (crea la base y las tablas)
│   ├── insertar_usuario.sql         (usuario administrador inicial)
│   ├── datos_empresa_real.sql       (datos del emisor para factura/ticket)
│   ├── actualizar_factura_electronica.sql
│   └── actualizar_codigo_barras.sql
├── imagenes_productos/          → fotos de los productos de ejemplo
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

Base: **`electroservis_app`** (14 tablas)

| Tabla | Guarda |
|---|---|
| `usuario` | usuarios del sistema (con rol y estado) |
| `cliente` | clientes |
| `proveedor` | proveedores |
| `producto` | productos (stock, precios, IVA, código de barras, foto) |
| `tipo_equipo` | tipos de equipo (celular, notebook, TV, ...) |
| `equipo` | equipos de los clientes |
| `ordenservicio` | órdenes del taller |
| `venta` / `detalleventa` | cabecera y renglones de cada venta |
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

1. **Las contraseñas se guardan en texto plano.** Lo correcto sería guardarlas con un hash (por ejemplo SHA-256 o bcrypt).
2. **Los permisos por rol no se aplican**: cualquier usuario que entra puede hacer todo. La base ya guarda el rol, falta usarlo para ocultar/deshabilitar botones.
3. **El usuario y la contraseña de MySQL están escritos en el código** (`Conexion.java`). Lo ideal sería un archivo de configuración aparte.
4. **La factura electrónica no está conectada a SIFEN/ekuatía**: imprime el formato con un CDC generado internamente, pero no se envía ni se firma digitalmente.
5. **La tabla `cliente` no tiene columna `RUC`**: la factura usa la cédula del cliente. **La consulta de RUC por internet** (para autocompletar el nombre desde el RUC) quedó sin implementar.
6. Los formularios usan posiciones fijas (AbsoluteLayout), así que la ventana no se adapta sola si se achica mucho (las pantallas están pensadas para 1100 × 700 aprox.).

---

## 👨‍🎓 Autores

- **Marcos Aquino**
- **Matías** — *(completá tu apellido 😄)*

Proyecto de laboratorio — **3º año de BTI — 2026**
