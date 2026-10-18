# 📖 Manual de usuario — ElectroServis G.M.G.

Guía práctica para usar el sistema en el día a día del taller. Está pensada para alguien que
recién abre el programa, sin necesidad de saber nada de programación.

---

## 1. Cómo entrar

1. Abrir el sistema (`ElectroServisGMG`). Aparece la pantalla de **Login**.
2. Escribir el **usuario** y la **contraseña** y dar *Enter*.
3. Si los datos están bien, entra directo al **Panel principal**.

> Si no entra: revisar que el usuario esté en estado **ACTIVO** (lo maneja un administrador desde
> *Usuarios*) y que la contraseña esté bien escrita. Las claves se guardan cifradas (hash SHA-256),
> así que desde la base **no se pueden leer**; si se olvida una, un administrador se la cambia desde
> la pantalla *Usuarios*.

Desde el login también se puede cambiar entre **tema oscuro** y **tema claro**. El sistema recuerda
la última elección.

### Los dos roles

| Rol | Qué puede hacer |
|---|---|
| **ADMINISTRADOR** | Todo. Además ve los botones *Usuarios* y *Ajustes*. |
| **USUARIO** | Todo el trabajo diario (clientes, inventario, órdenes, ventas, compras, caja, reportes). Los botones *Usuarios* y *Ajustes* le quedan **apagados**. |

Un administrador **no puede** sacarse a sí mismo el rol de administrador ni borrarse a sí mismo,
para no quedarse sin nadie que pueda administrar el sistema.

---

## 2. La pantalla principal (Panel)

Al entrar se ve el **Panel**, con cuatro tarjetas arriba:

- 📥 **Equipos ingresados**: órdenes pendientes o en proceso.
- 📦 **A retirar**: órdenes terminadas, esperando que el cliente venga.
- ✅ **Terminados**: equipos ya entregados.
- 📱 **Stock total**: suma de unidades en inventario. Si algún producto llegó a su **stock mínimo**,
  acá aparece la cantidad a reponer y **en la lista de abajo esos productos se ven en rojo**.

Abajo está la lista de productos con su stock, stock mínimo, precio y descripción. Pasar el mouse
por encima de la etiqueta de la tarjeta muestra **cuáles** productos hay que reponer.

### El menú de la izquierda

| Botón | Pantalla |
|---|---|
| Panel | Inicio / resumen |
| Órdenes | Órdenes de servicio (taller) |
| Ventas | Vender y facturar |
| Inventario | Productos (stock, precios) |
| Compras | Compras a proveedores |
| Clientes | Datos de los clientes |
| Proveedores | Datos de los proveedores |
| Equipos | Equipos de los clientes |
| Caja | Apertura, cierre y movimientos |
| Reportes | Listados en PDF |
| Usuarios | Solo administrador |
| Ajustes | Solo administrador |

Abajo de todo se ve **quién está conectado** con su rol.

---

## 3. Orden recomendado para empezar (de cero)

1. **Ajustes** → cargar los datos de la empresa (razón social, RUC, timbrado, rango de facturas, logo).
2. **Proveedores** → cargar los proveedores.
3. **Inventario** → cargar los productos con su stock y precios.
4. **Clientes** → cargar los clientes.
5. **Caja** → **abrir la caja** con el monto inicial (sin esto, las ventas y compras no encuentran caja abierta).
6. Ya se puede **vender**, **comprar** y **tomar órdenes de servicio**.

---

## 4. Clientes y proveedores

Son pantallas parecidas: una lista arriba y el formulario abajo.

- **Nuevo**: limpia el formulario para cargar uno nuevo.
- **Guardar**: da de alta o guarda los cambios.
- **Editar**: con una fila seleccionada, trae los datos al formulario para corregirlos.
- **Eliminar**: borra el registro elegido.
- **Buscar**: filtra la lista mientras se escribe (nombre, apellido, cédula/RUC, etc.).

**Controles de duplicados:**

- No deja guardar dos **clientes con la misma cédula**.
- No deja guardar dos **proveedores con el mismo RUC**.

La comparación ignora puntos, guiones y espacios, así que `1.234.567` y `1234567` se toman como la
misma cédula. Si el sistema avisa que ya existe, hay que buscarlo en la lista y editarlo en vez de
cargarlo de nuevo.

---

## 5. Inventario (Productos)

Cada producto tiene: código interno, **código de barras**, nombre, **stock**, **stock mínimo**,
precio de compra, precio de venta, **IVA** (10 %, 5 % o exenta), proveedor, descripción y foto.

### Código de barras

- Se puede **escanear** el código del producto y queda cargado.
- Si el producto no trae código de fábrica, el botón **Generar** crea uno válido (EAN-13, con prefijo
  **784** de Paraguay) y muestra la **vista previa** de cómo va a quedar el símbolo.

### Stock mínimo (aviso de reposición)

- **Stock mín.**: la cantidad a la que hay que reponer. Un **0** significa "no avisar".
- Cuando el stock llega a ese número o menos, la fila se ve **en rojo** en el inventario y en el Panel,
  y el Panel muestra cuántos productos hay que reponer.

