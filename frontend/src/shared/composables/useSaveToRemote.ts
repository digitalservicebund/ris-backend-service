import * as Sentry from "@sentry/vue"
import { computed, onUnmounted, ref } from "vue"
import { ServiceResponse, ResponseError } from "@/services/httpClient"

function getCurrentTime(dateSaved: Date) {
  const fullHour = ("0" + dateSaved.getHours()).slice(-2)
  const fullMinute = ("0" + dateSaved.getMinutes()).slice(-2)
  return `${fullHour}:${fullMinute}`
}
export function useSaveToRemote(
  saveCallback: () => Promise<ServiceResponse<void>>,
  autoSaveInterval = 0,
) {
  const saveIsInProgress = ref(false)
  const lastSaveError = ref<ResponseError | undefined>(undefined)
  const lastSavedOn = ref<Date | undefined>(undefined)
  const formattedLastSavedOn = computed(
    () => lastSavedOn.value && getCurrentTime(lastSavedOn.value),
  )

  async function triggerSave(): Promise<void> {
    if (saveIsInProgress.value) {
      return
    }

    saveIsInProgress.value = true
    lastSaveError.value = undefined

    try {
      const response = await saveCallback()
      lastSaveError.value = response.error

      if (lastSaveError.value == undefined) {
        lastSavedOn.value = new Date()
      } else {
        Sentry.captureException(lastSaveError.value, {
          tags: {
            type: "save_failed",
          },
        })
      }
    } catch (error) {
      lastSaveError.value = { title: "Verbindung fehlgeschlagen" }
    } finally {
      saveIsInProgress.value = false
    }
  }
  const timer = setInterval(triggerSave, autoSaveInterval)

  onUnmounted(() => {
    clearInterval(timer)
  })

  return {
    saveIsInProgress,
    triggerSave,
    lastSaveError,
    formattedLastSavedOn,
  }
}
