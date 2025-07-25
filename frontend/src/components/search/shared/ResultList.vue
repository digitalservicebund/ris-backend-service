<script lang="ts" setup>
import { useScrollLock } from "@vueuse/core"
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import dayjsTimezone from "dayjs/plugin/timezone"
import dayjsUtc from "dayjs/plugin/utc"

import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref, watch } from "vue"

import IconBadge from "@/components/IconBadge.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import PopupModal from "@/components/PopupModal.vue"
import Tooltip from "@/components/Tooltip.vue"

import { useStatusBadge } from "@/composables/useStatusBadge"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"

import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconDelete from "~icons/ic/baseline-close"
import IconError from "~icons/ic/baseline-error"
import IconSubject from "~icons/ic/baseline-subject"
import IconNote from "~icons/ic/outline-comment-bank"
import IconEdit from "~icons/ic/outline-edit"
import IconView from "~icons/ic/outline-remove-red-eye"
import IconClock from "~icons/ic/outline-watch-later"
import IconArrowDown from "~icons/mdi/arrow-down-drop"

const props = defineProps<{
  kind: Kind
  pageEntries?: Page<DocumentUnitListEntry>
  loading?: boolean
  showPublicationDate?: boolean
}>()
const emit = defineEmits<{
  updatePage: [number]
  deleteDocumentationUnit: [documentUnitListEntry: DocumentUnitListEntry]
}>()
// Extend Day.js with necessary plugins for proper date handling and timezones
dayjs.extend(dayjsUtc)
dayjs.extend(dayjsTimezone)
dayjs.extend(customParseFormat)

const isPendingProceeding = computed(
  () => props.kind === Kind.PENDING_PROCEEDING,
)
const isDecision = computed(() => props.kind === Kind.DECISION)

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
  text && text.length > length ? `${text.slice(0, length)}...` : text

const noteTooltip = (listEntry: DocumentUnitListEntry) =>
  listEntry.note ? trimText(listEntry.note) : "Keine Notiz vorhanden"

const schedulingTooltip = (publicationDate?: string) =>
  publicationDate
    ? `Terminierte Übergabe am\n${dayjs.utc(publicationDate).tz("Europe/Berlin").format("DD.MM.YYYY HH:mm")}`
    : "Keine Übergabe terminiert"

const publicationDate = (listEntry: DocumentUnitListEntry) => {
  const date =
    listEntry.scheduledPublicationDateTime ?? listEntry.lastPublicationDateTime
  if (date) {
    return dayjs.utc(date).tz("Europe/Berlin").format("DD.MM.YYYY HH:mm")
  } else {
    return "-"
  }
}

/**
 * Returns the correct router link, depending on docunit kind and suffix
 * @param {DocumentUnitListEntry} item - The documentationunit list entry to determine the kind
 * @param {"categories" | "preview"} suffix - The variable part of the router link
 */
const getRouterLinkTo = (
  item: DocumentUnitListEntry,
  suffix: "categories" | "preview",
) => {
  return {
    name:
      item.documentType?.jurisShortcut === "Anh"
        ? `caselaw-pending-proceeding-documentNumber-${suffix}`
        : `caselaw-documentUnit-documentNumber-${suffix}`,
    params: {
      documentNumber: item.documentNumber ?? "undefined",
    },
  }
}

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

