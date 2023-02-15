<script lang="ts" setup>
import SubjectSelectionListEntry from "./SubjectSelectionListEntry.vue"
import { SubjectNode } from "@/domain/SubjectTree"

const props = defineProps<{
  selectedSubjects: SubjectNode[]
}>()
const emit = defineEmits<{
  (event: "remove-from-list", index: number): void
  (event: "node-clicked", node: SubjectNode): void
}>()
</script>

<template>
  <h1 class="heading-03-regular pb-8">Auswahl</h1>
  <div v-if="!props.selectedSubjects.length">Die Liste ist aktuell leer</div>
  <div v-else>
    <SubjectSelectionListEntry
      v-for="(subject, index) in props.selectedSubjects"
      :key="subject.subjectFieldNumber"
      :subject="subject"
      @node-clicked="emit('node-clicked', subject)"
      @remove-from-list="emit('remove-from-list', index)"
    />
  </div>
</template>
