<script lang="ts" setup>
import { onUnmounted } from "vue"
import { ServiceResponse } from "@/services/httpClient"
import TextButton from "@/shared/components/input/TextButton.vue"
import { useSaveToRemote } from "@/shared/composables/useSaveToRemote"

const props = defineProps<{
  ariaLabel: string
  serviceCallback: () => Promise<ServiceResponse<void>>
}>()

const getCurrentTime = (dateSaved: Date) => {
  const fullHour = ("0" + dateSaved.getHours()).slice(-2)
  const fullMinute = ("0" + dateSaved.getMinutes()).slice(-2)
  return `${fullHour}:${fullMinute}`
}

const { triggerSave, lastSaveError, lastSavedOn, timer } = useSaveToRemote(
  props.serviceCallback,
  10000
)

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<template>
  <div class="flex flex-col space-y-[5px]">
    <TextButton
      :aria-label="ariaLabel"
      label="Speichern"
      @click="triggerSave"
    />
    <div class="justify-start">
      <div v-if="lastSaveError !== undefined">
        <p class="label-03-reg text-red-800">
          Fehler beim Speichern{{
            lastSaveError.title.includes("Berechtigung")
              ? ": " + lastSaveError.title
              : ""
          }}
        </p>
      </div>

      <div v-if="lastSavedOn !== undefined && lastSaveError === undefined">
        <p class="label-03-reg">
          Zuletzt
          <span>{{ getCurrentTime(lastSavedOn) }}</span>
          Uhr
        </p>
      </div>
    </div>
  </div>
</template>
