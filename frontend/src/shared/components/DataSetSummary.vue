<script lang="ts" generic="T" setup>
import { computed, h } from "vue"
import type { VNode } from "vue"

const props = defineProps<{
  // Remove validation once Vue 3.3.0 got released which fixes this issue.
  data: {
    type: T | T[]
    validator: () => true
  }
  summarizer?: (dataEntry: T) => string | VNode
}>()

const dataAsList = computed(() =>
  Array.isArray(props.data) ? props.data : [props.data]
)

// Remove type cast once Vue 3.3.0 got released which fixes this issue.
const summarizer = computed(
  () =>
    (props.summarizer ?? defaultSummarizer) as (dataEntry: T) => string | VNode
)

const summaries = computed(() => dataAsList.value.map(wrappedSummarizer))

function defaultSummarizer(dataEntry: T): string {
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

function wrappedSummarizer(dataEntry: T): VNode {
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
