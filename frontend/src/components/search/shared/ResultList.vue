<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref, watch } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import PopupModal from "@/components/PopupModal.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { ResponseError } from "@/services/httpClient"
import IconDelete from "~icons/ic/baseline-close"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconArrowDown from "~icons/mdi/arrow-down-drop"

const props = defineProps<{
  pageEntries?: Page<DocumentUnitListEntry>
  error?: ResponseError
  loading?: boolean
  emptyText?: string
}>()
const emit = defineEmits<{
  updatePage: [number]
  deleteDocumentationUnit: [documentUnitListEntry: DocumentUnitListEntry]
  takeOverDocumentationUnit: [documentUnitListEntry: DocumentUnitListEntry]
}>()

const entries = computed(() => props.pageEntries?.content || [])

const showDeleteModal = ref(false)
const selectedDocumentUnitListEntry = ref<DocumentUnitListEntry>()
const popupModalText = computed(
  () =>
    `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnitListEntry?.value?.documentNumber} wirklich dauerhaft löschen?`,
)
const scrollLock = useScrollLock(document)

/**
 * Clicking on a delete icon of a list entry shows a modal, which asks for user input to proceed
 * @param {DocumentUnitListEntry} documentUnitListEntry - The documentationunit list entry to be deleted
 */
function showDeleteConfirmationDialog(
  documentUnitListEntry: DocumentUnitListEntry,
) {
  selectedDocumentUnitListEntry.value = documentUnitListEntry
  showDeleteModal.value = true
}

/**
 * Propagates delete event to parent and closes modal again
 */
function onDelete() {
  if (selectedDocumentUnitListEntry.value) {
    emit("deleteDocumentationUnit", selectedDocumentUnitListEntry.value)
    showDeleteModal.value = false
  }
}

watch(showDeleteModal, () => (scrollLock.value = showDeleteModal.value))

const emptyText = computed(() => (props.loading ? "" : props.emptyText))
</script>

<template>
  <div data-testId="search-result-list">
    <PopupModal
      v-if="showDeleteModal"
      aria-label="Dokumentationseinheit löschen"
      :content-text="popupModalText"
      header-text="Dokumentationseinheit löschen"
      primary-button-text="Löschen"
      primary-button-type="destructive"
      @close-modal="showDeleteModal = false"
      @primary-action="onDelete"
    />
    <Pagination
      :is-loading="loading"
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <DataTable :loading="loading" :value="entries">
        <Column field="documentNumber" header="Dokumentnummer">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center gap-8">
              <div>{{ item.documentNumber }}</div>
            </div>
          </template>
        </Column>
        <Column field="court.type" header="Gerichtstyp" />
        <Column field="decisionDate">
          <template #header>
            <div class="flex flex-row">
              Mitteilungsdatum
              <IconArrowDown />
            </div>
          </template>

          <template #body="{ data: item }">
            {{
              item.decisionDate
                ? dayjs(item.decisionDate).format("DD.MM.YYYY")
                : "-"
            }}
          </template>
        </Column>
        <Column field="fileNumber" header="Aktenzeichen" />
        <Column header="Status">
          <template #body="{ data: item }">
            <IconBadge
              v-if="item.status?.publicationStatus"
              class="inline-flex"
              v-bind="useStatusBadge(item.status).value"
              data-testid="publication-status"
            />
          </template>
        </Column>
        <Column field="resolutionDate" header="Erledigungsmitteilung">
          <template #body="{ data: item }">
            {{
              item.resolutionDate
                ? dayjs(item.decisionresolutionDateDate).format("DD.MM.YYYY")
                : "-"
            }}
          </template>
        </Column>
        <Column field="actions">
          <template #body="{ data: item }">
            <div class="flex flex-row justify-end -space-x-2">
              <router-link
                target="_blank"
                :to="{
                  name: 'caselaw-pending-proceeding-documentNumber-categories',
                  params: { documentNumber: item.documentNumber },
                }"
              >
                <Button
                  v-tooltip.bottom="{
                    value: 'Bearbeiten',
                    appendTo: 'body',
                  }"
                  aria-label="Dokumentationseinheit bearbeiten"
                  :disabled="!item.isEditable"
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconEdit />
                  </template>
                </Button>
              </router-link>

              <router-link
                target="_blank"
                :to="{
                  name:
                    item.documentType?.jurisShortcut === 'Anh'
                      ? 'caselaw-pending-proceeding-documentNumber-preview'
                      : 'caselaw-documentUnit-documentNumber-preview',
                  params: { documentNumber: item.documentNumber },
                }"
              >
                <Button
                  v-tooltip.bottom="{
                    value: 'Vorschau',
                    appendTo: 'body',
                  }"
                  aria-label="Dokumentationseinheit ansehen"
                  severity="secondary"
                  size="small"
                >
                  <template #icon>
                    <IconView />
                  </template>
                </Button>
              </router-link>

              <Button
                v-tooltip.bottom="{
                  value: 'Löschen',
                  appendTo: 'body',
                }"
                aria-label="Dokumentationseinheit löschen"
                :disabled="!item.isDeletable"
                severity="secondary"
                size="small"
                @click="
                  showDeleteConfirmationDialog(
                    entries.find(
                      (entry) => entry.uuid === item.uuid,
                    ) as DocumentUnitListEntry,
                  )
                "
              >
                <template #icon>
                  <IconDelete />
                </template>
              </Button>
            </div>
          </template>
        </Column>
        <template #empty> {{ emptyText }} </template>
      </DataTable>
    </Pagination>
  </div>
</template>
