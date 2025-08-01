<script lang="ts" setup>
import Button from "primevue/button"
import { onBeforeUnmount } from "vue"
import { useSaveToRemote } from "@/composables/useSaveToRemote"

const props = defineProps<{
  ariaLabel: string
}>()

const { saveIsInProgress, triggerSave, lastSaveError, formattedLastSavedOn } =
  useSaveToRemote(10_000)

const getErrorDetails = () =>
  lastSaveError.value?.title ? ": " + lastSaveError.value.title : ""

onBeforeUnmount(async function () {
  await triggerSave()
  window.onbeforeunload = null
})
window.onbeforeunload = function () {
  void triggerSave() // NOSONAR: The void is needed to avoid a browser warning on reload
}
</script>

<template>
  <div class="ml-12 flex items-center space-x-[12px] whitespace-nowrap">
    <p
      v-if="lastSaveError !== undefined"
      class="ris-label1-regular text-red-800"
    >
      Fehler beim Speichern{{ getErrorDetails() }}
    </p>
    <p v-else-if="saveIsInProgress === true" class="ris-label1-regular">
      speichern...
    </p>
    <p
      v-else-if="formattedLastSavedOn !== undefined"
      class="ris-label1-regular"
    >
      Gespeichert:
      <span>{{ formattedLastSavedOn }}</span>
      Uhr
    </p>
    <slot />
    <Button
      :aria-label="props.ariaLabel"
      data-testid="save-button"
      label="Speichern"
      size="small"
      @click="triggerSave"
    />
  </div>
</template>
