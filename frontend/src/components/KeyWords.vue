<script lang="ts" setup>
import { ref, computed } from "vue"
import KeywordsChipsInput from "@/components/input/KeywordsChipsInput.vue"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const errorMessage = ref<ResponseError>()

const store = useDocumentUnitStore()

const keywords = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.keywords,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.keywords = newValues
  },
})
</script>

<template>
  <div aria-label="Schlagwörter" class="border-b-1 border-blue-300">
    <h2 class="ds-label-01-bold mb-16">Schlagwörter</h2>
    <KeywordsChipsInput
      id="keywords"
      v-model="keywords"
      aria-label="Schlagwörter"
      :error="errorMessage"
    ></KeywordsChipsInput>
  </div>
</template>
