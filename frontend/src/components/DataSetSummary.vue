<script lang="ts" setup>
import { computed, h } from "vue"
import type { VNode } from "vue"

const props = defineProps<{
  data: unknown | object | unknown[]
  summarizer?: (dataEntry: unknown) => string | VNode
}>()

const dataAsList = computed(() =>
  Array.isArray(props.data) ? props.data : [props.data]
)

const summarizer = computed(() => props.summarizer ?? defaultSummarizer)
const summaries = computed(() => dataAsList.value.map(wrappedSummarizer))

function defaultSummarizer(data: unknown): string {
  if (["string", "boolean", "number"].includes(typeof data)) {
    return `${data}`
  } else if (Array.isArray(data)) {
    return data.map(defaultSummarizer).join(", ")
  } else if (typeof data == "object" && data !== null) {
    return Object.values(data).map(defaultSummarizer).join(" | ")
  } else {
    return ""
  }
}

function wrappedSummarizer(data: unknown): VNode {
  const summary = summarizer.value(data)
  return typeof summary == "string" ? h("span", summary) : summary
}
</script>

<template>
  <div class="flex flex-col items-start">
    <component
      :is="summary"
      v-for="(summary, index) in summaries"
      :key="index"
      class="border-b-1 border-b-blue-500 label-02-reg last:border-none py-10"
    />
  </div>
</template>
