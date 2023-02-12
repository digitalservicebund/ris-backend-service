<script lang="ts" setup>
import { SubjectNode } from "@/domain/SubjectTree"

interface Props {
  node: SubjectNode
}

const props = defineProps<Props>()

const emits = defineEmits<{
  (e: "node:toggle", nodeId: string): void
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
        >{{ props.node.isExpanded ? "remove" : "add" }}</span
      >
    </button>
    <span v-if="props.node.id !== 'root'" class="pl-6">{{
      props.node.id
    }}</span>
    <span class="pl-6 text-blue-800 text-sm">{{ props.node.stext }}</span>
  </div>
</template>
