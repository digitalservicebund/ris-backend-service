<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref, watch } from "vue"
import BulkAssignProcedure from "@/components/BulkAssignProcedure.vue"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import PopupModal from "@/components/PopupModal.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
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
  if (selectedDocumentUnitListEntry.value) {
    emit("deleteDocumentationUnit", selectedDocumentUnitListEntry.value)
    showDeleteModal.value = false
  }
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

watch(showDeleteModal, () => (scrollLock.value = showDeleteModal.value))

const rowStyleClass = (rowData: DocumentUnitListEntry) => {
  return selectionErrorDocUnitIds.value.includes(rowData.uuid!)
    ? "bg-red-200"
    : ""
}
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
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <DataTable
        v-model:selection="selectedDocumentationUnits"
        class="text-gray-900"
        :pt="{
          thead: {
            style: 'box-shadow: inset 0 -2px #DCE8EF;',
          },
          tablecontainer: {
            style: 'overflow: visible;',
          },
        }"
        :row-class="rowStyleClass"
        :value="entries"
      >
        <Column
          header-style="width: 3rem"
          :pt="{
            pcRowCheckbox: {
              root: {
                style:
                  'height: 100%; display: flex; align-items: center; justify-content: center;',
              },
              input: {
                style: `height: 1.5rem; width: 1.5rem; ${selectionErrorMessage && selectionErrorDocUnitIds.length === 0 ? 'border-color: var(--color-red-800);' : ''}`,
              },
            },
            pcHeaderCheckbox: {
              root: {
                style:
                  'display: flex; align-items: center; justify-content: center;',
              },
              input: {
                style: `height: 1.5rem; width: 1.5rem; ${selectionErrorMessage && selectionErrorDocUnitIds.length === 0 ? 'border-color: var(--color-red-800);' : ''}`,
              },
            },
          }"
          selection-mode="multiple"
        />
        <Column
          field="documentNumber"
          header="Dokumentnummer"
          header-class="ris-label3-bold"
        >
          <template #body="{ data: item }">
            <div class="flex flex-row items-center gap-8">
              <div>{{ item.documentNumber }}</div>
              <div class="flex flex-row items-center">
                <Tooltip :text="attachmentText(item)">
                  <IconAttachedFile
                    :aria-label="attachmentText(item)"
                    class="flex-end m-4 h-20 w-20"
                    :class="
                      item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                    "
                    data-testid="file-attached-icon"
                  />
                </Tooltip>

                <Tooltip :text="headNoteOrPrincipleText(item)">
                  <IconSubject
                    :aria-label="headNoteOrPrincipleText(item)"
                    class="flex-end m-4 flex h-20 w-20"
                    :class="
                      item.hasHeadnoteOrPrinciple
                        ? 'text-blue-800'
                        : 'text-gray-500'
                    "
                    data-testid="headnote-principle-icon"
                  />
                </Tooltip>

                <Tooltip :text="noteTooltip(item)">
                  <IconNote
                    :aria-label="noteTooltip(item)"
                    class="flex-end m-4 flex h-20 w-20"
                    :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                    data-testid="note-icon"
                  />
                </Tooltip>
              </div>
            </div>
          </template>
        </Column>
        <Column
          field="court.type"
          header="Gerichtstyp"
          header-class="ris-label3-bold"
        />
        <Column
          field="court.location"
          header="Ort"
          header-class="ris-label3-bold"
        />

        <Column field="decisionDate" header-class="ris-label3-bold">
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
        <Column
          field="fileNumber"
          header="Aktenzeichen"
          header-class="ris-label3-bold"
        />
        <Column field="source" header="Quelle" header-class="ris-label3-bold">
          <template #body="{ data: item }">
            {{
              item.source
                ? item.source +
                  " (" +
                  item.creatingDocumentationOffice?.abbreviation +
                  ")"
                : ""
            }}
          </template>
        </Column>
        <Column
          field="createdAt"
          header="Angelegt am"
          header-class="ris-label3-bold"
        >
          <template #body="{ data: item }">
            {{
              item.createdAt ? dayjs(item.createdAt).format("DD.MM.YYYY") : "-"
            }}
          </template>
        </Column>
        <Column header="Status" header-class="ris-label3-bold">
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
              <Tooltip
                v-if="
                  item.status?.publicationStatus == 'EXTERNAL_HANDOVER_PENDING'
                "
                text="Übernehmen"
              >
                <Button
                  aria-label="Dokumentationseinheit übernehmen"
                  class="z-20"
                  :disabled="!item.isEditable"
                  severity="secondary"
                  size="small"
                  @click="emit('takeOverDocumentationUnit', item)"
                >
                  <template #icon>
                    <IconCheck />
                  </template>
                </Button>
              </Tooltip>
              <Tooltip v-else text="Bearbeiten">
                <router-link
                  target="_blank"
                  :to="{
                    name: 'caselaw-documentUnit-documentNumber-categories',
                    params: { documentNumber: item.documentNumber },
                  }"
                >
                  <Button
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
              </Tooltip>
              <Tooltip text="Vorschau">
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
                    aria-label="Dokumentationseinheit ansehen"
                    class="z-20"
                    severity="secondary"
                    size="small"
                  >
                    <template #icon>
                      <IconView />
                    </template>
                  </Button>
                </router-link>
              </Tooltip>
              <Tooltip text="Löschen">
                <Button
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
              </Tooltip>
            </div>
          </template>
        </Column>
        <template v-if="!error" #empty>
          <div class="mt-40 grid justify-items-center bg-white">
            Es liegen keine Dokumentationseinheiten vor.
          </div>
        </template>
      </DataTable>
      <!-- Error State -->
      <div v-if="error">
        <InfoModal :description="error.description" :title="error.title" />
      </div>
    </Pagination>
  </div>
</template>
