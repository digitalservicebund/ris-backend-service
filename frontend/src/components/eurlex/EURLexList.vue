<script lang="ts" setup>
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import Select from "primevue/select"
import { computed, onMounted, ref } from "vue"
import { ComboboxItem } from "@/components/input/types"
import Pagination, { Page } from "@/components/Pagination.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnitParameters } from "@/domain/documentUnit"
import EURLexResult from "@/domain/eurlex"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/comboboxItemService"
import documentationUnitService from "@/services/documentUnitService"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconCallMade from "~icons/material-symbols/call-made"

const props = defineProps<{
  pageEntries?: Page<EURLexResult>
}>()

const emit = defineEmits<{
  updatePage: [number]
}>()

const selectedEntries = ref<EURLexResult[]>([])
const noDecisionSelected = ref<boolean>(false)
const selectedDocumentationOffice = ref<DocumentationOffice>()
const noDocumentationOfficeSelected = ref<boolean>(false)
const documentationOffices = ref<DocumentationOffice[]>()

const entries = computed(() => {
  return props.pageEntries?.content || []
})

onMounted(async () => {
  const comboboxItems: ComboboxItem[] | null = (
    await service.getDocumentationOffices(ref(undefined))
  ).data.value

  documentationOffices.value = comboboxItems?.map(
    (item) => item.value as DocumentationOffice,
  )
})

function openPreview(entry: EURLexResult) {
  if (entry && entry.celex) {
    window.open(entry.htmlLink, "_blank")
  }
}

async function handleAssignToDocOffice() {
  if (selectedEntries.value?.length == 0) {
    noDecisionSelected.value = true
  }

  if (!selectedDocumentationOffice.value) {
    noDocumentationOfficeSelected.value = true
  }

  const params: DocumentationUnitParameters = {
    documentationOffice: selectedDocumentationOffice.value,
    celexNumber: selectedEntries.value[0].celex,
  }
  await documentationUnitService.createNew(params)
}

function selectRow() {
  noDecisionSelected.value = selectedEntries.value.length == 0
}

function selectDocumentationOffice() {
  noDocumentationOfficeSelected.value =
    selectedDocumentationOffice.value == undefined
}
</script>

<template>
  <div class="flex flex-col items-start justify-self-end">
    <div class="flex">
      <Select
        v-model="selectedDocumentationOffice"
        aria-label="Dokumentationstelle auswählen"
        class="w-2xs"
        option-label="abbreviation"
        :options="documentationOffices"
        placeholder="Dokumentationsstelle auswählen"
        @change="selectDocumentationOffice"
      ></Select>
      <Button
        class="ml-8"
        label="Zuweisen"
        severity="secondary"
        @click="handleAssignToDocOffice"
      ></Button>
    </div>
    <span
      v-if="noDocumentationOfficeSelected"
      class="ris-body3-regular mt-8 flex text-red-900"
    >
      <IconErrorOutline class="mr-8" />
      {{ errorMessages.EURLEX_NO_DOCUMENTATION_OFFICE_SELECTED.title }}
    </span>
  </div>
  <div class="m-24">
    <Pagination
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <span
        v-if="noDecisionSelected"
        class="ris-body3-regular flex text-red-900"
      >
        <IconErrorOutline class="mr-8 ml-16" />
        {{ errorMessages.EURLEX_NO_DECISION_SELECTED.title }}
      </span>
      <DataTable
        v-model:selection="selectedEntries"
        data-key="celex"
        table-style="min-width: 50rem"
        :value="entries"
        @row-select="selectRow"
      >
        <Column header-style="width: 3rem" selection-mode="multiple"></Column>
        <Column field="celex" header="CELEX"></Column>
        <Column field="courtType" header="Gerichtstyp"></Column>
        <Column field="courtLocation" header="Ort"></Column>
        <Column header="Datum">
          <template #body="{ data }">
            {{ dayjs(data.date).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column field="fileNumber" header="Aktenzeichen"></Column>
        <Column header="Veröffentlichungsdatum">
          <template #body="{ data }">
            {{ dayjs(data.publicationDate).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column>
          <template #body="{ data }">
            <Button
              class="self-start"
              icon="pi"
              severity="secondary"
              size="small"
              @click="openPreview(data)"
            >
              <IconCallMade />
            </Button>
          </template>
          ></Column
        >
      </DataTable>
    </Pagination>
  </div>
</template>
