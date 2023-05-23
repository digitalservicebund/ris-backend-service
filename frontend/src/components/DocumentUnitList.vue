<script lang="ts" setup>
import dayjs from "dayjs"
import { ref } from "vue"
import { DocumentUnitListEntry } from "../domain/documentUnitListEntry"
import PopupModal from "@/shared/components/PopupModal.vue"

defineProps<{ documentUnitListEntries: DocumentUnitListEntry[] }>()
const emit = defineEmits<{
  (e: "deleteDocumentUnit", documentUnitListEntry: DocumentUnitListEntry): void
}>()

const showModal = ref(false)
const popupModalText = ref("")
const modalConfirmText = ref("Löschen")
const modalHeaderText = "Dokumentationseinheit löschen"
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const selectedDocumentUnitListEntry = ref<DocumentUnitListEntry>()
const toggleModal = () => {
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
const setSelectedDocumentUnitListEntry = (
  documentUnitListEntry: DocumentUnitListEntry
) => {
  selectedDocumentUnitListEntry.value = documentUnitListEntry
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnitListEntry.value.documentNumber} wirklich dauerhaft löschen?`
  toggleModal()
}
const onDelete = () => {
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
    <div
      v-if="documentUnitListEntries.length"
      class="border-collapse document-unit-list-table table w-full"
    >
      <div
        class="bg-gray-400 font-bold leading-[3] table-row text-18 text-center"
      >
        <div class="table-cell">Dokumentnummer</div>
        <div class="table-cell">Angelegt am</div>
        <div class="table-cell">Aktenzeichen</div>
        <div class="table-cell">Dokumente</div>
        <div class="table-cell">Löschen</div>
      </div>
      <div
        v-for="documentUnitListEntry in documentUnitListEntries"
        :key="documentUnitListEntry.id"
        class="border-b-2 border-b-gray-100 hover:bg-gray-100 leading-[3] table-row text-18"
      >
        <div class="px-[16px] py-0 table-cell">
          <router-link
            class="underline"
            :to="{
              name: documentUnitListEntry.fileName
                ? 'caselaw-documentUnit-:documentNumber-categories'
                : 'caselaw-documentUnit-:documentNumber-files',
              params: { documentNumber: documentUnitListEntry.documentNumber },
            }"
          >
            {{ documentUnitListEntry.documentNumber }}
          </router-link>
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{
            dayjs(documentUnitListEntry.creationTimestamp).format("DD.MM.YYYY")
          }}
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{
            documentUnitListEntry && documentUnitListEntry.fileNumber
              ? documentUnitListEntry.fileNumber
              : "-"
          }}
        </div>
        <div class="px-16 py-0 table-cell">
          {{
            documentUnitListEntry.fileName
              ? documentUnitListEntry.fileName
              : "-"
          }}
        </div>
        <div class="table-cell text-center">
          <span
            aria-label="Dokumentationseinheit löschen"
            class="cursor-pointer material-icons"
            tabindex="0"
            @click="setSelectedDocumentUnitListEntry(documentUnitListEntry)"
            @keyup.enter="
              setSelectedDocumentUnitListEntry(documentUnitListEntry)
            "
          >
            delete
          </span>
        </div>
      </div>
    </div>
    <span v-else>Keine Dokumentationseinheiten gefunden</span>
  </div>
</template>
