<script setup lang="ts">
import { storeToRefs } from "pinia"
import { useValidateNormFrame } from "@/composables/useValidateNormFrame"
import { validateNormFrame } from "@/services/norms"
import TextButton from "@/shared/components/input/TextButton.vue"
import { useSaveToRemote } from "@/shared/composables/useSaveToRemote"
import { useGlobalValidationErrorStore } from "@/stores/globalValidationErrorStore"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)
const validationErrorStore = useGlobalValidationErrorStore()
const { triggerValidation, lastValidateError, validateIsInProgress } =
  useValidateNormFrame(
    () => {
      validationErrorStore.removeByScope("NORM")
    },
    () => validateNormFrame(loadedNorm.value?.metadataSections),
    (errors) => {
      validationErrorStore.add(...errors)
    },
  )

const { triggerSave, saveIsInProgress, lastSaveError, formattedLastSavedOn } =
  useSaveToRemote(loadedNormStore.update, 10000)
</script>

<template>
  <div class="flex flex-row items-end justify-start gap-4 space-x-[10px]">
    <div class="flex flex-col">
      <TextButton
        aria-label="Rahmendaten Speichern Button"
        label="Speichern"
        @click="triggerSave"
      />
      <div class="mt-2 min-h-[60px] space-y-2">
        <div v-if="saveIsInProgress" class="text-sm flex items-center gap-4">
          <span class="material-icons"> cloud_upload </span>
          wird gespeichert
        </div>
        <div v-if="lastSaveError !== undefined" class="flex items-center gap-4">
          <span class="material-icons text-red-900"> error_outline </span>
          {{ lastSaveError?.title }}
        </div>
        <div
          v-if="formattedLastSavedOn !== undefined && !saveIsInProgress"
          class="text-sm"
        >
          Zuletzt {{ formattedLastSavedOn }} Uhr
        </div>
      </div>
    </div>
    <div>
      <TextButton
        aria-label="Daten prüfen"
        button-type="secondary"
        label="Daten prüfen"
        @click="triggerValidation"
      />
      <div class="mt-2 min-h-[60px] space-y-2">
        <div
          v-if="validateIsInProgress"
          class="text-sm flex items-center gap-4"
        >
          <span class="material-icons"> cloud_upload </span>
          Validierung läuft
        </div>
        <div v-if="lastValidateError" class="flex items-center gap-4">
          <span class="material-icons text-red-900"> error_outline </span>
          {{ lastValidateError?.title }}
        </div>
      </div>
    </div>
  </div>
</template>
