# Guía de Configuración de Groq

Esta guía detalla cómo configurar y utilizar la integración con Groq en AICha.

## Tabla de Contenidos

1. [Configuración de la API Key](#configuración-de-la-api-key)
2. [Arquitectura de Configuración](#arquitectura-de-configuración)
3. [Detección Automática de Modelos](#detección-automática-de-modelos)
4. [Configuración Manual](#configuración-manual)
5. [Solución de Problemas](#solución-de-problemas)

---

## Configuración de la API Key

### Android

Crea un archivo `local.properties` en la raíz del proyecto:

```properties
GROQ_API_KEY=tu_api_key_aquí
```

### iOS

Crea un archivo `Config.xcconfig` dentro de `iosApp/`:

```
GROQ_API_KEY = tu_api_key_aquí
```

### Obtener una API Key

1. Regístrate en [Groq Cloud](https://console.groq.com/)
2. Ve a la sección **API Keys**
3. Genera una nueva clave

> **Importante:** Nunca subas la API Key al repositorio. Los archivos `local.properties` y `Config.xcconfig` están en `.gitignore`.

---

## Arquitectura de Configuración

La configuración de Groq utiliza una arquitectura de capas:

```text
domain/config/GroqConfig.kt          # Interfaz
domain/model/GroqModel.kt            # Modelo de dominio
domain/model/GroqPreferences.kt      # Preferencias
data/remote/config/GroqConfigImpl.kt  # Implementación
data/remote/config/GroqModelResolver.kt # Resolución de modelos
di/GroqConfigModule.kt               # Inyección de dependencias
```

### Componentes Principales

| Componente | Responsabilidad |
|------------|-----------------|
| `GroqConfig` | Interfaz de configuración centralizada |
| `GroqModel` | Representa un modelo disponible en Groq |
| `GroqPreferences` | Configuración manual de preferencias |
| `GroqConfigImpl` | Implementación con detección automática |
| `GroqModelResolver` | Lógica de selección de modelos |

---

## Detección Automática de Modelos

La app detecta automáticamente los modelos disponibles en Groq:

### Flujo de Resolución

1. Al enviar un mensaje, se llama a `groqConfig.resolveChatModel()`
2. Se consulta la API de Groq para obtener modelos disponibles
3. Se filtran modelos no-chat (whisper, prompt-guard, tts, dall-e)
4. Se selecciona el mejor modelo basado en:
   - Preferencia manual (si está configurada)
   - Preferencia de proveedor (qwen > llama > mixtral > gemma)
   - Tamaño de contexto
5. Si no hay modelos disponibles, se usa el modelo por defecto

### Modelos Excluidos

Los siguientes tipos de modelos son excluidos automáticamente:

- **whisper**: Modelos de audio/transcripción
- **prompt-guard**: Modelos de seguridad
- **tts**: Modelos de texto-a-voz
- **dall-e**: Modelos de generación de imágenes

### Modelo por Defecto

Si no se puede resolver un modelo, se usa `qwen/qwen3.6-27b` como fallback.

---

## Configuración Manual

Es posible configurar manualmente el modelo preferido mediante `GroqPreferences`:

```kotlin
val preferences = GroqPreferences(
    preferredModelId = "llama3-70b-8192",  // Modelo preferido
    autoDetectModels = true,                // Habilitar detección automática
    cacheModels = true                      // Habilitar caché
)
```

### Propiedades de GroqPreferences

| Propiedad | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `preferredModelId` | `String?` | `null` | ID del modelo preferido (override manual) |
| `autoDetectModels` | `Boolean` | `true` | Habilitar detección automática de modelos |
| `cacheModels` | `Boolean` | `true` | Habilitar caché de modelos en memoria |

### Inyección de Preferencias

Para inyectar preferencias personalizadas, modifica el módulo de Koin:

```kotlin
val groqConfigModule = module {
    singleOf(::GroqModelResolver)
    single { 
        GroqPreferences(
            preferredModelId = "tu_modelo_preferido",
            autoDetectModels = true,
            cacheModels = true
        )
    }
    singleOf(::GroqConfigImpl) bind GroqConfig::class
}
```

---

## Solución de Problemas

### Error: "The model does not exist or you do not have access to it"

**Causa:** El modelo configurado no está disponible en tu cuenta de Groq.

**Solución:**
1. Verifica que tu API Key sea válida
2. Consulta los modelos disponibles en [Groq Cloud](https://console.groq.com/)
3. La app debería detectar automáticamente un modelo disponible

### Error: "No connected devices"

**Causa:** No hay un emulador o dispositivo Android conectado.

**Solución:**
1. Inicia un emulador Android
2. O conecta un dispositivo físico via USB

### La app no responde después de enviar un mensaje

**Causa:** Problema de conexión con la API de Groq.

**Solución:**
1. Verifica tu conexión a internet
2. Verifica que la API Key sea válida
3. Revisa los logs para errores específicos

### Modelos no se detectan automáticamente

**Causa:** La detección automática está deshabilitada o hay un error de red.

**Solución:**
1. Verifica que `autoDetectModels` esté en `true`
2. Verifica tu conexión a internet
3. La app usará el modelo por defecto si la detección falla

---

## Referencias

- [Documentación de Groq](https://console.groq.com/docs)
- [openai-kotlin SDK](https://github.com/Aallam/openai-kotlin)
- [Arquitectura del Proyecto](ARCHITECTURE.md)
- [Especificación de la Feature](SPEC.md)
