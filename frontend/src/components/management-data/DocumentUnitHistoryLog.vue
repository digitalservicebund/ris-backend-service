<script lang="ts" setup>
import Column from "primevue/column"
import DataTable from "primevue/datatable"

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
</template>
