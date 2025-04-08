<script lang="ts" setup>
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import LoadingSpinner from "@/components/LoadingSpinner.vue"

type LogItem = {
  timestamp: string
  name: string
  subject: string
}

defineProps<{
  data?: LogItem[]
  loading: boolean
}>()

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
      :loading="loading"
      :row-class="rowClass"
      scroll-height="250px"
      scrollable
      :value="data"
    >
      <template #empty> Keine Daten. </template>
      <template #loading>
        <LoadingSpinner size="small" />
      </template>
      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
        header-class="ris-label2-bold text-gray-900"
      >
      </Column>
    </DataTable>
  </div>
</template>
