<script lang="ts" setup>
import { SubjectNode } from "@/domain/SubjectTree"

interface Props {
  node: SubjectNode
  selected: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: "node:toggle", node: SubjectNode): void
  (e: "node:add", node: SubjectNode): void
  (e: "node:delete", nodeId: string): void
}>()
</script>

<template>
  <div :style="{ 'padding-left': `${props.node.depth * 40}px` }">
    <button
      class="align-middle pr-4 text-blue-800"
      @click="emit('node:toggle', node)"
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
      @click="emit('node:delete', node.subjectFieldNumber)"
    >
      <span
        aria-label="Sachgebiet entfernen"
        class="material-icons rounded-full w-icon"
        >check_box</span
      >
    </button>
    <span
      v-if="props.node.subjectFieldNumber !== 'root'"
      class="pl-6 subject-field-number"
      >{{ props.node.subjectFieldNumber }}
    </span>
    <span class="pl-6 subject-field-text text-blue-800">{{
      props.node.subjectFieldText
    }}</span>
  </div>
</template>

<style lang="scss" scoped>
.subject-field-number {
  font-size: 16px;
}

.subject-field-text {
  font-size: 14px;
}
</style>
