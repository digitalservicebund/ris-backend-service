<script lang="ts" setup>
import { h, ref } from "vue"
import AnnouncementInputGroup from "@/components/AnnouncementGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import { Metadata, MetadataSections } from "@/domain/Norm"
import DataSetSummary, {
  withSummarizer,
  defaultSummarizer,
} from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"

function chipSummarizer(dataEntry: undefined) {
  return h(
    "div",
    { class: `bg-yellow-700 rounded-full py-4 px-10 text-white font-bold` },
    defaultSummarizer(dataEntry)
  )
}

function AnnouncementSummarizer(
  data: Metadata,
  sections: MetadataSections
): string {
  console.log(data)
  if (!data) return ""

  const printGazette = data.ANNOUNCEMENT_GAZETTE?.[0]
  const printYear = data.YEAR?.[0]
  const printNumber = data.NUMBER?.[0]
  const printPage = data.PAGE_NUMBER?.[0]
  const section = sections.PRINT_ANNOUNCEMENT

  return `${section}, ${printGazette}, ${printYear}, ${printNumber}, ${printPage}`
}

const announcementData = ref<string[]>([])

const defaultAnnouncement = { type: "", institution: "" }

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

  <ExpandableDataSet :data-set="announcementData" title="Amtliche Fundstelle">
    <EditableList
      v-model="announcementData"
      :default-value="defaultAnnouncement"
      :edit-component="AnnouncementInputGroup"
      :summary-component="AnnouncementSummarizer"
    />
  </ExpandableDataSet>
</template>
