<script lang="ts" setup>
import { createNode, FieldOfLaw } from "@/domain/fieldOfLaw"

interface Props {
  text: string
  keywords: string[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "linked-field:select": [node: FieldOfLaw]
}>()

type Token = {
  content: string
  isLink: boolean
}

function tokenizeText(): Token[] {
  if (props.keywords.length === 0) {
    return [{ content: props.text, isLink: false }]
  }
  return props.text
    .split(new RegExp(`(${props.keywords.join("|")})`))
    .map((part) => ({
      content: part,
      isLink: props.keywords.includes(part),
    }))
}

function handleTokenClick(token: Token) {
  if (!token.isLink) return
  const toNode = createNode(token.content)
  emit("linked-field:select", toNode)
}
</script>

<template>
  <button
    v-for="(token, idx) in tokenizeText()"
    :key="idx"
    class="text-left"
    :class="token.isLink && 'linked-field'"
    tabindex="0"
    @click="handleTokenClick(token)"
    @keyup.enter="handleTokenClick(token)"
  >
    {{ token.content }}
  </button>
</template>

<style lang="scss" scoped>
.linked-field {
  cursor: pointer;
  text-decoration: underline;
}
</style>
