<script lang="ts" setup>
import dayjs from "dayjs"
import { ref } from "vue"
import DocumentUnit from "../domain/documentUnit"
import PopupModal from "./PopupModal.vue"

defineProps<{ documentUnits: DocumentUnit[] }>()
const emit = defineEmits<{
  (e: "deleteDocumentUnit", documentUnit: DocumentUnit): void
}>()

const showModal = ref(false)
const popupModalText = ref("")
const modalConfirmText = ref("Löschen")
const modalHeaderText = "Dokumentationseinheit löschen"
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const selectedDocumentUnit = ref(new DocumentUnit("1"))
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
const setSelectedDocumentUnit = (documentUnit: DocumentUnit) => {
  selectedDocumentUnit.value = documentUnit
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnit.value.documentNumber} wirklich dauerhaft löschen?`
  toggleModal()
}
const onDelete = () => {
  emit("deleteDocumentUnit", selectedDocumentUnit.value)
  toggleModal()
}
</script>

<template>
  <div>
    <PopupModal
      v-if="showModal"
      :cancel-button-type="modalCancelButtonType"
      :confirm-button-type="modalConfirmButtonType"
      :confirm-text="modalConfirmText"
      :content-text="popupModalText"
      :header-text="modalHeaderText"
      @close-modal="toggleModal"
      @confirm-action="onDelete"
    />
    <div
      v-if="documentUnits.length"
      class="border-collapse document-unit-list-table table w-full"
    >
      <div
        class="bg-gray-400 font-bold leading-[3] table-row text-18 text-center"
      >
        <div class="table-cell">Dok.-Nummer</div>
        <div class="table-cell">Angelegt am</div>
        <div class="table-cell">Aktenzeichen</div>
        <div class="table-cell">Dokumente</div>
        <div class="table-cell">Löschen</div>
      </div>
      <div
        v-for="documentUnit in documentUnits"
        :key="documentUnit.id"
        class="border-b-2 border-b-gray-100 hover:bg-gray-100 leading-[3] table-row text-18"
      >
        <div class="px-[16px] py-0 table-cell">
          <router-link
            class="underline"
            :to="{
              name: documentUnit.s3path
                ? 'jurisdiction-documentUnit-:documentNumber-categories'
                : 'jurisdiction-documentUnit-:documentNumber-files',
              params: { documentNumber: documentUnit.documentNumber },
            }"
          >
            {{ documentUnit.documentNumber }}
          </router-link>
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{ dayjs(documentUnit.creationtimestamp).format("DD.MM.YYYY") }}
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{
            documentUnit.coreData && documentUnit.coreData.fileNumber
              ? documentUnit.coreData.fileNumber
              : "-"
          }}
        </div>
        <div class="px-16 py-0 table-cell">
          {{ documentUnit.filename ? documentUnit.filename : "-" }}
        </div>
        <div class="table-cell text-center">
          <span
            aria-label="Dokumentationseinheit löschen"
            class="cursor-pointer material-icons"
            tabindex="0"
            @click="setSelectedDocumentUnit(documentUnit)"
            @keyup.enter="setSelectedDocumentUnit(documentUnit)"
          >
            delete
          </span>
        </div>
      </div>
    </div>
    <span v-else>Keine Dokumentationseinheiten gefunden</span>
  </div>
</template>
