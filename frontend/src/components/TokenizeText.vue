<script lang="ts" setup>
interface Props {
  text: string
  keywords: string[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (event: "linkToken:clicked", content: string): void
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
  emit("linkToken:clicked", token.content)
}
</script>

<template>
  <span
    v-for="(token, idx) in tokenizeText()"
    :key="idx"
    :class="token.isLink && 'linked-field'"
    @click="handleTokenClick(token)"
    @keyup.enter="handleTokenClick(token)"
  >
    {{ token.content }}
  </span>
</template>

<style lang="scss" scoped>
.linked-field {
  cursor: pointer;
  text-decoration: underline;
}
</style>
