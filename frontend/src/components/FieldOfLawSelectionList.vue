<script lang="ts" setup>
import FieldOfLawListEntry from "./FieldOfLawListEntry.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

const props = defineProps<{
  modelValue: FieldOfLaw[] | undefined
}>()

const emit = defineEmits<{
  "node:select": [node: FieldOfLaw]
  "node:remove": [node: FieldOfLaw]
  "linked-field:select": [node: FieldOfLaw]
}>()
</script>

<template>
  <div class="pt-20">
    <div v-if="props.modelValue && !props.modelValue.length">
      Die Liste ist aktuell leer
    </div>
    <div v-else>
      <FieldOfLawListEntry
        v-for="fieldOfLaw in props.modelValue"
        :key="fieldOfLaw.identifier"
        :field-of-law="fieldOfLaw"
        show-bin
        @linked-field:select="emit('node:select', $event)"
        @node:remove="emit('node:remove', fieldOfLaw)"
        @node:select="emit('node:select', fieldOfLaw)"
      />
    </div>
  </div>
</template>
