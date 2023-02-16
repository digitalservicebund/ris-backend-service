<script lang="ts" setup>
import { ROOT_ID, SubjectNode } from "@/domain/SubjectTree"

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
        >{{
          props.node.isExpanded && !props.node.inDirectPathMode
            ? "remove"
            : "add"
        }}</span
      >
    </button>
    <span v-if="node.subjectFieldNumber !== ROOT_ID">
      <button
        v-if="selected"
        class="appearance-none border-2 focus:outline-2 h-24 hover:outline-2 ml-2 outline-0 outline-blue-800 outline-none outline-offset-[-4px] rounded-sm text-blue-800 w-24"
        @click="emit('node:delete', node.subjectFieldNumber)"
      >
        <span
          aria-label="Sachgebiet entfernen"
          class="material-icons selected-icon"
          >done</span
        >
      </button>
      <button
        v-else
        class="appearance-none border-2 focus:outline-2 h-24 hover:outline-2 ml-2 outline-0 outline-blue-800 outline-none outline-offset-[-4px] rounded-sm text-blue-800 w-24"
        @click="emit('node:add', node)"
      ></button>
    </span>
    <div
      v-if="props.node.subjectFieldNumber !== ROOT_ID"
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
  white-space: nowrap;
}

.subject-field-text {
  font-size: 14px;
}

.selected-icon {
  font-size: 14px;
}
</style>
