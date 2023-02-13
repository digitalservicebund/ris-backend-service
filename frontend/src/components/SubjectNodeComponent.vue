<script lang="ts" setup>
import { SubjectNode } from "@/domain/SubjectTree"

interface Props {
  node: SubjectNode
  selected: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: "node:toggle", nodeId: string): void
  (e: "node:add", node: SubjectNode): void
  (e: "node:delete", nodeId: string): void
}>()
</script>

<template>
  <div :style="{ 'padding-left': `${props.node.depth * 40}px` }">
    <button
      class="align-middle pr-4 text-blue-800"
      @click="emit('node:toggle', node.id)"
    >
      <span
        aria-label="Sachgebietsbaum aufklappen"
        class="bg-blue-200 material-icons rounded-full w-icon"
        >{{
          props.node.isLeaf ? "eco" : props.node.isExpanded ? "remove" : "add"
        }}</span
      >
    </button>
    <button
      v-if="!selected"
      class="align-middle pr-4 text-blue-800"
      @click="emit('node:add', node)"
    >
      <span
        aria-label="Sachgebiet hinzufügen"
        class="material-icons rounded-full w-icon"
        >check_box_outline_blank</span
      >
    </button>
    <button
      v-if="selected"
      class="align-middle pr-4 text-blue-800"
      @click="emit('node:delete', node.id)"
    >
      <span
        aria-label="Sachgebiet entfernen"
        class="material-icons rounded-full w-icon"
        >check_box</span
      >
    </button>
    <span v-if="props.node.id !== 'root'" class="node-id pl-6">{{
      props.node.id
    }}</span>
    <span class="node-stext pl-6 text-blue-800">{{ props.node.stext }}</span>
  </div>
</template>

<style lang="scss" scoped>
.node-id {
  font-size: 16px;
}

.node-stext {
  font-size: 14px;
}
</style>
