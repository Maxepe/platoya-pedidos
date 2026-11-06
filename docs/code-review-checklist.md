# Checklist de code review

Aplicado al [PR #1](https://github.com/Maxepe/platoya-pedidos/pull/1) (`refactor/order-processor` → `main`). Cada ítem tiene un criterio de aceptación verificable e indica quién lo verifica.

| # | Ítem | Criterio de aceptación | Verificación | Base | Refactor |
|---|---|---|---|---|---|
| 1 | Responsabilidad única | Cada clase de servicio tiene un solo motivo de cambio, enunciable en una frase | Manual | ✗ | ✓ |
| 2 | Tamaño de funciones | Ningún método supera las 15 líneas, salvo excepción justificada | Checkstyle `MethodLength` | ✗ (4) | ✓ (1 justificada, CR-06) |
| 3 | Complejidad | Ningún método supera una complejidad ciclomática de 5 | Checkstyle `CyclomaticComplexity` | ✗ (máx. 27) | ✓ (máx. 4) |
| 4 | Parámetros | Ningún método tiene más de 4 parámetros | Checkstyle `ParameterNumber` | ✗ (8) | ✓ |
| 5 | Inyección de dependencias | Solo por constructor; sin `new` de servicios ni `@Autowired` en campos | Checkstyle `RegexpSinglelineJava` | ✗ (8) | ✓ (tras CR-02) |
| 6 | Abiertos a extensión | Un tipo de cliente nuevo se agrega sin modificar clases existentes | Manual | ✗ | ✓ |
| 7 | Manejo de errores | Sin `catch (Exception)`; excepciones de dominio con un estado HTTP coherente | Checkstyle `IllegalCatch` + manual | ✗ (3) | ✓ |
| 8 | Integridad de datos | Toda operación que modifica más de una entidad es transaccional | Manual + prueba C14 | ✗ | ✓ (tras CR-01) |
| 9 | Nombres | Variables locales de 3 caracteres o más y que revelen intención | Checkstyle `LocalVariableName` + manual | ✗ (21) | ✓ |
| 10 | Constantes | Sin literales numéricos de negocio en el código | Checkstyle `MagicNumber` | ✗ (11) | ✓ |
| 11 | Duplicación | Sin bloques de 3 o más líneas repetidos | Manual | ✗ | ✓ |
| 12 | Pruebas | Suite en verde y cobertura de líneas ≥ 85 % | CI (JaCoCo `check`) | — (24, sin unitarias) | ✓ (45, 94,9 %) |
| 13 | Contrato externo | Sin cambios en la API salvo los declarados (D1 y D2) | Suite de caracterización | — | ✓ (corrida 2) |

Siete de los trece criterios están automatizados: los verifican el hook de pre-commit y el CI en cada commit y en cada PR.

## Observaciones del PR #1

| ID | Archivo | Detectado por | Observación | Severidad | Acción |
|---|---|---|---|---|---|
| CR-01 | `InventoryService` | Revisión manual | `reserve()` depende de correr dentro de la transacción de `createOrder()`, pero nada lo garantiza. Propuesta: `Propagation.MANDATORY`. | Alta | **Corregido** en `55cecd4`. |
| CR-02 | `ProductController` | Checkstyle (reporte del CI) | `@Autowired` sobre el campo `productRepository`; no cumple el ítem 5. | Baja | **Corregido** en `55cecd4`: inyección por constructor. Comentario general del PR, porque el archivo no está en el diff. |
| CR-03 | `OrderProcessor.createOrder` | Revisión manual | Los avisos se envían dentro de la transacción. | Media | **Pendiente aceptado:** `@TransactionalEventListener(AFTER_COMMIT)`. |
| CR-04 | `OrderController` | Revisión manual | Se expone la entidad JPA `Order` como respuesta. | Media | **Pendiente aceptado:** DTO de respuesta. |
| CR-05 | `model/*` | Revisión manual | Los montos se manejan con `double`. | Media | **Pendiente aceptado:** `Money` con `BigDecimal` en una versión nueva de la API. |
| CR-06 | `Order.confirm()` | Checkstyle `MethodLength` | 19 líneas, por encima del máximo de 15. | Baja | **Excepción justificada** en `55cecd4`: 15 asignaciones sin lógica; `@SuppressWarnings("checkstyle:MethodLength")` solo en ese método. |
| CR-07 | `CustomerPricingPolicies` | Revisión manual | Un tipo `"vip"` en minúscula se cotiza como REGULAR sin aviso. | Baja | **Se mantiene:** es el comportamiento de la base (C07). |

Comentario positivo: la suite de caracterización por la API, sin depender de clases internas, es lo que permite afirmar que el comportamiento se conserva; se mantiene como suite de regresión.

En el mismo commit de correcciones (`55cecd4`), Checkstyle pasó de modo informe a **bloqueante**.

## Resultado

Con CR-01, CR-02 y CR-06 resueltos y CR-03 a CR-07 documentados, se cumplen los 13 criterios. El check `calidad` quedó en verde: 0 violaciones de Checkstyle, 45/45 pruebas y 94,9 % de cobertura de líneas. El PR #1 se integró a `main` con un *merge commit*, sin *squash*.

| Métrica | `estado-inicial` | `estadorefactorizado` |
|---|---|---|
| Violaciones de Checkstyle | 51 | 0 (1 suprimida, CR-06) |
| Complejidad ciclomática máxima | 27 | 4 |
| Líneas de `OrderProcessor` | 223 | 81 |
| Pruebas | 24 | 45 |
| Cobertura de líneas | 92,6 % (224/242) | 94,9 % (279/294) |
| Cobertura de ramas | 73,9 % (65/88) | 82,8 % (48/58) |
