<script lang="ts" setup>
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed } from "vue"
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

const getColumnWidth = (field: string) => {
  if (field === "createdAt") {
    return { width: "250px" }
  } else if (field === "createdBy" || field === "description") {
    return { width: "fit-content", wordBreak: "break-all" }
  }
  return {} // default style
}

const formattedData = computed(() => {
  if (props.loading) {
    return []
  }

  return (
    props.data?.map((item) => ({
      ...item,
      createdAt: formatTimestamp(item.createdAt),
      createdBy: formatCreatedBy(item.documentationOffice, item.createdBy),
    })) ?? []
  )
})

const formatTimestamp = (date?: string) =>
  date ? DateUtil.formatDateTime(date) : "–"

const formatCreatedBy = (docOffice?: string, createdBy?: string) => {
  if (!docOffice && !createdBy) return "–"
  if (!createdBy) return docOffice
  if (!docOffice) return createdBy
  return `${docOffice} (${createdBy})`
}
</script>

<template>
  <div class="flex flex-col py-24">
    <h2 class="ris-body1-bold pb-16">Historie</h2>
    <DataTable
      data-testid="document-unit-history-log"
      :loading="loading"
      scroll-height="315px"
      scrollable
      :value="formattedData"
    >
      <Column
        v-for="col in columns"
        :key="col.field"
        :field="col.field"
        :header="col.header"
        :style="getColumnWidth(col.field)"
      />
    </DataTable>
    <div v-if="error" class="bg-blue-100 p-8">
      Die Historie konnte nicht geladen werden.
    </div>
    <!-- Empty state -->
    <div v-else-if="data?.length === 0" class="bg-blue-100 p-8">
      Keine Daten
    </div>
  </div>
</template>
