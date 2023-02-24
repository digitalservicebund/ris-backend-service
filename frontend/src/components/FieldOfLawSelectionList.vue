<script lang="ts" setup>
import FieldOfLawSelectionListEntry from "./FieldOfLawSelectionListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLawTree"

const props = defineProps<{
  selectedSubjects: FieldOfLawNode[]
}>()
const emit = defineEmits<{
  (event: "remove-from-list", index: number): void
  (event: "node-clicked", node: FieldOfLawNode): void
}>()
</script>

<template>
  <h1 class="heading-03-regular pb-8">Auswahl</h1>
  <div v-if="!props.selectedSubjects.length">Die Liste ist aktuell leer</div>
  <div v-else>
    <FieldOfLawSelectionListEntry
      v-for="(subject, index) in props.selectedSubjects"
      :key="subject.identifier"
      :subject="subject"
      @node-clicked="emit('node-clicked', subject)"
      @remove-from-list="emit('remove-from-list', index)"
    />
  </div>
</template>
