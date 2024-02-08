@ -1,59 +0,0 @@
<script lang="ts" setup>
import { h } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

function demoSummarizer(dataEntry: undefined) {
  return h("div", { class: ["flex flex-row items-center"] }, [
    h(
      h(h(IconErrorOutline), {
        "aria-label": "Fehlerhafte Eingabe",
        class: ["mr-8 text-red-800"],
      }),
    ),
    h("div", { class: ["ds-label-01-reg text-red-800"] }, dataEntry),
  ])
}

const CustomSummary = withSummarizer(demoSummarizer)
</script>

<template>
  <KitchensinkPage name="Expandable data set">
    <KitchensinkStory name="Default summarizer">
      <ExpandableDataSet
        :data-set="['First Value', 'SecondValue']"
        title="Title"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>

    <KitchensinkStory name="Custom Summarizer">
      <ExpandableDataSet
        :data-set="['First Value', 'SecondValue']"
        :summary-component="CustomSummary"
        title="Title"
      >
        <div class="h-64">Content</div>
      </ExpandableDataSet>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
