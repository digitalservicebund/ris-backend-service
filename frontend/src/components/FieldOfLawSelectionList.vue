<script lang="ts" setup>
import FieldOfLawListEntry from "./FieldOfLawListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{
  selectedFieldsOfLaw: FieldOfLawNode[]
}>()
const emit = defineEmits<{
  (event: "remove-from-list", identifier: string): void
  (event: "node-clicked", node: FieldOfLawNode): void
  (event: "linkedField:clicked", identifier: string): void
}>()

function handleLinkedFieldClicked(identifier: string) {
  emit("linkedField:clicked", identifier)
}
</script>

<template>
  <div class="pt-20">
    <div v-if="!props.selectedFieldsOfLaw.length">
      Die Liste ist aktuell leer
    </div>
    <div v-else>
      <hr class="border-blue-500 mt-20 w-full" />
      <FieldOfLawListEntry
        v-for="fieldOfLaw in props.selectedFieldsOfLaw"
        :key="fieldOfLaw.identifier"
        :field-of-law="fieldOfLaw"
        show-bin
        @linked-field:clicked="handleLinkedFieldClicked"
        @node-clicked="emit('node-clicked', fieldOfLaw)"
        @remove-from-list="emit('remove-from-list', fieldOfLaw.identifier)"
      />
    </div>
  </div>
</template>
