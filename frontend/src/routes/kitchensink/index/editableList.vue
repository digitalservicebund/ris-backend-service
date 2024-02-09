<script lang="ts" setup>
import { computed, h, ref } from "vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import EditableListItem from "@/domain/editableListItem"
import DummyInputGroup from "@/kitchensink/components/DummyInputGroup.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import DummyListItem from "@/kitchensink/domain/dummyListItem"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"

const listWithEntries = ref<DummyListItem[]>([
  new DummyListItem({ text: "foo" }),
  new DummyListItem({ text: "bar" }),
])

function summerizer(dataEntry: EditableListItem) {
  return h("div", { class: ["ds-label-01-reg"] }, dataEntry.renderDecision)
}

const SummaryComponent = withSummarizer(summerizer)

const defaultValue = new DummyListItem()
const emptyList = ref([])

const localModelValue = computed({
  get: () => listWithEntries.value,
  set: (value: DummyListItem[]) => {
    console.log("set", value)
    listWithEntries.value = value
  },
})
</script>

<template>
  <KitchensinkPage name="Editable list">
    <KitchensinkStory name="With entries">
      <EditableList
        v-model="localModelValue"
        :default-value="defaultValue"
        :edit-component="DummyInputGroup"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
    <KitchensinkStory name="With no entries">
      <EditableList
        v-model="emptyList"
        :default-value="defaultValue"
        :edit-component="DummyInputGroup"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
