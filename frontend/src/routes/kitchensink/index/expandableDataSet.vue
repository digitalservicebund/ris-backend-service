<script lang="ts" setup>
import { h } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import DataSetSummary, {
  withSummarizer,
  defaultSummarizer,
} from "@/shared/components/DataSetSummary.vue"

function chipSummarizer(dataEntry: undefined) {
  return h(
    "div",
    { class: `bg-yellow-700 rounded-full py-4 px-10 text-white font-bold` },
    defaultSummarizer(dataEntry),
  )
}

const ChipSummary = withSummarizer(chipSummarizer)
</script>

<template>
  <KitchensinkPage name="Expandable data set">
    <KitchensinkStory name="Short section">
      <ExpandableDataSet
        :data-set="['First Value', 'SecondValue']"
        title="Short Section"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>

    <KitchensinkStory name="Long section">
      <ExpandableDataSet
        :data-set="['Single Value']"
        :summary-component="DataSetSummary"
        title="A Much Longer Section Name"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>

    <KitchensinkStory name="Special section">
      <ExpandableDataSet
        :data-set="['First Value', 'Second Value']"
        :summary-component="ChipSummary"
        title="Special Section"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
