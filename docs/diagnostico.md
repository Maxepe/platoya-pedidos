# Diagnóstico de deuda técnica y code smells

Versión analizada: tag [`estado-inicial`](https://github.com/Maxepe/platoya-pedidos/tree/estado-inicial). Los enlaces apuntan a las líneas de ese tag.

- `OP` = [`OrderProcessor.java`](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java) (223 líneas)
- `OC` = [`OrderController.java`](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/controller/OrderController.java)

## Método

1. Lectura del código base y registro de cada problema con archivo y línea.
2. Contraste con el catálogo de smells de Fowler y con los principios SOLID.
3. Análisis estático con Checkstyle (`config/checkstyle.xml`), en modo informe.
4. Reproducción por la API de los problemas con impacto externo.
5. Priorización según impacto en el usuario × frecuencia de cambio de la zona afectada.

## Defectos externos reproducidos

| Defecto | Reproducción | Resultado en la base | Caso de prueba |
|---|---|---|---|
| Pérdida de stock en pedidos rechazados | `POST /api/orders` con 1 hamburguesa y 99 pizzas | `400 Stock insuficiente para Pizza muzzarella`, pero la hamburguesa pasa de 20 a 19 | C14 |
| Pedido inexistente responde `200` vacío | `GET /api/orders/99` | `200 OK` sin cuerpo | C22 |

## Tabla de diagnóstico

| # | Smell / debilidad | Ubicación | Evidencia | Prioridad |
|---|---|---|---|---|
| S1 | Inconsistencia transaccional del stock (defecto, no smell) | [OP L59-84](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L59-L84) | El stock se descuenta y se guarda (L74-75) dentro del bucle, antes de validar los ítems siguientes, sin `@Transactional`. | Crítica |
| S2 | God Object / Large Class (SRP) | [OP](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java) | Valida, reserva stock, calcula precios, persiste, notifica y arma el reporte. | Alta |
| S3 | Long Method | [OP L30-125](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L30-L125) | `processOrder()` de 96 líneas, con cinco bloques comentados. | Alta |
| S4 | Manejo de errores deficiente | OP (14 `RuntimeException`), [OC L31, L50, L59](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/controller/OrderController.java#L31), [OP L128](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L128) | Todo error termina en `400`; un pedido inexistente responde `200` vacío. | Alta |
| S5 | Acoplamiento a implementaciones concretas (DIP) | [OP L24-28](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L24-L28), L117, L121, L165, L187 | `new EmailSender()` y `new WhatsAppService()` en el negocio; `@Autowired` en campos. | Alta |
| S6 | Condicional por tipo (OCP) | [OP L87-105](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L87-L105) | `if ("VIP") … else if ("EMPLEADO")` y otra vez `"VIP"` para el envío. | Media |
| S7 | Long Parameter List + Data Clumps | [OP L30-32](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L30-L32), L47-54; [OC L28-29](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/controller/OrderController.java#L28-L29) | 8 parámetros, 7 de tipo `String`, que viajan siempre juntos. | Media |
| S8 | Duplicated Code | [OP L146-153](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L146-L153) y [L172-179](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L172-L179); L89 y L99 | Búsqueda y validación del pedido copiada; condición `"VIP"` repetida; armado de avisos en tres lugares. | Media |
| S9 | Primitive Obsession | OP L112, L154, L162, L180, L183, L200-206; [Map L210-221](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L210-L221) | Estado como `String` escrito a mano en 8 lugares; reporte como `Map<String, Object>`. | Media |
| S10 | Feature Envy / modelo anémico | [OP L154-162](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L154-L162), L180-183; [`Order.java`](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/model/Order.java) | Las transiciones de estado las decide `OrderProcessor`; `Order` tiene 16 setters públicos. | Media |
| S11 | Magic Numbers | OP L90, L92, L97, L101, L102; `100.0` en L105, L215 y L217 | `0.10`, `0.20`, `0.21`, `20000`, `1500` sin nombre. | Baja |
| S12 | Nombres poco expresivos | [OP L193-222](https://github.com/Maxepe/platoya-pedidos/blob/estado-inicial/src/main/java/com/platoya/pedidos/service/OrderProcessor.java#L193-L222) y todo OP | `o`, `oi`, `op`, `oo`, `d`, `tmp`, `x`, `r`, `c1`/`c2`/`c3`, `fact`, `lista`, `prodRepo`. | Baja |

## Confirmación con Checkstyle

`mvn checkstyle:check` sobre `etapa-diagnostico` (mismo `src/main` que `estado-inicial`): **51 violaciones**.

| Regla | Violaciones | Smell |
|---|---|---|
| `LocalVariableName` (mínimo 3 caracteres) | 21 | S12 |
| `MagicNumber` | 11 | S11 |
| `MethodLength` (máximo 15) | 4 | S3 |
| `CyclomaticComplexity` (máximo 5) | 3 (`processOrder`: 27) | S2, S3, S6 |
| `RegexpSinglelineJava` (`@Autowired` en campos) | 4 | S5 |
| `RegexpSinglelineJava` (`new EmailSender` / `new WhatsAppService`) | 4 | S5 |
| `IllegalCatch` | 3 | S4 |
| `ParameterNumber` (máximo 4) | 1 | S7 |

S1, S8, S9 y S10 son problemas de diseño que una regla sintáctica no detecta.

## Priorización

S1 primero, porque corrompe datos. Después S2 a S5, que bloquean la capacidad de prueba. S6 a S10 se resuelven con los refactorings R1 a R6, y S11 y S12 salen al extraer métodos y clases con nombres expresivos.
