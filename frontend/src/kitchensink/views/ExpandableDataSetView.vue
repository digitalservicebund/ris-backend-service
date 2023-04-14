<script lang="ts" setup>
import { h, ref } from "vue"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import ParticipatingInstitutionsInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import PrintAnnouncementInputGroup from "@/components/PrintAnnouncementInputGroup.vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { MetadatumType } from "@/domain/Norm"
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

const leadData = ref<string[]>([])
const subjectData = ref<string[]>([])
const participationData = ref<string[]>([])
const citationDateData = ref<string[]>([])
const ageIndicationData = ref<string[]>([])
const printAnnouncementData = ref<string[]>([])

const defaultLead = { jurisdiction: "", unit: "" }
const defaultSubject = { fna: "", previousFna: "", gesta: "", bgb3: "" }
const defaultParticipation = { type: "", institution: "" }
const defaultAgeIndication = {
  start: "",
  finish: "",
  startUnitValue: "",
  endUnitValue: "",
}
const defaultPrintAnnouncement = { type: "", institution: "" }

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

  <ExpandableDataSet :data-set="leadData" title="Lead">
    <EditableList
      v-model="leadData"
      :default-value="defaultLead"
      :edit-component="LeadInputGroup"
    />
  </ExpandableDataSet>

  <ExpandableDataSet :data-set="subjectData" title="Subject">
    <EditableList
      v-model="subjectData"
      :default-value="defaultSubject"
      :edit-component="SubjectAreaInputGroup"
    />
  </ExpandableDataSet>

  <ExpandableDataSet :data-set="participationData" title="Participation">
    <EditableList
      v-model="participationData"
      :default-value="defaultParticipation"
      :edit-component="ParticipatingInstitutionsInputGroup"
    />
  </ExpandableDataSet>

  <ExpandableDataSet :data-set="citationDateData" title="Zitierdatum">
    <EditableList
      v-model="citationDateData"
      :default-value="{}"
      :edit-component="CitationDateInputGroup"
    />
  </ExpandableDataSet>

  <ExpandableDataSet :data-set="ageIndicationData" title="Altersangabe">
    <EditableList
      v-model="ageIndicationData"
      :default-value="defaultAgeIndication"
      :edit-component="AgeIndicationInputGroup"
    />
  </ExpandableDataSet>

  <ExpandableDataSet
    :data-set="printAnnouncementData"
    title="Amtliche Fundstelle"
  >
    <EditableList
      v-model="printAnnouncementData"
      :default-value="defaultAgeIndication"
      :edit-component="PrintAnnouncementInputGroup"
    />
  </ExpandableDataSet>
</template>
