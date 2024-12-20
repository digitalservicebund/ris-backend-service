<script setup lang="ts">
import { computed, ref, watch } from "vue"
import ImportCategoryItem from "@/components/category-import/ImportCategoryItem.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  importableKeywords?: string[]
  importableFieldsOfLaw?: FieldOfLaw[]
  importableNorms?: NormReference[]
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

const norms = computed({
  get: () => store.documentUnit?.contentRelatedIndexing.norms ?? [],
  set: (newValues: NormReference[]) =>
    (store.documentUnit!.contentRelatedIndexing.norms = newValues),
})

const importSuccess = ref({ keywords: false, fieldsOfLaw: false, norms: false })
const errorMessage = ref({
  keywords: undefined as string | undefined,
  fieldsOfLaw: undefined as string | undefined,
  norms: undefined as string | undefined,
})

const importableData = computed(() => ({
  keywords: props.importableKeywords ?? [],
  fieldsOfLaw: props.importableFieldsOfLaw ?? [],
  norms: props.importableNorms ?? [],
}))

const isImportable = computed(() => ({
  keywords: importableData.value.keywords.length > 0,
  fieldsOfLaw: importableData.value.fieldsOfLaw.length > 0,
  norms: importableData.value.norms.length > 0,
}))

const categoryIdentifier = {
  keywords: DocumentUnitCategoriesEnum.KEYWORDS,
  fieldsOfLaw: DocumentUnitCategoriesEnum.FIELDS_OF_LAW,
  norms: DocumentUnitCategoriesEnum.NORMS,
}

const labelText = {
  keywords: "Schlagwörter",
  fieldsOfLaw: "Sachgebiete",
  norms: "Normen",
}

type CategoryType = "keywords" | "fieldsOfLaw" | "norms"

async function importData(type: CategoryType, mergeData: () => void) {
  hideErrors()
  if (!isImportable.value[type]) return

  mergeData()

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    errorMessage.value[type] = "Fehler beim Speichern der " + labelText[type]
  } else {
    displaySuccess(type)
    scrollToCategory(categoryIdentifier[type])
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

function mergeNorms() {
  importableData.value.norms.forEach((importableNorm) => {
    const existingWithAbbreviation = norms.value.find(
      (existing) =>
        existing.normAbbreviation?.abbreviation ===
        importableNorm.normAbbreviation?.abbreviation,
    )
    if (existingWithAbbreviation) {
      //import single norms into existing norm reference
      const singleNorms = existingWithAbbreviation?.singleNorms
      if (singleNorms && importableNorm.singleNorms) {
        importableNorm.singleNorms
          .filter(
            (importableSingleNorm) =>
              !singleNorms.some(
                (singleNorm) =>
                  singleNorm.singleNorm === importableSingleNorm.singleNorm,
              ),
          )
          .map((importableSingleNorm) => {
            singleNorms.push(
              new SingleNorm({ ...importableSingleNorm, id: undefined }),
            )
          })
      }
    } else {
      // import entire norm reference
      importableNorm.singleNorms?.forEach(
        (singleNorm) => (singleNorm.id = undefined),
      )
      norms.value.push(new NormReference({ ...importableNorm }))
    }
  })
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
  errorMessage.value = {
    keywords: undefined,
    fieldsOfLaw: undefined,
    norms: undefined,
  }
}

watch(
  () => props,
  () => hideErrors(),
)
</script>

<template>
  <ImportCategoryItem
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

  <ImportCategoryItem
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

  <ImportCategoryItem
    :error-message="errorMessage.norms"
    :import-success="importSuccess.norms"
    :importable="isImportable.norms"
    :label="labelText.norms"
    @import="
      importData('norms', () => {
        mergeNorms()
      })
    "
  />
</template>
