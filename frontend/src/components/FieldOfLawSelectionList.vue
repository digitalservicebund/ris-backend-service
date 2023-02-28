<script lang="ts" setup>
import FieldOfLawSelectionListEntry from "./FieldOfLawSelectionListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{
  selectedNodes: FieldOfLawNode[]
}>()
const emit = defineEmits<{
  (event: "remove-from-list", index: number): void
  (event: "node-clicked", node: FieldOfLawNode): void
  (event: "linkedField:clicked", identifier: string): void
}>()

function handleLinkedFieldClicked(identifier: string) {
  emit("linkedField:clicked", identifier)
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Auswahl</h1>
  <div v-if="!props.selectedNodes.length">Die Liste ist aktuell leer</div>
  <div v-else>
    <FieldOfLawSelectionListEntry
      v-for="(node, index) in props.selectedNodes"
      :key="node.identifier"
      :node="node"
      @linked-field:clicked="handleLinkedFieldClicked"
      @node-clicked="emit('node-clicked', node)"
      @remove-from-list="emit('remove-from-list', index)"
    />
  </div>
</template>
