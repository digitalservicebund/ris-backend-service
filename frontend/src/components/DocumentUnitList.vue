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

const props = defineProps<{
  documentUnitListEntries?: DocumentUnitListEntry[]
  searchResponseError?: ResponseError
  isLoading?: boolean
  isDeletable?: boolean
}>()

const emit = defineEmits<{
  deleteDocumentUnit: [documentUnitListEntry: DocumentUnitListEntry]
}>()

const emptyStatus = computed(() => {
  if (!props.documentUnitListEntries) {
    return "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit."
  } else if (props.documentUnitListEntries.length === 0) {
    return "Keine Ergebnisse gefunden."
  }
  return undefined
})

const showModal = ref(false)
const popupModalText = ref("")
const modalConfirmText = ref("Löschen")
const modalHeaderText = "Dokumentationseinheit löschen"
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const selectedDocumentUnitListEntry = ref<DocumentUnitListEntry>()
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
function setSelectedDocumentUnitListEntry(
  documentUnitListEntry: DocumentUnitListEntry,
) {
  selectedDocumentUnitListEntry.value = documentUnitListEntry
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnitListEntry.value.documentNumber} wirklich dauerhaft löschen?`
  toggleModal()
}

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
          Typ
        </div>
        <div
          class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
        >
          Anhang
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
        v-for="(listEntry, id) in documentUnitListEntries"
        :key="id"
        class="ds-label-01-reg table-row hover:bg-gray-100"
        data-testid="listEntry"
      >
        <div
          class="table-cell min-h-56 border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          <router-link
            class="underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            :to="{
              name: listEntry.fileName
                ? 'caselaw-documentUnit-documentNumber-categories'
                : 'caselaw-documentUnit-documentNumber-files',
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
          {{
            listEntry.documentType ? listEntry.documentType.jurisShortcut : "-"
          }}
        </div>
        <div class="table-cell border-b-1 border-blue-300 px-12 align-middle">
          <span v-if="listEntry.fileName" class="text-blue-800">
            <IconAttachedFile />
          </span>
          <span v-else class="text-gray-500"><IconAttachedFile /></span>
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
