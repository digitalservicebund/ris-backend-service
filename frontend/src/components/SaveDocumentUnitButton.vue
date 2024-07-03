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
  <div class="ml-12 flex items-center space-x-[12px] whitespace-nowrap">
    <p v-if="lastSaveError !== undefined" class="ds-label-01-reg text-red-800">
      Fehler beim Speichern{{ getErrorDetails() }}
    </p>
    <p v-else-if="formattedLastSavedOn !== undefined" class="ds-label-01-reg">
      Zuletzt
      <span>{{ formattedLastSavedOn }}</span>
      Uhr
    </p>
    <TextButton
      :aria-label="ariaLabel"
      label="Speichern"
      size="small"
      @click="triggerSave"
    />
  </div>
</template>
