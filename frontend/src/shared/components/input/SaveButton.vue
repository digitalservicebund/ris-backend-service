<script lang="ts" setup>
import { onUnmounted } from "vue"
import TextButton from "./TextButton.vue"
import { ServiceResponse } from "@/services/httpClient"
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

const { saveIsInProgress, triggerSave, lastSaveError, lastSavedOn, timer } =
  useSaveToRemote(props.serviceCallback, 10000)

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<template>
  <div class="flex flex-row items-end justify-start space-x-[10px]">
    <TextButton
      :aria-label="ariaLabel"
      label="Speichern"
      @click="triggerSave"
    />
    <div class="flex flex-row items-end justify-start">
      <div v-if="saveIsInProgress">
        <div class="flex h-[25px] w-[30px] flex-wrap items-center">
          <span class="material-icons"> cloud_upload </span>
        </div>
        <p class="text-sm font-normal tracking-[0.16px]">
          Daten werden gespeichert
        </p>
      </div>
      <div v-if="lastSaveError !== undefined && !saveIsInProgress">
        <div class="icon text-red-900">
          <span class="material-icons"> error_outline </span>
        </div>
        <p class="text-base font-normal tracking-[0.16px]">
          Fehler beim Speichern
        </p>
      </div>
      <div v-if="lastSavedOn !== undefined && !saveIsInProgress">
        <p class="text-sm font-normal tracking-[0.16px]">
          Zuletzt gespeichert um
          <span
            class="duration-2000 scale-87 text-base transition delay-1000 ease-in"
            >{{ getCurrentTime(lastSavedOn) }}</span
          >
          Uhr
        </p>
      </div>
    </div>
  </div>
</template>
