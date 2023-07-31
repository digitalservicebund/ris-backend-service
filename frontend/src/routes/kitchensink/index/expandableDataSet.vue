<script lang="ts" setup>
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import DataSetSummary, {
  withSummarizer,
  defaultSummarizer,
} from "@/shared/components/DataSetSummary.vue"

function checkMarkSummarizer(dataEntry: undefined) {
  const summarizerData: SummarizerDataSet[] = []
  summarizerData.push(
    new SummarizerDataSet([defaultSummarizer(dataEntry)], {
      type: Type.CHECKMARK,
    }),
  )
  return normsMetadataSummarizer(summarizerData)
}

const CheckmarkSummary = withSummarizer(checkMarkSummarizer)
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
        :data-set="['Checkbox example value']"
        :summary-component="CheckmarkSummary"
        title="Special Section"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
