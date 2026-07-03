# 🐾 SmartPetAI

**App de productividad y hábitos de estudio para Android, con estética kawaii, gamificación e IA generativa para coaching personalizado.**

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)
![OpenAI](https://img.shields.io/badge/AI-OpenAI_API-412991?logo=openai&logoColor=white)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)

---

## 📌 Descripción

**SmartPetAI** es una aplicación Android de **productividad y gestión de estudio** con una interfaz kawaii, diseñada para ayudar a estudiantes a mejorar sus hábitos académicos sin caer en el burnout. La app combina **gestión del tiempo**, **organización de tareas** y **motivación gamificada** a través de personajes acompañantes que reaccionan al estado del usuario en tiempo real.

### Funcionalidades principales

- ⏱️ **Temporizador Pomodoro** para sesiones de estudio enfocadas.
- ✅ **Gestión de tareas y materias**, con prioridades y seguimiento de pendientes.
- 🎭 **Personajes acompañantes reactivos** (inspirados en Cinnamoroll, Pompompurin, Hello Kitty) que cambian de estado según el comportamiento del usuario, para prevenir el burnout y reforzar el hábito de estudio.
- 📊 **Estadísticas semanales de productividad**, tareas completadas y metas cumplidas.
- 🔔 **Notificaciones locales** con recordatorios de estudio.

## 🤖 Componente de Inteligencia Artificial

SmartPetAI integra IA en dos niveles:

1. **CharacterEngine (motor de decisiones local)** — Sistema de reglas en tiempo real que analiza variables del usuario (minutos estudiados sin descanso, tareas pendientes, nivel de foco) para determinar el estado y mensaje del personaje activo.
2. **AI Study Coach (OpenAI API + Firebase Cloud Functions)** — Una Cloud Function conectada a la **API de OpenAI (GPT)** analiza las estadísticas semanales del usuario (productividad, metas, tareas) y genera **recomendaciones personalizadas** escritas con la personalidad de las mascotas.

## 🛠️ Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose (Material 3, UI declarativa) |
| Arquitectura | MVVM |
| Autenticación | Firebase Authentication |
| Base de datos | Firebase Firestore (NoSQL) |
| Backend serverless | Firebase Cloud Functions (Node.js) |
| Almacenamiento | Firebase Cloud Storage |
| IA generativa | OpenAI API (GPT) |
| Notificaciones | Android Notifications API |

## 🏗️ Arquitectura

```
Usuario (App Android - Jetpack Compose)
        │
        ├── CharacterEngine (lógica local, tiempo real)
        │
        ├── Firebase Authentication (login/registro)
        ├── Firebase Firestore (perfil, tareas, sesiones, stats)
        ├── Firebase Cloud Storage (imágenes de perfil)
        │
        └── Firebase Cloud Function (Node.js)
                    │
                    └── OpenAI API → genera recomendaciones semanales
```

## 🚀 Instalación

```bash
# Clonar el repositorio
git clone https://github.com/SandroFCR/SmartPetAI.git
cd SmartPetAI
```

1. Abrir el proyecto en **Android Studio**.
2. Conectar tu propio proyecto de **Firebase** (Authentication, Firestore, Storage, Cloud Functions).
3. Configurar tu **API Key de OpenAI** en la Cloud Function correspondiente.
4. Ejecutar sobre un emulador o dispositivo Android.

## 🔮 Mejoras futuras

- [ ] Modo offline con sincronización diferida
- [ ] Más personajes y skins desbloqueables
- [ ] Widget de escritorio para el temporizador Pomodoro
- [ ] Tests unitarios y de UI (JUnit / Compose Testing)

## 👤 Autor

**Sandro** — [GitHub](https://github.com/SandroFCR)