### Etiquetas

El botón de **etiquetas** genera una hoja **A4 con 24 etiquetas** (3 columnas × 8 filas) con nombre,
precio, código y el código de barras dibujado. Se abre el PDF solo y se imprime desde ahí.

### Ojo con el stock

El stock lo toca el sistema solo: **baja al vender**, **sube al comprar**, **baja con los repuestos
que se cargan en una orden de servicio** y **vuelve** si se anula una venta o se quita un repuesto.
No hace falta corregirlo a mano en el día a día (sí al hacer un recuento físico).

---

## 6. Equipos

Los equipos de los clientes (tipo, marca, modelo, número de serie). Se cargan para poder asociarlos
a una orden de servicio. Si el equipo de un cliente ya está cargado de antes, se reutiliza.

---

## 7. Órdenes de servicio (el taller)

Es el corazón del negocio: qué equipo entró, qué tiene, cuánto se cobra y cuándo se entrega.

### Cargar una orden

1. *Órdenes* → **Nuevo**.
2. Elegir **Cliente** y **Equipo** (si el equipo no está, se carga antes en *Equipos*).
3. Escribir el **diagnóstico** (la falla que reporta el cliente) y las **observaciones**.
4. Poner el **costo** de la reparación (mano de obra).
5. **Guardar**.

El estado arranca en `PENDIENTE`.

### Estados

| Estado | Significa |
|---|---|
| `PENDIENTE` | Entró y todavía no se empezó. |
| `EN_PROCESO` | Se está trabajando. |
| `COMPLETADO` | Está listo, esperando que el cliente lo retire. |
| `ENTREGADO` | El cliente ya se lo llevó (el botón **Retirar** marca esto y guarda la fecha de entrega). |

### Repuestos usados 🔧

Con la orden elegida en la lista, el botón **Repuestos usados** abre una ventana donde se carga qué
productos del stock se usaron en la reparación:

- **Agregar**: se elige el producto (el desplegable muestra el **stock disponible**), se pone la
  cantidad y se agrega. El sistema:
  1. **descuenta el stock** (si no alcanza, avisa y no agrega),
  2. guarda el repuesto con el **precio del día**,
  3. **suma el importe al costo de la orden**.
- **Quitar el seleccionado**: devuelve el repuesto al **stock** y lo resta del costo.
- **Nuevo repuesto del catálogo…**: si el repuesto todavía no está en el inventario, se carga ahí
  mismo (nombre, precio y cantidad que entra) y queda disponible.

Abajo del formulario de la orden aparece un resumen: *"2 repuestos (3 unidades) · Gs. 250.000 ya
incluidos en el costo de la reparación"*.

### Comprobante

El botón **Imprimir orden** genera el comprobante por **duplicado** (original para el taller y copia
para el cliente) con los datos del equipo, la falla y el costo.

---

## 8. Compras

1. *Compras* → **Nuevo**, elegir **proveedor** y la **fecha**.
2. Cargar los productos del detalle (con el lector de código de barras o a mano) y las cantidades.
3. **Guardar**.

La compra **suma el stock** de cada producto y deja el **egreso en la caja** (requiere caja abierta).

---

## 9. Ventas

1. *Ventas* → **Nuevo**.
2. Elegir el **cliente** (o consumidor final, según la pantalla), la **fecha**, la **forma de pago**
   (contado / crédito) y la **condición**.
3. Cargar los productos del detalle:
   - con el **lector de código de barras**: se escanea en el campo del código y el producto entra solo
     al dar *Enter*. Si se escanea dos veces el mismo producto con el mismo precio, **suma la cantidad**
     en el mismo renglón;
   - o eligiéndolo de la lista.
4. **Guardar**.

Al guardar, el sistema **descuenta el stock**, asienta el **ingreso en la caja** y calcula el **IVA**
de cada renglón.

> ⛔ Si no hay stock suficiente, la venta **no se registra** y el sistema avisa cuánto queda.

### Vuelto

Al cobrar en **efectivo** se abre sola la **ventana de vuelto**: muestra el total y va calculando
cuánto hay que devolver mientras se escribe con cuánto paga el cliente. En **verde** el vuelto, en
**rojo** lo que falta. Tiene botones rápidos (*Justo*, *+10.000*, *+20.000*, *+50.000*, *+100.000*)
y se cierra con *Enter* o *Esc*. También se puede abrir a mano con el botón **Vuelto**.

*(El vuelto no se guarda en la base: es una calculadora para no equivocarse con el cambio.)*

### Imprimir

- **Ticket**: rollo de **80 mm**, con el IVA chico debajo del total.
- **Factura electrónica**: con los datos del emisor, el logo, los renglones con IVA por producto y la
  línea `CDC:`. El número de factura es **correlativo** y no se repite aunque se reimprima.

### Anular una venta

Si una venta se cargó mal o el cliente la devolvió:

1. Elegir la venta en la lista.
2. Botón **Anular** → confirmar.

Qué pasa al anular:

