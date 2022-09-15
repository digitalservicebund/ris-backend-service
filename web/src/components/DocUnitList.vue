<script lang="ts" setup>
import dayjs from "dayjs"
import { ref } from "vue"
import DocUnit from "../domain/docUnit"
import PopupModal from "./PopupModal.vue"

defineProps<{ docUnits: DocUnit[] }>()
const emit = defineEmits<{
  (e: "deleteDocUnit", docUnit: DocUnit): void
}>()

const showModal = ref(false)
const popupModalText = ref("")
const modalConfirmText = ref("Löschen")
const modalHeaderText = "Dokumentationseinheit löschen"
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const selectedDocUnit = ref(new DocUnit("1"))
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
const setSelectedDocUnit = (docUnit: DocUnit) => {
  selectedDocUnit.value = docUnit
  popupModalText.value = `Möchten Sie die Dokumentationseinheit ${selectedDocUnit.value.documentnumber} wirklich dauerhaft löschen?`
  toggleModal()
}
const onDelete = () => {
  emit("deleteDocUnit", selectedDocUnit.value)
  toggleModal()
}
</script>

<template>
  <div>
    <PopupModal
      v-if="showModal"
      :content-text="popupModalText"
      :confirm-text="modalConfirmText"
      :header-text="modalHeaderText"
      :cancel-button-type="modalCancelButtonType"
      :confirm-button-type="modalConfirmButtonType"
      @close-modal="toggleModal"
      @confirm-action="onDelete"
    />
    <v-table v-if="docUnits.length" class="doc-unit-list-table">
      <thead>
        <tr class="table-header">
          <th class="text-center" scope="col">Dok.-Nummer</th>
          <th class="text-center" scope="col">Angelegt am</th>
          <th class="text-center" scope="col">Aktenzeichen</th>
          <th class="text-center" scope="col">Dokumente</th>
          <th class="text-center" scope="col">Löschen</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="docUnit in docUnits" :key="docUnit.id">
          <td>
            <router-link
              class="doc-unit-list-active-link"
              :to="{
                name: docUnit.s3path
                  ? 'jurisdiction-docUnit-:documentNumber-categories'
                  : 'jurisdiction-docUnit-:documentNumber-files',
                params: { documentNumber: docUnit.documentnumber },
              }"
            >
              {{ docUnit.documentnumber }}
            </router-link>
          </td>
          <td>{{ dayjs(docUnit.creationtimestamp).format("DD.MM.YYYY") }}</td>
          <td>{{ docUnit.fileNumber ? docUnit.fileNumber : "-" }}</td>
          <td>
            {{ docUnit.filename ? docUnit.filename : "-" }}
          </td>
          <td>
            <span tabindex="0" @keyup.enter="setSelectedDocUnit(docUnit)">
              <v-icon
                aria-label="Dokumentationseinheit löschen"
                @click="setSelectedDocUnit(docUnit)"
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
@import "@/styles/variables";

.table-header {
  background-color: $gray400;
}

.doc-unit-list-table td,
th {
  font-size: medium !important;
}

.doc-unit-list-active-link {
  text-decoration: underline;
}
</style>
