<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref, watch } from "vue"
import BulkAssignProcedure from "@/components/BulkAssignProcedure.vue"
import IconBadge from "@/components/IconBadge.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import PopupModal from "@/components/PopupModal.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { InboxType } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { ResponseError } from "@/services/httpClient"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconCheck from "~icons/ic/baseline-check"
import IconDelete from "~icons/ic/baseline-close"
import IconSubject from "~icons/ic/baseline-subject"
import IconNote from "~icons/ic/outline-comment-bank"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconArrowDown from "~icons/mdi/arrow-down-drop"

const props = defineProps<{
  pageEntries?: Page<DocumentUnitListEntry>
  error?: ResponseError
  loading?: boolean
  inboxType: InboxType
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
const attachmentText = (listEntry: DocumentUnitListEntry) =>
  listEntry.hasAttachments ? "Anhang vorhanden" : "Kein Anhang vorhanden"

const headNoteOrPrincipleText = (listEntry: DocumentUnitListEntry) =>
  listEntry.hasHeadnoteOrPrinciple
    ? "Kurztext vorhanden"
    : "Kein Kurztext vorhanden"

const trimText = (text: string, length: number = 50) =>
  text.length > length ? `${text.slice(0, length)}...` : text

const noteTooltip = (listEntry: DocumentUnitListEntry) =>
  listEntry.note ? trimText(listEntry.note) : "Keine Notiz vorhanden"

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

// When changing a page or accepting a Fremdanlage (takeover), the docUnit list is updated.
// We only want to keep docUnits in the selection if the docUnit is still visible.
watch(
  () => props.pageEntries,
  () => {
    const selectedIds = selectedDocumentationUnits.value.map(
      (docUnit) => docUnit.uuid,
    )
    selectedDocumentationUnits.value =
      props.pageEntries?.content.filter((entry) =>
        selectedIds.includes(entry.uuid),
      ) ?? []
  },
  { deep: true },
)

/**
 * Propagates delete event to parent and closes modal again
 */
function onDelete() {
  updateSelectionErrors(undefined, [])
  if (selectedDocumentUnitListEntry.value) {
    emit("deleteDocumentationUnit", selectedDocumentUnitListEntry.value)
    showDeleteModal.value = false
  }
}

/**
 * Propagates takeover event to parent and resets error messages
 */
function onTakeOver(item: DocumentUnitListEntry) {
  updateSelectionErrors(undefined, [])
  emit("takeOverDocumentationUnit", item)
}

const selectedDocumentationUnits = ref<DocumentUnitListEntry[]>([])
const selectionErrorMessage = ref<string | undefined>(undefined)
const selectionErrorDocUnitIds = ref<string[]>([])
function updateSelectionErrors(
  error: string | undefined,
  docUnitIds: string[],
) {
  selectionErrorMessage.value = error
  selectionErrorDocUnitIds.value = docUnitIds
}

function reloadList() {
  selectedDocumentationUnits.value = []
  emit("updatePage", 0)
}

function resetErrorMessages() {
  selectionErrorMessage.value = undefined
  selectionErrorDocUnitIds.value = []
}

watch(showDeleteModal, () => (scrollLock.value = showDeleteModal.value))

const rowStyleClass = (rowData: DocumentUnitListEntry) => {
  return selectionErrorDocUnitIds.value.includes(rowData.uuid!)
    ? "bg-red-200"
    : ""
}

const emptyText = computed(() =>
  props.loading ? "" : "Es liegen keine Dokumentationseinheiten vor.",
)
</script>

<template>
  <div data-testId="inbox-list">
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
    <BulkAssignProcedure
      :documentation-units="selectedDocumentationUnits"
      @procedure-assigned="reloadList"
      @update-selection-errors="updateSelectionErrors"
    />
    <InputErrorMessages
      v-if="selectionErrorMessage"
      class="pl-16"
      :error-message="selectionErrorMessage"
    />
    <Pagination
      :is-loading="loading"
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <DataTable
        v-model:selection="selectedDocumentationUnits"
        :loading="loading"
        :row-class="rowStyleClass"
        :value="entries"
      >
        <Column
          header-style="width: 3rem"
          :pt="{
            pcRowCheckbox: {
              input: {
                style: `${selectionErrorMessage && selectionErrorDocUnitIds.length === 0 ? 'border-color: var(--color-red-800);' : ''}`,
                onClick: () => resetErrorMessages(),
              },
            },
            pcHeaderCheckbox: {
              input: {
                style: `${selectionErrorMessage && selectionErrorDocUnitIds.length === 0 ? 'border-color: var(--color-red-800);' : ''}`,
                onClick: () => resetErrorMessages(),
              },
            },
          }"
          selection-mode="multiple"
        />
        <Column field="documentNumber" header="Dokumentnummer">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center gap-8">
              <div>{{ item.documentNumber }}</div>
              <div class="flex flex-row items-center">
                <Button
                  v-tooltip.bottom="{
                    value: attachmentText(item),
                    appendTo: 'body',
                  }"
                  :aria-label="attachmentText(item)"
                  severity="ghost"
                  size="small"
                >
                  <template #icon>
                    <IconAttachedFile
                      :class="
                        item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                      "
                      data-testid="file-attached-icon"
                    />
                  </template>
                </Button>

                <Button
                  v-tooltip.bottom="{
                    value: headNoteOrPrincipleText(item),
                    appendTo: 'body',
                  }"
                  :aria-label="headNoteOrPrincipleText(item)"
                  severity="ghost"
                  size="small"
                >
                  <template #icon>
                    <IconSubject
                      :aria-label="headNoteOrPrincipleText(item)"
                      :class="
                        item.hasHeadnoteOrPrinciple
                          ? 'text-blue-800'
                          : 'text-gray-500'
                      "
                      data-testid="headnote-principle-icon"
                    />
                  </template>
                </Button>

                <Button
                  v-tooltip.bottom="{
                    value: noteTooltip(item),
                    appendTo: 'body',
                  }"
                  :aria-label="noteTooltip(item)"
                  severity="ghost"
                  size="small"
                >
                  <template #icon>
                    <IconNote
                      :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                      data-testid="note-icon"
                    />
                  </template>
                </Button>
              </div>
            </div>
          </template>
        </Column>
        <Column field="court.type" header="Gerichtstyp" />
        <Column field="court.location" header="Ort" />

        <Column field="decisionDate">
          <template #header>
            <div class="flex flex-row">
              Datum
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
        <Column field="source" header="Quelle">
          <template #body="{ data: item }">
            {{
              item.source
                ? item.source +
                  (item.creatingDocumentationOffice
                    ? " (" +
                      item.creatingDocumentationOffice?.abbreviation +
                      ")"
                    : "")
                : ""
            }}
          </template>
        </Column>
        <Column field="createdAt" header="Angelegt am">
          <template #body="{ data: item }">
            {{
              item.createdAt ? dayjs(item.createdAt).format("DD.MM.YYYY") : "-"
            }}
          </template>
        </Column>
        <Column v-if="inboxType != InboxType.EU" header="Status">
          <template #body="{ data: item }">
            <IconBadge
              v-if="item.status?.publicationStatus"
              class="inline-flex"
              v-bind="useStatusBadge(item.status).value"
              data-testid="publication-status"
            />
          </template>
        </Column>
        <Column field="actions">
          <template #body="{ data: item }">
            <div class="flex flex-row justify-end -space-x-2">
              <Button
                v-if="
                  item.status?.publicationStatus == 'EXTERNAL_HANDOVER_PENDING'
                "
                v-tooltip.bottom="{
                  value: 'Übernehmen',
                  appendTo: 'body',
                }"
                aria-label="Dokumentationseinheit übernehmen"
                :disabled="!item.isEditable"
                severity="secondary"
                size="small"
                @click="onTakeOver(item)"
              >
                <template #icon>
                  <IconCheck />
                </template>
              </Button>

              <router-link
                v-else
                target="_blank"
                :to="{
                  name: 'caselaw-documentUnit-documentNumber-categories',
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
