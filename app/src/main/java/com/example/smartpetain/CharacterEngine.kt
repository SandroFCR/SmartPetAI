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
        equippedPetName: String,
        studyMinutes: Int,
        minutesSinceBreak: Int,
        pendingTasks: Int,
        completedTasks: Int,
        petLevel: Int = 1
    ): Character {
        val state = analyzeState(minutesSinceBreak, pendingTasks, completedTasks)
        
        // Prioridad 1: Alerta de Saturación (Pompompurin siempre avisa)
        if (state == UserState.SATURATED) return pompompurinAlert(minutesSinceBreak)
        
        // Prioridad 2: Alerta de Desorganización (Hello Kitty siempre avisa)
        if (state == UserState.DISORGANIZED) return helloKittyAlert(pendingTasks)

        // Prioridad 3: Mascota equipada en estado normal (FOCUSED)
        return when (equippedPetName) {
            "Pompompurin" -> pompompurinFocused(studyMinutes, petLevel)
            "Hello Kitty" -> helloKittyFocused(studyMinutes, petLevel)
            else -> cinnamorollFocused(studyMinutes, petLevel)
        }
    }

    private fun analyzeState(
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

    private fun cinnamorollFocused(studyMinutes: Int, level: Int): Character {
        val message = when {
            studyMinutes >= 60 -> "¡Increíble nivel $level! Llevas más de una hora enfocada. 🌟"
            studyMinutes >= 25 -> "¡Buen trabajo! Completaste un Pomodoro. ¡Sigue así! 💙"
            else -> "¡Tú puedes! Concéntrate en tu meta. (Nivel $level) ✨"
        }
        return Character(
            name = "Cinnamoroll",
            emoji = "🐶",
            role = "Motivación y enfoque",
            message = message,
            color = "purple"
        )
    }

    private fun pompompurinFocused(studyMinutes: Int, level: Int): Character {
        val message = when {
            studyMinutes >= 45 -> "¡Nivel $level alcanzado! Estás haciendo un gran esfuerzo. 🍮"
            else -> "Me encanta verte estudiar con calma. (Nivel $level) ✨"
        }
        return Character(
            name = "Pompompurin",
            emoji = "🐶",
            role = "Descanso y equilibrio",
            message = message,
            color = "amber"
        )
    }

    private fun helloKittyFocused(studyMinutes: Int, level: Int): Character {
        val message = when {
            studyMinutes >= 30 -> "¡Nivel $level! Todo está bajo control y bien organizado. 🎀"
            else -> "¡Qué ordenado está todo! Sigamos así. (Nivel $level) 💕"
        }
        return Character(
            name = "Hello Kitty",
            emoji = "🎀",
            role = "Organización",
            message = message,
            color = "pink"
        )
    }

    private fun pompompurinAlert(minutesSinceBreak: Int): Character {
        val message = when {
            minutesSinceBreak >= 90 -> "¡Oye! Llevas ${minutesSinceBreak} min sin descansar. ¡Para ya! 😴"
            else -> "Tu cerebro necesita un respiro. ¡Toma 10 minutos! ☕"
        }
        return Character(
            name = "Pompompurin",
            emoji = "🐶",
            role = "Descanso y equilibrio",
            message = message,
            color = "amber"
        )
    }

    private fun helloKittyAlert(pendingTasks: Int): Character {
        val message = when {
            pendingTasks >= 5 -> "¡Tienes $pendingTasks tareas pendientes! Organicémonos 📋"
            else -> "Tienes $pendingTasks tareas por hacer. ¡Empecemos! 🎀"
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
