<script lang="ts" setup>
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
  <div class="flex flex-row gap-8 px-16 py-8">
    <button
      :aria-label="fieldOfLaw.identifier + ' im Sachgebietsbaum anzeigen'"
      class="ds-link-02-bold"
      tabindex="0"
      @click="emit('node:select', fieldOfLaw)"
      @keyup.enter="emit('node:select', fieldOfLaw)"
    >
      <span class="overflow-hidden text-ellipsis whitespace-nowrap">{{
        fieldOfLaw.identifier
      }}</span>
    </button>
    <span class="ds-label-02-reg mt-2"> {{ fieldOfLaw.text }}</span>
  </div>
</template>
