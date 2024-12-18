<script setup lang="ts">
import { computed, ref, watch } from "vue"
import ImportSingleCategory from "@/components/category-import/ImportSingleCategory.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  importableFieldsOfLaw: FieldOfLaw[]
}>()

const store = useDocumentUnitStore()

const importable = computed(() => props.importableFieldsOfLaw.length > 0)
const copySuccess = ref(false)
const errorMessage = ref<string | undefined>(undefined)

const existingFieldsOfLaw = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.fieldsOfLaw ?? [],
  set: (newValues: FieldOfLaw[]) => {
    store.documentUnit!.contentRelatedIndexing.fieldsOfLaw = newValues
  },
})

async function importFieldsOfLaw() {
  hideError()
  if (!importable.value) return
  const uniqueImportableFieldsOfLaw = props.importableFieldsOfLaw.filter(
    (fieldOfLaw) =>
      !existingFieldsOfLaw.value.find(
        (entry) => entry.identifier === fieldOfLaw.identifier,
      ),
  )
  existingFieldsOfLaw.value.push(...uniqueImportableFieldsOfLaw)

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    errorMessage.value = "Fehler beim Speichern der Sachgebiete"
  } else {
    //display success badge for 7 seconds
    copySuccess.value = true
    setTimeout(() => {
      copySuccess.value = false
    }, 7000)

    scrollToCategory()
  }
}

function scrollToCategory() {
  const element = document.getElementById(
    DocumentUnitCategoriesEnum.FIELDS_OF_LAW,
  )
  const headerOffset = 80
  const elementPosition = element ? element.getBoundingClientRect().top : 0
  const offsetPosition = elementPosition + window.scrollY - headerOffset
  window.scrollTo({
    top: offsetPosition,
    behavior: "smooth",
  })
}

function hideError() {
  errorMessage.value = undefined
}

watch(
  () => props.importableFieldsOfLaw,
  () => {
    hideError()
  },
)
</script>

<template>
  <ImportSingleCategory
    :error-message="errorMessage"
    :import-success="copySuccess"
    :importable="importable"
    label="Sachgebiete"
    @import="importFieldsOfLaw"
  />
</template>
