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
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocumentUnit.value.documentnumber} wirklich dauerhaft löschen?`
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
    <v-table v-if="documentUnits.length" class="document-unit-list-table">
      <thead>
        <tr class="bg-gray-400">
          <th class="text-center" scope="col">Dok.-Nummer</th>
          <th class="text-center" scope="col">Angelegt am</th>
          <th class="text-center" scope="col">Aktenzeichen</th>
          <th class="text-center" scope="col">Dokumente</th>
          <th class="text-center" scope="col">Löschen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="documentUnit in documentUnits" :key="documentUnit.id">
          <td>
            <router-link
              class="document-unit-list-active-link"
              :to="{
                name: documentUnit.s3path
                  ? 'jurisdiction-documentUnit-:documentNumber-categories'
                  : 'jurisdiction-documentUnit-:documentNumber-files',
                params: { documentNumber: documentUnit.documentnumber },
              }"
            >
              {{ documentUnit.documentnumber }}
            </router-link>
          </td>
          <td>
            {{ dayjs(documentUnit.creationtimestamp).format("DD.MM.YYYY") }}
          </td>
          <td>{{ documentUnit.fileNumber ? documentUnit.fileNumber : "-" }}</td>
          <td>
            {{ documentUnit.filename ? documentUnit.filename : "-" }}
          </td>
          <td>
            <span
              tabindex="0"
              @keyup.enter="setSelectedDocumentUnit(documentUnit)"
            >
              <v-icon
                aria-label="Dokumentationseinheit löschen"
                @click="setSelectedDocumentUnit(documentUnit)"
              >
                delete
              </v-icon>
            </span>
          </td>
        </tr>
      </tbody>
    </v-table>
    <span v-else>Keine Dokumentationseinheiten gefunden</span>
  </div>
</template>

<style lang="scss" scoped>
.document-unit-list-table td,
th {
  font-size: medium !important;
}

.document-unit-list-active-link {
  text-decoration: underline;
}
</style>
