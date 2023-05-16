<script lang="ts" setup>
import { h } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import DataSetSummary, {
  withSummarizer,
  defaultSummarizer,
} from "@/shared/components/DataSetSummary.vue"

function chipSummarizer(dataEntry: undefined) {
  return h(
    "div",
    { class: `bg-yellow-700 rounded-full py-4 px-10 text-white font-bold` },
    defaultSummarizer(dataEntry)
  )
}

const ChipSummary = withSummarizer(chipSummarizer)
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
    title="A Much Longer Section Name"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>

  <ExpandableDataSet
    :data-set="['First Value', 'Second Value']"
    :summary-component="ChipSummary"
    title="Special Section"
  >
    <div class="h-64">Content</div>
  </ExpandableDataSet>
</template>
