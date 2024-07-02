<script lang="ts" setup>
import { onBeforeUnmount, toRaw } from "vue"
import TextButton from "@/components/input/TextButton.vue"
import { useSaveToRemote } from "@/composables/useSaveToRemote"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  ariaLabel: string
  serviceCallback: () => Promise<ServiceResponse<void>>
}>()

const { triggerSave, lastSaveError, formattedLastSavedOn } = useSaveToRemote(
  props.serviceCallback,
  10000,
)

const getErrorDetails = () => {
  if (
    lastSaveError.value &&
    toRaw(lastSaveError.value).title &&
    toRaw(lastSaveError.value).title.includes("Berechtigung") // temporary workaround
  ) {
    return ": " + toRaw(lastSaveError.value).title
  }
  return ""
}

onBeforeUnmount(function () {
  triggerSave()
  window.onbeforeunload = null
})
window.onbeforeunload = function () {
  triggerSave()
}
</script>

<template>
  <div class="flex items-center space-x-[12px]">
    <div>
      <div v-if="lastSaveError !== undefined">
        <p class="text-red-800">Fehler beim Speichern{{ getErrorDetails() }}</p>
      </div>

      <div
        v-if="formattedLastSavedOn !== undefined && lastSaveError === undefined"
      >
        <p>
          Zuletzt
          <span>{{ formattedLastSavedOn }}</span>
          Uhr
        </p>
      </div>
    </div>
    <TextButton
      :aria-label="ariaLabel"
      label="Speichern"
      @click="triggerSave"
    />
  </div>
</template>
