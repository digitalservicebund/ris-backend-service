<script lang="ts" setup>
import FlexContainer from "@/components/FlexContainer.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

defineProps<{
  fieldOfLaw: FieldOfLaw
  showBin?: boolean
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:select": [node: FieldOfLaw]
  "linked-field:select": [node: FieldOfLaw]
}>()
</script>

<template>
  <FlexContainer
    class="ds-link-02-bold py-4"
    flex-direction="flex-row"
    justify-content="justify-start"
  >
    <button
      :aria-label="
        fieldOfLaw.identifier +
        ' ' +
        fieldOfLaw.text +
        ' im Sachgebietsbaum anzeigen'
      "
      class="whitespace-nowrap"
      tabindex="0"
      @click="emit('node:select', fieldOfLaw)"
      @keyup.enter="emit('node:select', fieldOfLaw)"
    >
      {{ fieldOfLaw.identifier }} | {{ fieldOfLaw.text }}
    </button>
  </FlexContainer>
</template>
