<script lang="ts" setup>
import { computed, h } from "vue"
import type { VNode } from "vue"

const props = defineProps<{
  data: undefined
  summarizer?: (dataEntry: undefined) => string | VNode
}>()

const dataAsList = computed(() =>
  Array.isArray(props.data) ? props.data : [props.data]
)

const summarizer = computed(() => props.summarizer ?? defaultSummarizer)
const summaries = computed(() => dataAsList.value.map(wrappedSummarizer))

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function defaultSummarizer(dataEntry: any): string {
  if (["string", "boolean", "number"].includes(typeof dataEntry)) {
    return `${dataEntry}`
  } else if (Array.isArray(dataEntry)) {
    return dataEntry.map(defaultSummarizer).join(", ")
  } else if (typeof dataEntry == "object" && dataEntry !== null) {
    return Object.values(dataEntry).map(defaultSummarizer).join(" | ")
  } else {
    return ""
  }
}

function wrappedSummarizer(dataEntry: undefined): VNode {
  const summary = summarizer.value(dataEntry)
  return typeof summary == "string" ? h("span", summary) : summary
}
</script>

<template>
  <div class="flex flex-col items-start">
    <component
      :is="summary"
      v-for="(summary, index) in summaries"
      :key="index"
      class="border-b-1 border-b-blue-500 first:pt-0 label-02-reg last:border-none last:pb-0 py-10"
    />
  </div>
</template>