defineSlots<{
  "empty-state-content"?: (props: Record<string, never>) => unknown
}>()
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
              <template v-if="isDecision">
                <Tooltip :text="attachmentText(item)">
                  <IconAttachedFile
                    :aria-label="attachmentText(item)"
                    class="flex-end h-20 w-20"
                    :class="
                      item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                    "
                    data-testid="file-attached-icon"
                  />
                </Tooltip>

                <Tooltip :text="headNoteOrPrincipleText(item)">
                  <IconSubject
                    :aria-label="headNoteOrPrincipleText(item)"
                    class="flex-end flex h-20 w-20"
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
                    class="flex-end flex h-20 w-20"
                    :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                    data-testid="note-icon"
                  />
                </Tooltip>

                <Tooltip
                  :text="schedulingTooltip(item.scheduledPublicationDateTime)"
                >
                  <IconClock
                    :aria-label="
                      schedulingTooltip(item.scheduledPublicationDateTime)
                    "
                    class="flex-end flex h-20 w-20"
                    :class="
                      item.scheduledPublicationDateTime
                        ? 'text-blue-800'
                        : 'text-gray-500'
                    "
                    data-testid="scheduling-icon"
                  />
                </Tooltip>
              </template>
            </div>
          </template>
        </Column>

        <Column field="court.type" header="Gerichtstyp">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center gap-8">
              <div>{{ item.court?.type ?? "-" }}</div>
            </div>
          </template>
        </Column>

        <Column v-if="isDecision" field="court.location" header="Ort">
          <template #body="{ data: item }">
            {{ item.court?.location ?? "-" }}
          </template>
        </Column>

        <Column field="decisionDate">
          <template #header>
            <div class="flex flex-row">
              {{ isDecision ? "Datum" : "Mitteilungsdatum" }}
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

        <Column field="fileNumber" header="Aktenzeichen">
          <template #body="{ data: item }">
            <div class="flex flex-row items-center gap-8">
              <div>{{ item.fileNumber ?? "-" }}</div>
            </div>
          </template>
        </Column>

        <Column v-if="isDecision" field="appraisalBody" header="Spruchkörper">
          <template #body="{ data: item }">
            {{ item.appraisalBody ?? "-" }}
          </template>
        </Column>

        <Column
          v-if="isDecision"
          field="documentType.jurisShortcut"
          header="Typ"
        >
          <template #body="{ data: item }">
            {{ item.documentType ? item.documentType.jurisShortcut : "-" }}
          </template>
        </Column>

        <Column header="Status">
          <template #body="{ data: item }">
            <IconBadge
              v-if="item.status?.publicationStatus"
              class="inline-flex"
              v-bind="useStatusBadge(item.status).value"
              data-testid="publication-status"
            />
            <span
              v-if="
                item.status?.publicationStatus ===
                  PublicationState.EXTERNAL_HANDOVER_PENDING && isDecision
              "
              class="ris-body2-regular"
            >
              {{
                " aus " +
                item.source +
                " (" +
                item.creatingDocumentationOffice?.abbreviation +
                ")"
              }}
            </span>
          </template>
        </Column>

        <Column header="Fehler">
          <template #body="{ data: item }">
            <IconBadge
              v-if="item.status?.withError"
              background-color="bg-red-300"
              class="inline-flex"
              color="text-red-900"
              data-testid="publication-error"
              :icon="IconError"
              label="Fehler"
            />
            <span v-else>-</span>
          </template>
        </Column>

        <Column
          v-if="isPendingProceeding"
          field="resolutionDate"
          header="Erledigungsmitteilung"
        >
          <template #body="{ data: item }">
            {{
              item.resolutionDate
                ? dayjs(item.resolutionDate).format("DD.MM.YYYY")
                : "-"
            }}
          </template>
        </Column>

        <Column v-if="isDecision && showPublicationDate" header="jDV Übergabe">
          <template #body="{ data: item }">
            {{ publicationDate(item) }}
          </template>
        </Column>

        <Column field="actions">
          <template #header>
            <span class="sr-only">Aktionen</span>
          </template>
          <template #body="{ data: item }">
            <div class="flex flex-row justify-end -space-x-2">
              <router-link
                target="_blank"
                :to="getRouterLinkTo(item, 'categories')"
              >
                <Button
                  v-tooltip.bottom="{
                    value: 'Bearbeiten',
                    appendTo: 'body',
                  }"
                  aria-label="Dokumentationseinheit bearbeiten"
                  :disabled="
                    !item.isEditable ||
                    item.status?.publicationStatus ==
                      PublicationState.EXTERNAL_HANDOVER_PENDING
                  "
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
                :to="getRouterLinkTo(item, 'preview')"
              >
                <Button
                  v-tooltip.bottom="{
                    value: 'Vorschau',
                    appendTo: 'body',
                  }"
                  aria-label="Dokumentationseinheit ansehen"
                  class="z-10"
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
                :disabled="
                  !item.isDeletable ||
                  item.status?.publicationStatus ==
                    PublicationState.EXTERNAL_HANDOVER_PENDING
                "
                severity="secondary"
                size="small"
                @click="showDeleteConfirmationDialog(item)"
              >
                <template #icon>
                  <IconDelete />
                </template>
              </Button>
            </div>
          </template>
        </Column>

        <template #empty>
          <div class="mt-40 grid justify-items-center bg-white">
            <slot name="empty-state-content"></slot>
          </div>
        </template>
      </DataTable>
    </Pagination>
  </div>
</template>