- la venta **no se borra**: queda marcada como **ANULADA** y se ve en **rojo** en la lista;
- los productos **vuelven al stock**;
- se registra un **egreso en la caja** por el total;
- **no se puede imprimir** el ticket ni la factura de una venta anulada;
- el número de factura **no se reutiliza** (nunca se repite).

Una venta anulada no aparece en el reporte de ventas.

---

## 10. Caja

- **Abrir caja**: se carga el monto inicial del día.
- Durante el día, la caja se mueve sola con cada **venta** (ingreso), **compra** (egreso) y
  **anulación** (egreso). También se pueden cargar movimientos a mano.
- **Cerrar caja**: se carga el monto final y queda el cierre guardado.

> 💡 Si al vender o comprar el sistema dice que no hay caja abierta, hay que ir a *Caja* y abrirla.

---

## 11. Reportes

Se elige el reporte y se genera el **PDF**, que se abre automáticamente:

- **Inventario** (con el stock actual).
- **Clientes**.
- **Ventas por rango de fechas** (no incluye las ventas anuladas).
- **Ticket** de una venta.
- **Comprobante** de una orden de servicio.
- **Factura electrónica**.

Los PDF se guardan en la carpeta `reportes_generados/`.

---

## 12. Ajustes (solo administrador)

### Empresa y timbrado

Datos que salen impresos en la factura y el ticket: razón social, nombre comercial, RUC + DV,
vigencia, tipo de contribuyente, establecimiento, punto de expedición, rango de facturas, dirección,
ciudad, teléfono, correo, **actividades económicas** y **logo**. Se guardan con el botón
**Guardar datos**.

### Apariencia y tema

- Botones **Oscuro** / **Claro** para cambiar el tema al instante.
- **Al abrir el sistema**: *Siempre oscuro*, *Siempre claro* o *Recordar último tema*.

### Respaldo de la base 🛟

El botón **Respaldo de la base…** abre la ventana de respaldo:

- **Respaldar ahora…**: elige dónde guardar y arma un archivo **`.sql`** con **todos los datos** del
  sistema. Por defecto se guardan en la carpeta `respaldos/`, con el nombre
  `electroservis_AAAAMMDD_HHMMSS.sql`. La ventana muestra cuándo fue el **último respaldo**.
- **Restaurar desde archivo…**: vuelve a cargar los datos de un respaldo anterior. **Reemplaza todos
  los datos actuales** por los del archivo, así que pide confirmación. Si algo falla en el medio,
  **no se cambia nada** (va todo en una transacción).
- **Abrir la carpeta de respaldos**: abre la carpeta en el explorador de Windows.

> 💡 Conviene hacer un respaldo **al cerrar la semana** y antes de tocar algo importante.
> Después de restaurar, abrir de nuevo el Panel para ver los datos al día.

---

## 13. Usuarios (solo administrador)

- **Nuevo**: nombre, usuario, contraseña (mínimo 4 caracteres), **rol** y estado.
- **Editar**: con una fila seleccionada. Al guardar hay que volver a escribir la contraseña.
- **Eliminar**: borra el usuario elegido.
- La lista muestra **ID, Nombre, Usuario, Rol y Estado**.

Reglas:

- Los usuarios nuevos se crean como **USUARIO** por defecto.
- Un administrador **no se puede** sacar el rol de administrador ni borrarse a sí mismo.
- No se pueden cargar dos usuarios con el mismo nombre de usuario.

---

## 14. Problemas comunes

| Síntoma | Qué revisar |
|---|---|
| El login se queda pensando o tira error de conexión | Que MySQL esté encendido y que `Conexion.java` tenga el usuario y la contraseña correctos. |
| "No hay una caja abierta" | Ir a *Caja* y abrirla. |
| No deja vender / cargar un repuesto | No hay stock suficiente: el mensaje dice cuántas unidades quedan. |
| No aparece el botón *Usuarios*/*Ajustes* | El usuario conectado es un **USUARIO**, no un administrador. |
| Un producto aparece en rojo en la lista | Su stock llegó al **stock mínimo**: hay que reponer. |
| La factura "salta" un número | Es normal: los números **anulados o reimpresos no se reutilizan**. |
| No se puede imprimir el ticket/factura de una venta | Esa venta está **ANULADA**. |
| Se cargó mal una venta | *Anular* la venta (devuelve el stock y asienta el egreso), no hace falta borrarla. |

---

## 15. Glosario

| Palabra | Qué es |
|---|---|
| **Stock** | Cantidad de unidades que hay de un producto. |
| **Stock mínimo** | Cantidad a la que hay que reponer ese producto. |
| **Repuesto** | Producto del stock que se usa para reparar un equipo. |
| **Orden de servicio** | El trabajo de reparación de un equipo de un cliente. |
| **CDC** | Código de Control que lleva la factura electrónica. |
| **IVA** | Impuesto: en el sistema cada producto puede ser 10 %, 5 % o exenta. |
| **Vuelto** | La plata que se le devuelve al cliente cuando paga con un billete mayor. |
| **Respaldo** | Copia de seguridad de todos los datos del sistema. |

---

**ElectroServis G.M.G.** — Proyecto de laboratorio, 3.º año BTI (2026).
Autores: **Marcos Aquino** y **Matías Bogado**.
