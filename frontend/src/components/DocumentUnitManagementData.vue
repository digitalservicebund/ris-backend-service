<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import DuplicateRelationListItem from "@/components/DuplicateRelationListItem.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore())

const data = [
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "NeuRIS",
    subject: "Status verändert auf „Veröffentlicht“",
  },
  {
    timestamp: "31.03.2025 15:36",
    name: "BGH (Anna Agensburg)",
    subject: "Dokument an JDV-Portal übergeben",
  },
]

// Dynamically generate the columns based on the keys in the data
const columns = [
  { field: "timestamp", header: "Änderung am" },
  { field: "name", header: "Von" },
  { field: "subject", header: "Was" },
]
const rowClass = () => {
  return "bg-blue-100"
}
</script>

<template>
  <div class="w-full grow p-24">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Verwaltungsdaten</TitleElement>
      <div class="py-24">
        <h2 class="ris-body1-bold pb-16">Historie:</h2>
        <DataTable
          :row-class="rowClass"
          scroll-height="250px"
          scrollable
          :value="data"
        >
          <Column
            v-for="col in columns"
            :key="col.field"
            :field="col.field"
            :header="col.header"
            header-class="ris-label2-bold text-gray-900"
          ></Column>
        </DataTable>
      </div>
      <dl>
        <div class="flex gap-24 px-0">
          <dt class="ris-body1-bold shrink-0 grow-0 basis-160">
            Dublettenverdacht:
          </dt>
          <dd class="ris-body2-regular flex flex-col gap-32">
            <DuplicateRelationListItem
              v-for="duplicateRelation in documentUnit?.managementData
                .duplicateRelations"
              :key="duplicateRelation.documentNumber"
              :duplicate-relation="duplicateRelation"
            />
            <div
              v-if="!documentUnit?.managementData.duplicateRelations.length"
              class="flex flex-row gap-8"
            >
              <IconCheck class="text-green-700" />
              <span>Es besteht kein Dublettenverdacht.</span>
            </div>
          </dd>
        </div>
      </dl>
    </div>
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement
        >Dokumentationseinheit "{{ documentUnit?.documentNumber }}"
        löschen</TitleElement
      >
      <DocumentUnitDeleteButton
        :document-number="documentUnit?.documentNumber!"
        :uuid="documentUnit?.uuid!"
      />
    </div>
  </div>
</template>
