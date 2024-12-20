<script setup lang="ts">
import { computed, ref, watch } from "vue"
import ImportSingleCategory from "@/components/category-import/ImportSingleCategory.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  importableKeywords: string[] | undefined
  importableFieldsOfLaw: FieldOfLaw[] | undefined
}>()

const store = useDocumentUnitStore()

const keywords = computed({
  get: () => store.documentUnit?.contentRelatedIndexing.keywords ?? [],
  set: (newValues: string[]) =>
    (store.documentUnit!.contentRelatedIndexing.keywords = newValues),
})

const fieldsOfLaw = computed({
  get: () => store.documentUnit?.contentRelatedIndexing.fieldsOfLaw ?? [],
  set: (newValues: FieldOfLaw[]) =>
    (store.documentUnit!.contentRelatedIndexing.fieldsOfLaw = newValues),
})

const importSuccess = ref({ keywords: false, fieldsOfLaw: false })
const errorMessage = ref({
  keywords: undefined as string | undefined,
  fieldsOfLaw: undefined as string | undefined,
})

const importableData = computed(() => ({
  keywords: props.importableKeywords ?? [],
  fieldsOfLaw: props.importableFieldsOfLaw ?? [],
}))

const isImportable = computed(() => ({
  keywords: importableData.value.keywords.length > 0,
  fieldsOfLaw: importableData.value.fieldsOfLaw.length > 0,
}))

const category = {
  keywords: DocumentUnitCategoriesEnum.KEYWORDS,
  fieldsOfLaw: DocumentUnitCategoriesEnum.FIELDS_OF_LAW,
}

const labelText = {
  keywords: "Schlagwörter",
  fieldsOfLaw: "Sachgebiete",
}

type CategoryType = "keywords" | "fieldsOfLaw"

async function importData(type: CategoryType, mergeData: () => void) {
  hideErrors()
  if (!isImportable.value[type]) return

  mergeData()

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    errorMessage.value[type] = "Fehler beim Speichern der " + labelText[type]
  } else {
    displaySuccess(type)
    scrollToCategory(category[type])
  }
}

function mergeKeywords() {
  const uniqueImportableKeywords = importableData.value.keywords.filter(
    (keyword) => !keywords.value.includes(keyword),
  )
  keywords.value.push(...uniqueImportableKeywords)
}

function mergeFieldsOfLaw() {
  const uniqueImportableFieldsOfLaw = importableData.value.fieldsOfLaw.filter(
    (fieldOfLaw) =>
      !fieldsOfLaw.value.find(
        (entry) => entry.identifier === fieldOfLaw.identifier,
      ),
  )
  fieldsOfLaw.value.push(...uniqueImportableFieldsOfLaw)
}

function displaySuccess(type: CategoryType) {
  importSuccess.value[type] = true
  setTimeout(() => (importSuccess.value[type] = false), 7000)
}

function scrollToCategory(category: DocumentUnitCategoriesEnum) {
  const element = document.getElementById(category)
  if (element) {
    const headerOffset = 80
    const offsetPosition =
      element.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top: offsetPosition, behavior: "smooth" })
  }
}

function hideErrors() {
  errorMessage.value = { keywords: undefined, fieldsOfLaw: undefined }
}

watch(
  () => props,
  () => hideErrors(),
)
</script>

<template>
  <ImportSingleCategory
    :error-message="errorMessage.keywords"
    :import-success="importSuccess.keywords"
    :importable="isImportable.keywords"
    :label="labelText.keywords"
    @import="
      importData('keywords', () => {
        mergeKeywords()
      })
    "
  />

  <ImportSingleCategory
    :error-message="errorMessage.fieldsOfLaw"
    :import-success="importSuccess.fieldsOfLaw"
    :importable="isImportable.fieldsOfLaw"
    :label="labelText.fieldsOfLaw"
    @import="
      importData('fieldsOfLaw', () => {
        mergeFieldsOfLaw()
      })
    "
  />
</template>
