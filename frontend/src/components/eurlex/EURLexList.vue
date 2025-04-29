<script lang="ts" setup>
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import Select from "primevue/select"
import { computed, ref } from "vue"
import { Page } from "@/components/Pagination.vue"
import EURLexResult from "@/domain/eurlex"
import dayjs from "dayjs"
import IconCallMade from "~icons/material-symbols/call-made"
// import IconAdd from "~icons/material-symbols/add"

const props = defineProps<{
  pageEntries?: Page<EURLexResult>
}>()

const emit = defineEmits<{}>()

const selectedEntries = ref<EURLexResult[]>()

const entries = computed(() => {
  return props.pageEntries?.content || []
})

function openPreview() {}

function handleAssignToDokOffice() {
  if (selectedEntries.value?.length == 0) {
  }
}
</script>

<template>
  <div class="flex justify-end">
    <Select
      aria-label="Dokumentationstelle auswählen"
      placeholder="Dokumentationsstelle auswählen"
    ></Select>
    <Button
      class="ml-8"
      label="Zuweisen"
      severity="secondary"
      @click="handleAssignToDokOffice"
    ></Button>
  </div>
  <div class="m-24">
    <DataTable
      v-model:selection="selectedEntries"
      data-key="celex"
      table-style="min-width: 50rem"
      :value="entries"
    >
      <Column header-style="width: 3rem" selection-mode="multiple"></Column>
      <Column field="celex" header="CELEX"></Column>
      <Column field="courtType" header="Gerichtstyp"></Column>
      <Column field="courtLocation" header="Ort"></Column>
      <Column field="date" header="Datum">
        <template #body="{ data }">
          {{ dayjs(data.date).format("DD.MM.YYYY") }}
        </template>
      </Column>
      <Column field="fileNumber" header="Aktenzeichen"></Column>
      <Column field="publicationDate" header="Veröffentlichungsdatum">
        <template #body="{ data }">
          {{ dayjs(data.publicationDate).format("DD.MM.YYYY") }}
        </template>
      </Column>
      <Column>
        <template #body>
          <Button severity="secondary" @click="openPreview">
            <IconCallMade />
          </Button>
        </template>
        ></Column
      >
    </DataTable>
  </div>
</template>
