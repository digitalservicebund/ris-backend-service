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
import Tooltip from "@/components/Tooltip.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconCheck from "~icons/ic/baseline-check"
import IconDelete from "~icons/ic/baseline-close"
import IconSubject from "~icons/ic/baseline-subject"
import IconNote from "~icons/ic/outline-comment-bank"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconClock from "~icons/ic/outline-watch-later"

const props = defineProps<{
  pageEntries?: Page<DocumentUnitListEntry>
}>()

const emit = defineEmits<{
  updatePage: [number]
  deleteDocumentationUnit: [documentUnitListEntry: DocumentUnitListEntry]
  takeOverDocumentationUnit: [documentUnitListEntry: DocumentUnitListEntry]
}>()

const entries = computed(() => {
  return props.pageEntries?.content || []
})

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
  // On takeover, we mutate the page content -> needs deep watching
  { deep: 2 },
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
    ? "bg-red-300"
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
      :error-message="selectionErrorMessage"
    />
    <Pagination
      navigation-position="bottom"
      :page="pageEntries"
      @update-page="emit('updatePage', $event)"
    >
      <DataTable
        v-model:selection="selectedDocumentationUnits"
        class="ris-label2-bold text-gray-900"
        :pt="{
          thead: {
            style: 'box-shadow: inset 0 -2px #DCE8EF;',
          },
        }"
        :row-class="rowStyleClass"
        :value="entries"
      >
        <Column
          :pt="{
            pcRowCheckbox: {
              input: {
                style:
                  selectionErrorMessage && selectionErrorDocUnitIds.length === 0
                    ? 'border-color: var(--color-red-800)'
                    : '',
              },
            },
            pcHeaderCheckbox: {
              input: {
                style:
                  selectionErrorMessage && selectionErrorDocUnitIds.length === 0
                    ? 'border-color: var(--color-red-800)'
                    : '',
              },
            },
          }"
          selection-mode="multiple"
        >
        </Column>
        <Column field="documentNumber" header="Dokumentnummer">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center space-x-8">
              <div>{{ item.documentNumber }}</div>

              <Tooltip text="Tooltip Text">
                <IconAttachedFile
                  class="flex-end h-20 w-20"
                  :class="
                    item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                  "
                  data-testid="file-attached-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconSubject
                  class="flex-end flex h-20 w-20"
                  :class="
                    item.hasHeadnoteOrPrinciple
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="headnote-principle-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconNote
                  class="flex-end flex h-20 w-20"
                  :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                  data-testid="note-icon"
                />
              </Tooltip>

              <Tooltip text="Tooltip Text">
                <IconClock
                  class="flex-end flex h-20 w-20"
                  :class="
                    item.scheduledPublicationDateTime
                      ? 'text-blue-800'
                      : 'text-gray-500'
                  "
                  data-testid="scheduling-icon"
                />
              </Tooltip>
            </div> </template
        ></Column>
        <Column field="court.type" header="Gerichtstyp"></Column>
        <Column field="court.location" header="Ort"></Column>

        <Column field="decisionDate" header="Datum">
          <template #body="{ data: item }"
            >{{
              item.decisionDate
                ? dayjs(item.decisionDate).format("DD.MM.YYYY")
                : "-"
            }}
          </template>
        </Column>
        <Column field="fileNumber" header="Aktenzeichen"></Column>
        <Column field="source" header="Quelle">
          <template #body="{ data: item }">
            {{
              item.source
                ? item.source +
                  " (" +
                  item.creatingDocumentationOffice?.abbreviation +
                  ")"
                : ""
            }}</template
          >
        </Column>
        <Column field="createdAt" header="Angelegt am">
          <template #body="{ data: item }"
            >{{
              item.createdAt ? dayjs(item.createdAt).format("DD.MM.YYYY") : "-"
            }}
          </template></Column
        >
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
                      entries?.find(
                        (entry) => entry.uuid == item.uuid,
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
      </DataTable>
    </Pagination>
  </div>
</template>
