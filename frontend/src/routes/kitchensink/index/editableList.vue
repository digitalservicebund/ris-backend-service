<script lang="ts" setup>
import { computed, h, ref } from "vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import EditableListItem from "@/domain/editableListItem"
import DummyInputGroup from "@/kitchensink/components/DummyInputGroup.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import DummyListItem from "@/kitchensink/domain/dummyListItem"

const listWithEntries = ref<DummyListItem[]>([
  new DummyListItem({ text: "foo" }),
  new DummyListItem({ text: "bar" }),
])

function summerizer(dataEntry: EditableListItem) {
  return h("div", { class: ["ris-label1-regular"] }, dataEntry.renderSummary)
}

const SummaryComponent = withSummarizer(summerizer)

const emptyList = ref([])

const localModelValue = computed({
  get: () => listWithEntries.value,
  set: (value: DummyListItem[]) => {
    listWithEntries.value = value
  },
})
</script>

<template>
  <KitchensinkPage name="Editable list">
    <KitchensinkStory name="With entries">
      <EditableList
        v-model="localModelValue"
        :create-entry="() => new DummyListItem()"
        :edit-component="DummyInputGroup"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
    <KitchensinkStory name="With no entries">
      <EditableList
        v-model="emptyList"
        :create-entry="() => new DummyListItem()"
        :edit-component="DummyInputGroup"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
