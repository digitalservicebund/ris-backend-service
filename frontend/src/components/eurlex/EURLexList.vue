<script lang="ts" setup>
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, onMounted, ref } from "vue"
import DocumentationOfficeSelector from "@/components/DocumentationOfficeSelector.vue"
import { ComboboxItem } from "@/components/input/types"
import Pagination, { Page } from "@/components/Pagination.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import { EurlexParameters } from "@/domain/documentUnit"
import EURLexResult from "@/domain/eurlex"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/comboboxItemService"
import documentationUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconCallMade from "~icons/material-symbols/call-made"

const props = defineProps<{
  pageEntries?: Page<EURLexResult>
}>()

const emit = defineEmits<{
  updatePage: [number, string?, string?, string?, string?, string?]
  handleServiceError: [ResponseError?]
  assign: [number]
}>()

const selectedEntries = ref<EURLexResult[]>([])
const noDecisionSelected = ref<boolean>(false)
const selectedDocumentationOffice = ref<DocumentationOffice>()
const noDocumentationOfficeSelected = ref<boolean>(false)
const documentationOffices = ref<DocumentationOffice[]>()
const currentPage = ref<number>(props.pageEntries?.number ?? 0)

const entries = computed(() => {
  return props.pageEntries?.content || []
})

onMounted(async () => {
  const comboboxItems: ComboboxItem[] | null = (
    await service.getDocumentationOffices(ref(undefined))
  ).data.value

  documentationOffices.value = comboboxItems?.map(
    ({ value }) => value as DocumentationOffice,
  )
})

function openPreview(entry: EURLexResult) {
  if (entry && entry.celex) {
    window.open(entry.htmlLink, "_blank")
  }
}

function updatePage(page: number) {
  emit("updatePage", page)
  currentPage.value = page
}

async function handleAssignToDocOffice() {
  if (selectedEntries.value?.length == 0) {
    noDecisionSelected.value = true
  }

  if (!selectedDocumentationOffice.value) {
    noDocumentationOfficeSelected.value = true
  }

  if (selectedDocumentationOffice.value) {
    noDocumentationOfficeSelected.value = false
    const params: EurlexParameters = {
      documentationOffice: selectedDocumentationOffice.value,
      celexNumbers: selectedEntries.value.map(({ celex }) => celex),
    }

    const response =
      await documentationUnitService.createNewOutOfEurlexDecision(params)
    if (response.status == 201) {
      emit("handleServiceError")
    } else {
      emit("handleServiceError", response.error)
    }

    selectedEntries.value = []

    emit("assign", currentPage.value)
  }
}

function selectRow() {
  noDecisionSelected.value = selectedEntries.value.length == 0
}
</script>

<template>
  <div class="flex flex-col items-start justify-self-end">
    <div class="flex">
      <DocumentationOfficeSelector
        v-model="selectedDocumentationOffice"
        v-model:has-error="noDocumentationOfficeSelected"
        class="min-w-2xs"
      />
      <Button
        aria-label="Dokumentationsstelle zuweisen"
        class="ml-8"
        label="Zuweisen"
        severity="secondary"
        @click="handleAssignToDocOffice"
      ></Button>
    </div>
  </div>
  <div class="m-24">
    <Pagination
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="updatePage"
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
        <Column header="Ort"
          ><template #body=""
            ><span data-testid="court-location">-</span></template
          ></Column
        >
        <Column header="Datum">
          <template #body="{ data }">
            {{ dayjs(data.date).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column field="fileNumber" header="Aktenzeichen"></Column>
        <Column header="Abzugsdatum">
          <template #body="{ data }">
            {{ dayjs(data.publicationDate).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column>
          <template #body="{ data }">
            <Button
              v-if="data.htmlLink"
              aria-label="Öffne Vorschau"
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
