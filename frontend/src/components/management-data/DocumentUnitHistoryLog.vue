<script lang="ts" setup>
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed } from "vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import { ResponseError } from "@/services/httpClient"
import DateUtil from "@/utils/dateUtil"

const props = defineProps<{
  data?: DocumentationUnitHistoryLog[]
  loading: boolean
  error?: ResponseError
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
      createdBy: item.documentationOffice
        ? `${item.documentationOffice} (${item.createdBy})`
        : item.createdBy,
    })) ?? [],
)

const formatTimestamp = (date?: string) =>
  date ? DateUtil.formatDateTime(date) : "–"
</script>

<template>
  <div class="flex flex-col py-24">
    <h2 class="ris-body1-bold pb-16">Historie:</h2>
    <DataTable
      v-if="formattedData.length > 0 && !loading"
      data-testid="document-unit-history-log"
      :row-class="rowClass"
      scroll-height="250px"
      scrollable
      :value="formattedData"
    >
      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
        header-class="ris-label2-bold text-gray-900 w-64 sticky top-0 bg-white z-10"
      />
    </DataTable>
    <!-- Empty state -->
    <LoadingSpinner v-else-if="loading" class="self-center" size="small" />
    <div v-else-if="error" class="bg-blue-100 p-8">
      Die Historie konnte nicht geladen werden.
    </div>
    <div v-else class="bg-blue-100 p-8">Keine Daten</div>
  </div>
</template>
