# SPEC.md — Feature: Chat con IA

Este documento especifica el comportamiento y los detalles específicos de la **feature de Chat** de la app AICha, compartida entre **Android e iOS** vía Kotlin Multiplatform. Para la arquitectura transversal del proyecto (capas Clean Architecture, módulos, convención Route/Screen, DI, testing, CI/CD), ver [`docs/ARCHITECTURE.md`](./ARCHITECTURE.md); este documento no repite esas reglas, solo las particulariza para esta feature.

---

## 1. Requerimientos de la feature

### 1.1. Pantalla principal (Chat)

- Interfaz para iniciar y mantener conversaciones con la IA, implementada como Composables de **Compose Multiplatform** en `composeApp/src/commonMain/kotlin/.../ui/chat/`, compartidos sin cambios entre Android e iOS.
- Flujo síncrono visual: por cada mensaje enviado por el usuario, la IA debe devolver una respuesta en pantalla.
- **Validación (Android):** este flujo tiene un Journey (Android CLI) que lo cubre — `composeApp/src/androidInstrumentedTest/journey/send_message.xml` — que verifica la pantalla de chat, el envío de un mensaje y la aparición de la respuesta/loading. Ver `ARCHITECTURE.md` §12.2 para el detalle de la herramienta.
- **Validación (iOS):** no existe hoy una herramienta equivalente a Journeys; este flujo se valida manualmente en simulador/dispositivo antes de cada release hasta que se resuelva el punto abierto en `ARCHITECTURE.md` §16.

### 1.2. Contexto de la conversación

- La IA debe recibir siempre como contexto histórico toda la conversación previa, para que la nueva respuesta sea coherente con el hilo actual.
- Este historial se arma en la capa de repositorio (`data/repository/ChatRepositoryImpl`, `commonMain`), invocado a través de un use case (`domain/usecase/SendMessageUseCase`, ver `ARCHITECTURE.md` §6); nunca se construye en la UI ni directamente en el ViewModel.

### 1.3. Historial de conversaciones

- Menú lateral (Navigation Drawer, Compose Multiplatform) desplegable, compartido entre ambas plataformas.
- Permite visualizar y seleccionar conversaciones previas almacenadas localmente (Room Multiplatform).
- En la parte superior del menú lateral, botón de acción rápida para "Iniciar nueva conversación".
- Nota de plataforma: el gesto/afordancia para abrir el drawer (swipe desde el borde, botón de menú) puede requerir un pequeño ajuste `expect`/`actual` a nivel de Composable puntual si el patrón de navegación nativo difiere entre Android e iOS (ver `ARCHITECTURE.md` §4); la lógica de estado y contenido del drawer permanece 100% común.

---

## 2. Proveedor de IA (específico de esta feature)

- **Motor de inferencia:** Groq.
- **SDK cliente:** `openai-kotlin` (Aallam) — `https://github.com/Aallam/openai-kotlin`. Se usa porque Groq expone un endpoint 100% compatible con el formato de peticiones de OpenAI; **no se está usando la API de OpenAI**, solo su formato de request/response.
- El SDK está construido sobre Ktor, por lo que es multiplatform sin cambios de código funcional entre Android e iOS; solo cambia el `HttpClientEngine` resuelto vía `expect`/`actual` (`OkHttp` en Android, `Darwin` en iOS — ver `ARCHITECTURE.md` §4).
- El cliente se configura apuntando al `baseUrl` de Groq (no al de OpenAI) y con la API Key de Groq, resuelta también vía `expect`/`actual` (`ARCHITECTURE.md` §4 y §15) según la plataforma.

---

## 3. Pantallas y capas involucradas

Siguiendo la convención Route/Screen y las capas de `ARCHITECTURE.md` §6-§7:

| Componente | Paquete | Responsabilidad |
|---|---|---|
| `ChatScreen`/`ChatRoute`/`ChatViewModel` | `ui/chat/` (`commonMain`) | Envío de mensajes, renderizado de la conversación activa |
| `HistoryScreen`/`HistoryRoute`/`HistoryViewModel` | `ui/history/` (`commonMain`) | Listado de conversaciones previas + botón "Nueva conversación" |
| `SendMessageUseCase`, `ObserveConversationHistoryUseCase`, `CreateConversationUseCase` | `domain/usecase/` (`commonMain`) | Puerta de entrada obligatoria desde los ViewModels hacia `domain/repository/` |
| `ChatRepository` (interfaz) | `domain/repository/` (`commonMain`) | Contrato que expone el historial y el envío de mensajes, sin conocer Room ni el SDK de Groq |
| `ChatRepositoryImpl` | `data/repository/` (`commonMain`) | Arma el contexto histórico, decide cuándo leer de Room vs. pedir respuesta a Groq |

> Historial de decisión ya tomada: el drawer de historial vive como feature independiente (`ui/history/`), con su propio `Route`/`ViewModel`/`UiState`, dado que tiene lógica propia (listar, seleccionar, crear conversación) más allá de ser un simple componente visual de `ui/chat/`.

---

## 4. Persistencia (específico de esta feature)

- **Room Multiplatform** (2.7+) almacena las conversaciones y sus mensajes (historial); entidades y DAOs viven en `data/local/` (`commonMain`).
- **Mandatorio usar KSP** (no KAPT, que no es multiplatform) para los procesadores de Room, tal como se define en `ARCHITECTURE.md` §10.
- La instancia de la base de datos se construye vía `expect`/`actual`: en Android recibe un `Context` (inyectado por Koin), en iOS resuelve el path al directorio de documentos de la sandbox de la app (ver `ARCHITECTURE.md` §4).
- Modelo mínimo esperado (a confirmar con el equipo antes de implementar el esquema definitivo): conversación (id, título/resumen, fecha — `kotlinx-datetime`, no `java.time`) y mensaje (id, conversación asociada, rol emisor, contenido, timestamp).

---

## 5. Reglas de trabajo para esta feature

- **Verificación de compilación:** siempre que se genere código para esta feature, compilar inmediatamente en ambos targets antes de continuar:
  ```bash
  ./gradlew :composeApp:compileDebugKotlinAndroid
  ./gradlew :composeApp:compileKotlinIosSimulatorArm64
  ```
- **Respeto a Gradle:** aunque los `build.gradle.kts`/`libs.versions.toml` actuales parezcan incorrectos, se consideran válidos tal cual están. Si hay que modificarlos, solo agregar lo nuevo que se necesite, sin tocar lo ya existente.
- **Código común primero:** toda lógica de esta feature (use cases, repositorio, mappers, ViewModel, Composables) va en `commonMain` salvo que dependa genuinamente de una API nativa; en ese caso, aislar el punto exacto vía `expect`/`actual` (ver `ARCHITECTURE.md` §4), nunca duplicar la pantalla o el ViewModel completo por plataforma.
- **Tests en `commonTest`:** `SendMessageUseCaseTest`, `ChatRepositoryImplTest` y `ChatViewModelTest` viven en `commonTest` con fakes (no mocks JVM-only), para correr igual en la validación de ambas plataformas.
