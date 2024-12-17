<script setup lang="ts">
import { computed, ref, watch } from "vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconAdd from "~icons/ic/baseline-add"
import IconCheck from "~icons/ic/baseline-check"
import IconInfo from "~icons/ic/outline-info"

const props = defineProps<{
  importableKeywords: string[]
}>()

const store = useDocumentUnitStore()

const importable = computed(() => props.importableKeywords.length > 0)
const copySuccess = ref(false)
const errorMessage = ref<ResponseError>()

const existingKeywords = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.keywords ?? [],
  set: (newValues: string[]) => {
    store.documentUnit!.contentRelatedIndexing.keywords = newValues
  },
})

async function importKeywords() {
  errorMessage.value = undefined
  if (props.importableKeywords?.length) {
    const uniqueImportableKeywords = props.importableKeywords.filter(
      (keyword) => !existingKeywords.value.includes(keyword),
    )
    existingKeywords.value.push(...uniqueImportableKeywords)

    const updateResponse = await store.updateDocumentUnit()
    if (updateResponse.error) {
      errorMessage.value = {
        title: "Fehler beim Speichern der Schlagwörter",
      }
    } else {
      //display success badge for 7 seconds
      copySuccess.value = true
      setTimeout(() => {
        copySuccess.value = false
      }, 7000)

      //scroll to keywords
      const element = document.getElementById(
        DocumentUnitCategoriesEnum.KEYWORDS,
      )
      const headerOffset = 80
      const elementPosition = element ? element.getBoundingClientRect().top : 0
      const offsetPosition = elementPosition + window.scrollY - headerOffset
      window.scrollTo({
        top: offsetPosition,
        behavior: "smooth",
      })
    }
  }
}

watch(
  () => props.importableKeywords,
  () => {
    errorMessage.value = undefined
  },
)
</script>

<template>
  <div class="flex flex-row items-center gap-16">
    <TextButton
      aria-label="Schlagwörter übernehmen"
      button-type="primary"
      :disabled="!importable"
      :icon="IconAdd"
      size="medium"
      @click="importKeywords"
    />
    <span
      class="ds-label-01-reg"
      :class="importable ? 'text-blue-800' : 'text-gray-900'"
      >Schlagwörter</span
    >
    <IconBadge
      v-if="!importable"
      background-color="bg-blue-300"
      color="text-blue-900"
      :icon="IconInfo"
      label="Quellrubrik leer"
    />
    <IconBadge
      v-if="copySuccess"
      background-color="bg-green-300"
      color="text-green-800"
      :icon="IconCheck"
      label="Übernommen"
    />
  </div>
  <InfoModal
    v-if="errorMessage"
    :aria-label="errorMessage.title"
    :title="errorMessage.title"
  />
</template>
