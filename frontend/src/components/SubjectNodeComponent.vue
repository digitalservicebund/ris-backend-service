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
  <div
    class="node-row"
    :style="{ 'padding-left': `${props.node.depth * 40}px` }"
  >
    <span v-if="props.node.isLeaf" class="pl-28"></span>
    <button
      v-else
      class="align-middle pr-4 text-blue-800"
      @click="emit('node:toggle', node)"
    >
      <span
        aria-label="Sachgebietsbaum aufklappen"
        class="bg-blue-200 material-icons rounded-full w-icon"
        >{{ props.node.isExpanded ? "remove" : "add" }}</span
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
    <div
      v-if="props.node.subjectFieldNumber !== 'root'"
      class="pl-6 subject-field-number"
    >
      {{ props.node.subjectFieldNumber }}
    </div>
    <div class="pl-6 pt-2 subject-field-text text-blue-800">
      {{ props.node.subjectFieldText }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.node-row {
  display: flex;
  align-items: flex-start;
}

.subject-field-number {
  font-size: 16px;
}

.subject-field-text {
  font-size: 14px;
}
</style>
