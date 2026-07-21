# SPEC.md — Feature: Chat con IA

Este documento especifica el comportamiento y los detalles específicos de la **feature de Chat** de la app AICha. Para la arquitectura transversal del proyecto (capas, convención Route/Screen, DI, testing, CI/CD), ver [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md); este documento no repite esas reglas, solo las particulariza para esta feature.

---

## 1. Requerimientos de la feature

### 1.1. Pantalla principal (Chat)

- Interfaz para iniciar y mantener conversaciones con la IA.
- Flujo síncrono visual: por cada mensaje enviado por el usuario, la IA debe devolver una respuesta en pantalla.
- **Validación:** este flujo tiene un Journey (Android CLI) que lo cubre — `app/src/androidTest/jurney/send_message.xml` — que verifica la pantalla de chat, el envío de un mensaje y la aparición de la respuesta/loading. Ver `ARCHITECTURE.md` §10.2 para el detalle de la herramienta.

### 1.2. Contexto de la conversación

- La IA debe recibir siempre como contexto histórico toda la conversación previa, para que la nueva respuesta sea coherente con el hilo actual.
- Este historial se arma en la capa de repositorio (`data/repository/`, ver `ARCHITECTURE.md` §8), nunca se construye en la UI ni en el ViewModel.

### 1.3. Historial de conversaciones

- Menú lateral (Navigation Drawer) desplegable.
- Permite visualizar y seleccionar conversaciones previas almacenadas localmente.
- En la parte superior del menú lateral, botón de acción rápida para "Iniciar nueva conversación".

---

## 2. Proveedor de IA (específico de esta feature)

- **Motor de inferencia:** Groq.
- **SDK cliente:** `openai-kotlin` (Aallam) — `https://github.com/Aallam/openai-kotlin`. Se usa porque Groq expone un endpoint 100% compatible con el formato de peticiones de OpenAI; **no se está usando la API de OpenAI**, solo su formato de request/response.
- El cliente se configura apuntando al `baseUrl` de Groq (no al de OpenAI) y con la API Key de Groq.

---

## 3. Pantallas involucradas

Siguiendo la convención Route/Screen de `ARCHITECTURE.md` §4:

| Pantalla/componente | Paquete | Responsabilidad |
|---|---|---|
| Chat | `ui/chat/` | Envío de mensajes, renderizado de la conversación activa |
| Historial (Drawer) | `ui/chat/components/` o `ui/history/` (a definir según si el drawer es propio de la feature de chat o una feature independiente) | Listado de conversaciones previas + botón "Nueva conversación" |

> Pendiente de decisión: si el Navigation Drawer del historial se modela como un componente dentro de `ui/chat/` o como su propia feature (`ui/history/`) con su propio `Route`/`ViewModel`. Recomendado si el historial gana lógica propia (búsqueda, borrado, renombrado): feature independiente.

---

## 4. Persistencia (específico de esta feature)

- **Room** almacena las conversaciones y sus mensajes (historial).
- **Mandatorio usar KSP** (no KAPT) para los procesadores de Room, tal como se define en `ARCHITECTURE.md` §7.
- Modelo mínimo esperado (a confirmar con el equipo antes de implementar el esquema definitivo): conversación (id, título/resumen, fecha) y mensaje (id, conversación asociada, rol emisor, contenido, timestamp).

---

## 5. Reglas de trabajo para esta feature

- **Verificación de compilación:** siempre que se genere código para esta feature, compilar inmediatamente con `./gradlew compileDebugKotlin` para verificar que no haya errores de sintaxis o dependencias rotas antes de continuar.
- **Respeto a Gradle:** aunque los `build.gradle.kts`/`libs.versions.toml` actuales parezcan incorrectos, se consideran válidos tal cual están. Si hay que modificarlos, solo agregar lo nuevo que se necesite, sin tocar lo ya existente.