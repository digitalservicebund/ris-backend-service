<script lang="ts" setup>
import dayjs from "dayjs"
import { ref } from "vue"
import DocUnit from "../domain/docUnit"
import PopupModal from "./PopupModal.vue"

defineProps<{ docUnits: DocUnit[] }>()
const emit = defineEmits<{
  (e: "deleteDocUnit", docUnit: DocUnit): void
}>()

// Start: For popup modal to confirm delete action
const showModal = ref<boolean>(false)
const popupModalText = ref<string>(
  `Diese DE wurde bereits veröffentlicht, möchten Sie diese wirklich löschen?`
)
const confirmText = ref<string>("Löschen")
const selectedDocUnit = ref<DocUnit>()
const toggleModal = () => {
  showModal.value = !showModal.value
}
const setSelectedDocUnit = (docUnit: DocUnit) => {
  selectedDocUnit.value = docUnit
  popupModalText.value = `Diese DE ${selectedDocUnit.value.documentnumber} wurde bereits veröffentlicht, möchten Sie diese wirklich löschen?`
  toggleModal()
}
const onDelete = () => {
  emit("deleteDocUnit", selectedDocUnit.value)
  toggleModal()
}
// End: For popup modal to confirm delete action
</script>

<template>
  <PopupModal
    v-if="showModal"
    :content-text="popupModalText"
    :confirm-text="confirmText"
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
        <td>{{ docUnit.aktenzeichen ? docUnit.aktenzeichen : "-" }}</td>
        <td>
          {{ docUnit.filename ? docUnit.filename : "-" }}
        </td>
        <td>
          <v-icon
            aria-label="Dokumentationseinheit löschen"
            @click="setSelectedDocUnit(docUnit)"
          >
            delete
          </v-icon>
        </td>
      </tr>
    </tbody>
  </v-table>
  <span v-else>Keine Dokumentationseinheiten gefunden</span>
</template>

<style lang="scss">
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
