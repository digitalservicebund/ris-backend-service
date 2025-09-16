<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import dayjsTimezone from "dayjs/plugin/timezone"
import dayjsUtc from "dayjs/plugin/utc"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref, onMounted, onUnmounted } from "vue"
import AssigneeBadge from "@/components/AssigneeBadge.vue"
import BulkAssignProcessStep from "@/components/BulkAssignProcessStep.vue"
import CurrentAndPreviousProcessStepBadge from "@/components/CurrentAndPreviousProcessStepBadge.vue"
import IconBadge from "@/components/IconBadge.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import featureToggleService from "@/services/featureToggleService"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
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

// --- START: sticky header logic ---
const tableWrapper = ref<HTMLElement | null>(null)
const isSticky = ref(false)

const handleScroll = () => {
  if (tableWrapper.value) {
    const top = tableWrapper.value.getBoundingClientRect().top
    isSticky.value = top <= 0
  }
}

const stickyHeaderPT = computed(() => {
  return isSticky.value
    ? {
        thead: {
          class:
            "sticky top-0 bg-white shadow-[0_1px_0_var(--color-blue-300)] z-999",
        },
        tablecontainer: {
          style: {
            overflow: "visible !important",
          },
        },
      }
    : {}
})
// --- END: sticky header logic ---

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
    listEntry.scheduledPublicationDateTime ?? listEntry.lastHandoverDateTime
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

defineSlots<{
  "empty-state-content"?: (props: Record<string, never>) => unknown
}>()

const multiEditActive = ref()

// multi edit
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

const rowStyleClass = (rowData: DocumentUnitListEntry) => {
  if (selectionErrorDocUnitIds.value.includes(rowData.uuid!)) {
    return "bg-red-200"
  }

  return ""
}

onMounted(async () => {
  window.addEventListener("scroll", handleScroll)
  multiEditActive.value = (
    await featureToggleService.isEnabled("neuris.multi-edit")
  ).data
})

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
})
</script>

<template>
  <div ref="tableWrapper" data-testId="search-result-list">
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
        :pt="stickyHeaderPT"
        :row-class="rowStyleClass"
        :value="entries"
      >
        <Column
          v-if="multiEditActive"
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
              <IconError v-if="item.status?.withError" class="text-red-900" />
              <div class="min-w-[130px]">{{ item.documentNumber }}</div>
              <template v-if="isDecision">
                <span
                  v-tooltip.bottom="{
                    value: attachmentText(item),
                    appendTo: 'body',
                  }"
                >
                  <IconAttachedFile
                    :aria-label="attachmentText(item)"
                    class="flex-end h-20 w-20"
                    :class="
                      item.hasAttachments ? 'text-blue-800' : 'text-gray-500'
                    "
                    data-testid="file-attached-icon"
                  />
                </span>
                <span
                  v-tooltip.bottom="{
                    value: headNoteOrPrincipleText(item),
                    appendTo: 'body',
                  }"
                >
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
                </span>
                <span
                  v-tooltip.bottom="{
                    value: noteTooltip(item),
                    appendTo: 'body',
                  }"
                >
                  <IconNote
                    :aria-label="noteTooltip(item)"
                    class="flex-end flex h-20 w-20"
                    :class="!!item.note ? 'text-blue-800' : 'text-gray-500'"
                    data-testid="note-icon"
                  />
                </span>

                <span
                  v-tooltip.bottom="{
                    value: schedulingTooltip(item.scheduledPublicationDateTime),
                    appendTo: 'body',
                  }"
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
                </span>
              </template>
            </div>
          </template>
        </Column>

        <Column field="court" header="Gericht">
          <template #body="{ data: item }">
            {{
              [item.court?.type, item.court?.location]
                .filter(Boolean)
                .join(" ") || "-"
            }}
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
              class="inline-flex whitespace-nowrap"
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

        <Column v-if="isDecision" header="Schritt">
          <template #body="{ data: item }">
            <CurrentAndPreviousProcessStepBadge
              :current-process-step="
                item.currentDocumentationUnitProcessStep?.processStep
              "
              :previous-process-step="item.previousProcessStep"
            />
          </template>
        </Column>

        <Column v-if="isDecision" header="Person">
          <template #body="{ data: item }">
            <AssigneeBadge
              :name="item?.currentDocumentationUnitProcessStep?.user?.initials"
            />
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
            <span v-if="!multiEditActive" class="sr-only">Aktionen</span>
            <BulkAssignProcessStep
              v-else
              :documentation-units="selectedDocumentationUnits"
              @procedure-assigned="reloadList"
              @update-selection-errors="updateSelectionErrors"
            ></BulkAssignProcessStep>
          </template>
          <template #body="{ data: item }">
            <div class="flex flex-row justify-end -space-x-2">
              <router-link
                tabindex="-1"
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
                tabindex="-1"
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
