<script lang="ts" setup>
import TextButton from "./TextButton.vue"
import { SubjectNode } from "@/domain/SubjectTree"

interface Props {
  node: SubjectNode
}

const props = defineProps<Props>()

const emits = defineEmits<{
  (e: "node:toggle", nodeId: string): void
  (e: "node:add", nodeId: string): void
}>()
</script>

<template>
  <div :style="{ 'padding-left': `${props.node.depth * 40}px` }">
    <button
      class="align-middle pr-4 text-blue-800"
      @click="emits('node:toggle', node.id)"
    >
      <span
        aria-label="Sachgebietsbaum aufklappen"
        class="bg-blue-200 material-icons rounded-full w-icon"
        >{{
          props.node.isLeaf ? "eco" : props.node.isExpanded ? "remove" : "add"
        }}</span
      >
    </button>
    <span v-if="props.node.id !== 'root'" class="node-id pl-6">{{
      props.node.id
    }}</span>
    <span class="node-stext pl-6 text-blue-800">{{ props.node.stext }}</span>
    <TextButton
      aria-label="Übernehmen"
      button-type="tertiary"
      label="Übernehmen"
      @click="$emit('node:add', node.id)"
    ></TextButton>
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
