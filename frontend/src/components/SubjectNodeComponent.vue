<script lang="ts" setup>
import { ROOT_ID, SubjectNode } from "@/domain/SubjectTree"

interface Props {
  node: SubjectNode
  selected: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (event: "node:toggle", node: SubjectNode): void
  (event: "node:select", node: SubjectNode): void
  (event: "node:unselect", subjectFieldNumber: string): void
  (event: "linkedField:clicked", subjectFieldNumber: string): void
}>()

type Token = {
  content: string
  isLink: boolean
}

function tokenizeText(): Token[] {
  const stext = props.node.subjectFieldText
  const keywords = props.node.linkedFields
  if (!keywords) return [{ content: stext, isLink: false }]
  return stext.split(new RegExp(`(${keywords.join("|")})`)).map((part) => ({
    content: part,
    isLink: keywords.includes(part),
  }))
}

function handleTokenClick(token: Token) {
  if (!token.isLink) return
  emit("linkedField:clicked", token.content)
}
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
        class="align-top appearance-none border-2 focus:outline-2 h-24 hover:outline-2 ml-4 outline-0 outline-blue-800 outline-none outline-offset-[-4px] rounded-sm text-blue-800 w-24"
        @click="
          selected
            ? emit('node:unselect', node.subjectFieldNumber)
            : emit('node:select', node)
        "
      >
        <span
          v-if="selected"
          aria-label="Sachgebiet entfernen"
          class="material-icons selected-icon"
          >done</span
        >
      </button>
    </span>
    <div
      v-if="props.node.subjectFieldNumber !== ROOT_ID"
      class="pl-8 subject-field-number"
    >
      {{ props.node.subjectFieldNumber }}
    </div>
    <div class="pl-6 pt-2 subject-field-text text-blue-800">
      <span
        v-for="(token, idx) in tokenizeText()"
        :key="idx"
        :class="token.isLink && 'linked-field'"
        @click="handleTokenClick(token)"
        @keyup.enter="handleTokenClick(token)"
      >
        {{ token.content }}
      </span>
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
  font-size: 20px;
}

.linked-field {
  cursor: pointer;
  text-decoration: underline;
}
</style>
