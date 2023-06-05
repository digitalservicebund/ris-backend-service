<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref } from "vue"
import { DocumentUnitListEntry } from "../domain/documentUnitListEntry"
import { useStatusBadge } from "@/composables/useStatusBadge"
import IconBadge from "@/shared/components/IconBadge.vue"
import PopupModal from "@/shared/components/PopupModal.vue"

const props = defineProps<{
  documentUnitListEntries: DocumentUnitListEntry[]
}>()
const emit = defineEmits<{
  (e: "deleteDocumentUnit", documentUnitListEntry: DocumentUnitListEntry): void
}>()

const listEntriesWithStatus = computed(() =>
  props.documentUnitListEntries.map((entry) => ({
    ...entry,
    status: useStatusBadge(entry.status).value,
  }))
)

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
  documentUnitListEntry: DocumentUnitListEntry
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
        <div class="table-cell">DokStelle</div>
        <div class="table-cell">Status</div>
        <div class="table-cell">Dokumente</div>
        <div class="table-cell">Löschen</div>
      </div>
      <div
        v-for="listEntry in listEntriesWithStatus"
        :key="listEntry.id"
        class="border-b-2 border-b-gray-100 hover:bg-gray-100 leading-[3] table-row text-18"
      >
        <div class="px-[16px] py-0 table-cell">
          <router-link
            class="underline"
            :to="{
              name: listEntry.fileName
                ? 'caselaw-documentUnit-:documentNumber-categories'
                : 'caselaw-documentUnit-:documentNumber-files',
              params: { documentNumber: listEntry.documentNumber },
            }"
          >
            {{ listEntry.documentNumber }}
          </router-link>
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{ dayjs(listEntry.creationTimestamp).format("DD.MM.YYYY") }}
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{ listEntry.fileNumber ? listEntry.fileNumber : "-" }}
        </div>
        <div class="px-[16px] py-0 table-cell">
          {{
            listEntry.documentationOffice
              ? listEntry.documentationOffice.label
              : "-"
          }}
        </div>
        <div class="px-[16px] py-0 table-cell">
          <IconBadge
            v-if="listEntry.status"
            :color="listEntry.status.color"
            :icon="listEntry.status.icon"
            :value="listEntry.status.value"
          />
        </div>
        <div class="px-16 py-0 table-cell">
          {{ listEntry.fileName ? listEntry.fileName : "-" }}
        </div>
        <div class="table-cell text-center">
          <span
            aria-label="Dokumentationseinheit löschen"
            class="cursor-pointer material-icons"
            tabindex="0"
            @click="
              setSelectedDocumentUnitListEntry(
                documentUnitListEntries.find(
                  (entry) => entry.uuid == listEntry.uuid
                ) as DocumentUnitListEntry
              )
            "
            @keyup.enter="
              setSelectedDocumentUnitListEntry(
                documentUnitListEntries.find(
                  (entry) => entry.uuid == listEntry.uuid
                ) as DocumentUnitListEntry
              )
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
