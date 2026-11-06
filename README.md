# PlatoYa · módulo de pedidos

API REST para recibir y procesar pedidos con envío a domicilio de un local de comidas. Es el sistema base del Trabajo Práctico Integrador de **Metodología de Sistemas II** (TUP, UTN San Nicolás): se diagnosticó su deuda técnica, se refactorizó de forma segura y se revisó con un Pull Request, un hook de pre-commit y CI.

## Funcionalidades

| Operación | Endpoint |
|---|---|
| Registrar y procesar un pedido | `POST /api/orders` |
| Consultar un pedido | `GET /api/orders/{id}` |
| Listar pedidos, con filtro opcional por estado | `GET /api/orders?status=CONFIRMADO` |
| Cancelar un pedido (repone el stock) | `POST /api/orders/{id}/cancel` |
| Marcar un pedido como entregado | `POST /api/orders/{id}/deliver` |
| Reporte de ventas | `GET /api/orders/report` |
| Catálogo con stock | `GET /api/products` |

Reglas de negocio: descuento por tipo de cliente (`VIP` 10 %, `EMPLEADO` 20 %), IVA del 21 %, envío de $1.500 si la base imponible es menor a $20.000 (gratis para `VIP`), validación de stock y avisos por email y WhatsApp simulados por consola.

## Tecnologías

Java 21 · Spring Boot 3.5 (Web, Data JPA) · H2 en memoria · Maven · JUnit 5, Mockito y MockMvc · JaCoCo · Checkstyle · GitHub Actions.

## Instalación y uso

Requisitos: JDK 21 y Maven 3.9.

```sh
mvn spring-boot:run
```

La API responde en `http://localhost:8080`. Al iniciar se cargan cinco productos de ejemplo (`src/main/resources/data.sql`). Los datos son sintéticos.

```sh
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{
  "customerName": "Ana Prueba", "customerEmail": "ana@example.com", "customerPhone": "+54 9 000 000-0001",
  "customerType": "VIP", "street": "Calle Falsa 123", "city": "San Nicolas", "zipCode": "2900",
  "items": [{"productId": 1, "quantity": 2}, {"productId": 3, "quantity": 1}]
}'
```

Respuesta: `200 OK` con el pedido confirmado (`"total": 34303.5`). Los avisos se imprimen en la consola con los prefijos `[EMAIL]` y `[WHATSAPP]`.

## Calidad

```sh
mvn verify                                # Checkstyle + 45 pruebas + cobertura mínima del 85 %
git config core.hooksPath .githooks       # activa el hook de pre-commit (una vez por clon)
```

| Control | Dónde |
|---|---|
| Linter (Checkstyle, reglas del checklist) | `config/checkstyle.xml` |
| Hook de pre-commit (linter → pruebas y cobertura) | `.githooks/pre-commit` |
| CI en cada push a `main` y `refactor/**`, y en cada PR | `.github/workflows/calidad.yml` |

Pruebas: 24 de caracterización por la API (`OrderApiCharacterizationTest`) y 21 unitarias (`OrderPricingServiceTest`, `CustomerPricingPolicyContractTest`, `OrderTest`, `OrderProcessorTest`).

## Estructura

```
controller/   OrderController, ProductController, OrderRequestDto, ApiExceptionHandler
service/      OrderProcessor, InventoryService, OrderReportService, OrderRequest, OrderLineRequest, SalesReport
pricing/      OrderPricingService, CustomerPricingPolicy + Regular/Vip/Employee, CustomerPricingPolicies, Amounts
notification/ OrderNotifier, NotificationDispatcher, EmailGateway, WhatsAppGateway, EmailSender, WhatsAppService
model/        Order, OrderItem, Product, OrderStatus, CustomerInfo, DeliveryAddress, PriceBreakdown
exception/    BusinessRuleException, InsufficientStockException, OrderNotFoundException
```

## Historial y documentación

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base con los problemas diagnosticados |
| `etapa-diagnostico` | Pruebas de caracterización, Checkstyle en modo informe y diagnóstico |
| `etapa-refactoring` | Último commit de la serie R1 a R6 (rama `refactor/order-processor`) |
| `etapa-pruebas` | Pruebas unitarias, después de las correcciones del code review |
| `etapa-docs` / `estadorefactorizado` | Documentación y estado final del código |

- [`docs/diagnostico.md`](docs/diagnostico.md): deuda técnica y code smells de la versión base.
- [`docs/decisiones-tecnicas.md`](docs/decisiones-tecnicas.md): diseño, diagrama de clases y decisiones (D1, D2).
- [`docs/code-review-checklist.md`](docs/code-review-checklist.md): checklist, observaciones del PR #1 y resultado.
- [`docs/evidencias/`](docs/evidencias/): salidas de Checkstyle, de las pruebas y reportes de cobertura.

Autor: Maximiliano Agustín Petrei.
