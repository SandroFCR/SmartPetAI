const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {defineSecret} = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

const openAiApiKey = defineSecret("OPENAI_API_KEY");

exports.generateStudyCoach = onCall(
  {
    secrets: [openAiApiKey],
    timeoutSeconds: 30,
  },
  async (request) => {
    const data = request.data || {};
    const dailyGoalMinutes = clampNumber(data.dailyGoalMinutes, 1, 300, 25);
    const completedTasks = clampNumber(data.completedTasks, 0, 999, 0);
    const pendingTasks = clampNumber(data.pendingTasks, 0, 999, 0);
    const pets = Array.isArray(data.pets) ? data.pets : [];
    const weekStats = normalizeWeekStats(data.weekStats);

    const weeklyGoal = dailyGoalMinutes * 7;
    const effectiveMinutes = weekStats.reduce((sum, day) => {
      return sum + Math.min(day.minutes, dailyGoalMinutes);
    }, 0);
    const totalMinutes = weekStats.reduce((sum, day) => sum + day.minutes, 0);
    const productivity = Math.min(
      100,
      Math.max(0, Math.round((effectiveMinutes / weeklyGoal) * 100)),
    );
    const today = weekStats[getCurrentWeekIndex()] || {minutes: 0};
    const remainingTodayMinutes = Math.max(0, dailyGoalMinutes - today.minutes);
    const remainingWeeklyMinutes = Math.max(0, weeklyGoal - effectiveMinutes);

    const promptData = {
      dailyGoalMinutes,
      weeklyGoal,
      effectiveMinutes,
      totalMinutes,
      productivity,
      remainingTodayMinutes,
      remainingWeeklyMinutes,
      completedTasks,
      pendingTasks,
      pets,
      weekStats,
      availableCharacters: ["Cinnamoroll", "Pompompurin", "Hello Kitty"],
    };

    try {
      const response = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${openAiApiKey.value()}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: process.env.OPENAI_MODEL || "gpt-5.2",
          instructions:
            "Eres un coach de estudio dentro de una app kawaii de mascotas. " +
            "Responde en espanol claro, breve y motivador. " +
            "No uses markdown. No inventes datos. " +
            "Recibiras los niveles de las mascotas (pets). Felicita al usuario por niveles altos " +
            "o motiva a subir de nivel a las mascotas con niveles bajos recomendando acciones. " +
            "Elige solo una mascota entre Cinnamoroll, Pompompurin y Hello Kitty.",
          input:
            "Genera una recomendacion de estudio con estos datos reales: " +
            JSON.stringify(promptData),
          text: {
            format: {
              type: "json_schema",
              name: "study_coach_recommendation",
              strict: true,
              schema: {
                type: "object",
                additionalProperties: false,
                properties: {
                  characterName: {
                    type: "string",
                    enum: ["Cinnamoroll", "Pompompurin", "Hello Kitty"],
                  },
                  title: {
                    type: "string",
                    maxLength: 42,
                  },
                  message: {
                    type: "string",
                    maxLength: 150,
                  },
                  reason: {
                    type: "string",
                    maxLength: 170,
                  },
                  weeklyProgress: {
                    type: "string",
                    maxLength: 80,
                  },
                },
                required: [
                  "characterName",
                  "title",
                  "message",
                  "reason",
                  "weeklyProgress",
                ],
              },
            },
          },
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        logger.error("OpenAI request failed", {
          status: response.status,
          body: errorText,
        });
        throw new HttpsError("internal", "No se pudo generar la recomendacion.");
      }

      const json = await response.json();
      const text = extractOutputText(json);
      const recommendation = JSON.parse(text);

      return {
        characterName: recommendation.characterName,
        title: recommendation.title,
        message: recommendation.message,
        reason: recommendation.reason,
        weeklyProgress: recommendation.weeklyProgress,
      };
    } catch (error) {
      logger.error("generateStudyCoach failed", error);
      if (error instanceof HttpsError) {
        throw error;
      }
      throw new HttpsError("internal", "No se pudo generar la recomendacion.");
    }
  },
);

function clampNumber(value, min, max, fallback) {
  const number = Number(value);
  if (!Number.isFinite(number)) {
    return fallback;
  }
  return Math.min(max, Math.max(min, Math.round(number)));
}

function normalizeWeekStats(value) {
  const labels = ["Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"];
  const source = Array.isArray(value) ? value : [];

  return labels.map((label, index) => {
    const item = source[index] || {};
    return {
      day: String(item.day || label),
      minutes: clampNumber(item.minutes, 0, 1440, 0),
      completedSessions: clampNumber(item.completedSessions, 0, 99, 0),
    };
  });
}

function getCurrentWeekIndex() {
  const day = new Date().getDay();
  if (day === 0) {
    return 6;
  }
  return day - 1;
}

function extractOutputText(response) {
  if (typeof response.output_text === "string") {
    return response.output_text;
  }

  const output = Array.isArray(response.output) ? response.output : [];
  for (const item of output) {
    const content = Array.isArray(item.content) ? item.content : [];
    for (const part of content) {
      if (typeof part.text === "string") {
        return part.text;
      }
    }
  }

  throw new Error("OpenAI response did not include output text.");
}
