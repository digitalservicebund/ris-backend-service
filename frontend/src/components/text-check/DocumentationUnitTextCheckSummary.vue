<script lang="ts" setup>
import { onMounted, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import SuggestionGroup from "@/components/text-check/SuggestionGroup.vue"
import { ResponseError } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { Match, Suggestion } from "@/types/languagetool"

defineProps<{
  jumpToMatch?: (match: Match) => void
}>()

const responseError = ref<ResponseError | undefined>()

const store = useDocumentUnitStore()

const errors = ref<Suggestion[]>()
const errorCount = ref(0)
const loading = ref(true)

const selectedSuggestion = ref()

const checkAll = async () => {
  if (store.documentUnit) {
    const response = await languageToolService.checkAll(store.documentUnit.uuid)

    errors.value = response.data?.suggestions

    if (response.error) {
      responseError.value = response.error
    }

    if (response.data && response.data.suggestions) {
      let counter = 0
      response.data?.suggestions.forEach(
        (suggestion) => (counter += suggestion.matches.length),
      )
      errorCount.value = counter
      responseError.value = undefined
    }
  }
}

function acceptSuggestion(replacement: string) {
  throw new Error("accept all not yet implemented: " + replacement)
}

function ignoreSuggestion() {
  throw new Error("ignore all not yet implemented ")
}

onMounted(async () => {
  await checkAll()
  loading.value = false
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="flex flex-row gap-4">
      <span class="ds-label-01-bold">Rechtschreibprüfung </span>
      <span v-if="!loading">({{ errorCount }})</span>
    </div>

    <div v-if="responseError">
      <InfoModal
        :description="responseError.description"
        :title="responseError.title"
      />
    </div>

    <div>
      <div v-if="loading" class="my-112 grid justify-items-center">
        <LoadingSpinner />
      </div>
      <div v-else-if="errors?.length == 0">
        <span> Es wurden keine Rechtschreibfehler gefunden. </span>
      </div>
      <div v-else class="flex max-h-[85vh] flex-col gap-16 overflow-scroll">
        <SuggestionGroup
          v-for="error in errors"
          :key="error.word"
          :is-selected="selectedSuggestion == error"
          :suggestion="error"
          v-bind="{ jumpToMatch }"
          @click="selectedSuggestion = error"
          @suggestion:ignore="ignoreSuggestion"
          @suggestion:update="acceptSuggestion"
        />
      </div>
    </div>
  </div>
</template>
