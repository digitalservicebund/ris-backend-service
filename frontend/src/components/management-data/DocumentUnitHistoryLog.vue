<script lang="ts" setup>
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed } from "vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import DateUtil from "@/utils/dateUtil"

const props = defineProps<{
  data?: DocumentationUnitHistoryLog[]
  loading: boolean
}>()

// Dynamically generate the columns based on the keys in the data
const columns = [
  { field: "createdAt", header: "Änderung am" },
  { field: "createdBy", header: "Von" },
  { field: "description", header: "Was" },
]
const rowClass = () => {
  return "bg-blue-100"
}

const formattedData = computed(
  () =>
    props.data?.map((item) => ({
      ...item,
      createdAt: formatTimestamp(item.createdAt),
      createdBy: `${item.documentationOffice} (${item.createdBy})`,
    })) ?? [],
)

const formatTimestamp = (date?: string) =>
  date ? DateUtil.formatDateTime(date) : "–"
</script>

<template>
  <div class="py-24">
    <h2 class="ris-body1-bold pb-16">Historie:</h2>
    <DataTable
      :loading="loading"
      :row-class="rowClass"
      scroll-height="250px"
      scrollable
      :value="formattedData"
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
