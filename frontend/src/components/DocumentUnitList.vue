<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref } from "vue"
import DocumentUnitListEntry from "../domain/documentUnitListEntry"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import PopupModal from "@/components/PopupModal.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { ResponseError } from "@/services/httpClient"
import IconAttachedFile from "~icons/ic/baseline-attach-file"
import IconDelete from "~icons/ic/baseline-close"
import IconError from "~icons/ic/baseline-error"
import IconSubject from "~icons/ic/baseline-subject"

const props = defineProps<{
  documentUnitListEntries?: DocumentUnitListEntry[]
  searchResponseError?: ResponseError
  isLoading?: boolean
  isDeletable?: boolean
  emptyState?: string
}>()

const emit = defineEmits<{
  deleteDocumentUnit: [documentUnitListEntry: DocumentUnitListEntry]
}>()

const emptyStatus = computed(() => props.emptyState)

const listEntries = computed(() => {
  return props.documentUnitListEntries && !props.isLoading
    ? props.documentUnitListEntries
    : []
})

const showModal = ref(false)
const popupModalText = ref("")
const modalConfirmText = ref("Löschen")
const modalHeaderText = "Dokumentationseinheit löschen"
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const selectedDocumentUnitListEntry = ref<DocumentUnitListEntry>()

/**
 * Stops propagation of scrolling event, and toggles the showModal value
 */
function toggleModal() {
  showModal.value = !showModal.value
  if (showModal.value) {
    const scrollLeft = document.documentElement.scrollLeft
    const scrollTop = document.documentElement.scrollTop
    window.onscroll = () => {
      window.scrollTo(scrollLeft, scrollTop)
    }
  } else {
    window.onscroll = () => {
      return
    }
  }
}

/**
 * Clicking on a delete icon of a list entry triggers toggleModal() and asks for user input to proceed
 * @param {DocumentUnitListEntry} documentUnitListEntry - The documentationunit list entry to be deleted
 */
function setSelectedDocumentUnitListEntry(
  documentUnitListEntry: DocumentUnitListEntry,
) {
  selectedDocumentUnitListEntry.value = documentUnitListEntry
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnitListEntry.value.documentNumber} wirklich dauerhaft löschen?`
  toggleModal()
}

/**
 * Propagates delete event to parent and closes modal again
 */
function onDelete() {
  if (selectedDocumentUnitListEntry.value) {
    emit("deleteDocumentUnit", selectedDocumentUnitListEntry.value)
    toggleModal()
  }
}
</script>

<template>
  <div>
    <PopupModal
      v-if="showModal"
      :aria-label="modalHeaderText"
      :cancel-button-type="modalCancelButtonType"
      :confirm-button-type="modalConfirmButtonType"
      :confirm-text="modalConfirmText"
      :content-text="popupModalText"
      :header-text="modalHeaderText"
      @close-modal="toggleModal"
      @confirm-action="onDelete"
    />
    <div class="relative table w-full border-separate">
      <div
        class="ds-label-02-bold sticky top-0 table-row bg-white text-gray-900"
      >
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Dokumentnummer
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Gerichtstyp
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Ort
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Datum
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Aktenzeichen
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Spruchkörper
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Typ
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Inhalte
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Status
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Fehler
        </div>
        <div
          v-if="isDeletable"
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        ></div>
      </div>
      <div
        v-for="(listEntry, id) in listEntries"
        :key="id"
        class="ds-label-01-reg table-row hover:bg-blue-100"
        data-testid="listEntry"
      >
        <div
          class="table-cell min-h-56 border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          <router-link
            class="underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            :to="{
              name: 'caselaw-documentUnit-documentNumber-categories',
              params: { documentNumber: listEntry.documentNumber },
            }"
          >
            {{ listEntry.documentNumber }}
          </router-link>
        </div>

        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{ listEntry.court?.type ?? "-" }}
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{ listEntry.court?.location ?? "-" }}
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{
            listEntry.decisionDate
              ? dayjs(listEntry.decisionDate).format("DD.MM.YYYY")
              : "-"
          }}
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{ listEntry.fileNumber ? listEntry.fileNumber : "-" }}
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{ listEntry.appraisalBody ?? "-" }}
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{
            listEntry.documentType ? listEntry.documentType.jurisShortcut : "-"
          }}
        </div>
        <div class="table-cell border-b-1 border-blue-300 px-12 align-middle">
          <div class="flex flex-row">
            <span
              v-if="listEntry.hasAttachments"
              class="text-blue-800"
              data-testid="file-attached-icon"
            >
              <IconAttachedFile />
            </span>
            <span v-else class="text-gray-500"><IconAttachedFile /></span>
            <span
              v-if="listEntry.hasHeadnoteOrPrinciple"
              class="text-blue-800"
              data-testid="headnote-principle-icon"
            >
              <IconSubject />
            </span>
            <span v-else class="text-gray-500"><IconSubject /></span>
          </div>
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          <IconBadge
            v-if="listEntry.status?.publicationStatus"
            v-bind="useStatusBadge(listEntry.status).value"
          />
        </div>
        <div
          class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          <IconBadge
            v-if="listEntry.status?.withError"
            background-color="bg-red-300"
            color="text-red-900"
            :icon="IconError"
            label="Fehler"
          />
          <span v-else>-</span>
        </div>
        <div
          v-if="isDeletable"
          class="table-cell border-b-1 border-blue-300 px-12 align-middle"
        >
          <button
            aria-label="Dokumentationseinheit löschen"
            class="cursor-pointer align-middle text-blue-800 focus:outline-none focus-visible:outline-blue-800"
            @click="
              setSelectedDocumentUnitListEntry(
                documentUnitListEntries?.find(
                  (entry) => entry.uuid == listEntry.uuid,
                ) as DocumentUnitListEntry,
              )
            "
            @keyup.enter="
              setSelectedDocumentUnitListEntry(
                documentUnitListEntries?.find(
                  (entry) => entry.uuid == listEntry.uuid,
                ) as DocumentUnitListEntry,
              )
            "
          >
            <IconDelete />
          </button>
        </div>
      </div>
    </div>
    <!-- Loading State -->
    <div
      v-if="isLoading"
      class="my-112 grid justify-items-center bg-white bg-opacity-60"
    >
      <LoadingSpinner />
    </div>
    <!-- Error State -->
    <div v-if="searchResponseError" class="mt-24">
      <InfoModal
        :description="searchResponseError.description"
        :title="searchResponseError.title"
      />
    </div>

    <!-- Empty State -->
    <div
      v-if="emptyStatus && !searchResponseError && !isLoading"
      class="my-112 grid justify-items-center"
    >
      <span class="mb-16">{{ emptyStatus }}</span>
      <slot name="newlink" />
    </div>
  </div>
</template>
