package com.example.smartpetain

// Estados posibles del usuario
enum class UserState {
    FOCUSED,      // Estudiando bien
    SATURATED,    // Cansado / mucho tiempo sin pausa
    DISORGANIZED  // Tareas vencidas o sin planificar
}

// Personaje activo
data class Character(
    val name: String,
    val emoji: String,
    val role: String,
    val message: String,
    val color: String // "purple", "amber", "pink"
)

// Motor de decisión de personajes
object CharacterEngine {

    fun getCharacter(
        studyMinutes: Int,
        minutesSinceBreak: Int,
        pendingTasks: Int,
        completedTasks: Int
    ): Character {
        val state = analyzeState(studyMinutes, minutesSinceBreak, pendingTasks, completedTasks)
        return when (state) {
            UserState.SATURATED -> pompompurin(minutesSinceBreak)
            UserState.DISORGANIZED -> helloKitty(pendingTasks)
            UserState.FOCUSED -> cinnamoroll(studyMinutes)
        }
    }

    private fun analyzeState(
        studyMinutes: Int,
        minutesSinceBreak: Int,
        pendingTasks: Int,
        completedTasks: Int
    ): UserState {
        // Saturado: más de 50 min sin pausa
        if (minutesSinceBreak >= 50) return UserState.SATURATED

        // Desorganizado: más de 3 tareas pendientes y pocas completadas
        if (pendingTasks >= 3 && completedTasks == 0) return UserState.DISORGANIZED

        // Enfocado por defecto
        return UserState.FOCUSED
    }

    private fun cinnamoroll(studyMinutes: Int): Character {
        val message = when {
            studyMinutes >= 60 -> "¡Increíble! Llevas más de una hora. ¡Eres una máquina! 🌟"
            studyMinutes >= 25 -> "¡Vas muy bien! Completaste un Pomodoro. ¡Sigue así! 💙"
            studyMinutes >= 10 -> "¡Buen ritmo! Cada minuto cuenta 🌱"
            else -> "¡Tú puedes! Concéntrate en tu meta de hoy ✨"
        }
        return Character(
            name = "Cinnamoroll",
            emoji = "🐶",
            role = "Motivación y enfoque",
            message = message,
            color = "purple"
        )
    }

    private fun pompompurin(minutesSinceBreak: Int): Character {
        val message = when {
            minutesSinceBreak >= 90 -> "¡Oye! Llevas ${minutesSinceBreak} min sin descansar. ¡Para ya! 😴"
            minutesSinceBreak >= 70 -> "Tu cerebro necesita un respiro. ¡Toma 10 minutos! ☕"
            else -> "Parece que has estudiado mucho. ¡Hora de una pausa! 🍮"
        }
        return Character(
            name = "Pompompurin",
            emoji = "🐶",
            role = "Descanso y equilibrio",
            message = message,
            color = "amber"
        )
    }

    private fun helloKitty(pendingTasks: Int): Character {
        val message = when {
            pendingTasks >= 5 -> "¡Tienes $pendingTasks tareas pendientes! Organicémonos 📋"
            pendingTasks >= 3 -> "Tienes $pendingTasks tareas por hacer. ¡Empecemos por la más importante! 🎀"
            else -> "Recuerda planificar tu día. ¡Yo te ayudo! 💕"
        }
        return Character(
            name = "Hello Kitty",
            emoji = "🎀",
            role = "Organización",
            message = message,
            color = "pink"
        )
    }
}