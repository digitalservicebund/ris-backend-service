<script lang="ts" setup>
import { h } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import DataSetSummary, {
  withSummarizer,
  defaultSummarizer,
} from "@/shared/components/DataSetSummary.vue"

function chipSummarizer(this: { color: string }, dataEntry: undefined) {
  return h(
    "div",
    { class: `bg-${this.color} rounded-full py-4 px-10 text-white font-bold` },
    defaultSummarizer(dataEntry)
  )
}

const ChipSummary = withSummarizer(chipSummarizer.bind({ color: "yellow-700" }))
</script>

<template>
  <ExpandableDataSet
    :data-set="['First Value', 'SecondValue']"
    title="Short Section"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>

  <ExpandableDataSet
    :data-set="['Single Value']"
    :summary-component="DataSetSummary"
    title="Long Section Name"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>

  <ExpandableDataSet
    :data-set="['First Value', 'SecondValue']"
    :summary-component="ChipSummary"
    title="Short Section"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>
</template>
