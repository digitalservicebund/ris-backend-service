<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import EditionEvaluationReference from "./EditionReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import ErrorPage from "@/components/PageError.vue"
import EditionReferenceSummary from "@/components/periodical-evaluation/EditionReferenceSummary.vue"
import TitleElement from "@/components/TitleElement.vue"
import Reference from "@/domain/reference"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"

const store = useEditionStore()
const responseError = ref<ResponseError>()

const references = computed({
  get: () => (store.edition ? (store.edition.references as Reference[]) : []),
  set: (newValues) => {
    store.edition!.references = newValues
  },
})

const defaultValue = new Reference() as Reference

watch(references, async () => {
  const response = await store.updateEdition()
  responseError.value = response.error ? response.error : undefined
})

onMounted(async () => {
  const response = await store.loadEdition()
  responseError.value = response.error ? response.error : undefined
})
</script>

<template>
  <div class="flex w-full p-24">
    <div
      v-if="!responseError"
      class="flex w-full flex-col gap-24 bg-white p-24"
    >
      <TitleElement data-testid="references-title">Fundstellen</TitleElement>

      <div aria-label="Fundstellen">
        <EditableList
          v-model="references"
          :default-value="defaultValue"
          :edit-component="EditionEvaluationReference"
          :summary-component="EditionReferenceSummary"
        />
      </div>
    </div>
    <ErrorPage
      v-else
      back-button-label="Zurück zur Übersicht"
      back-router-name="caselaw-periodical-evaluation"
      :error="responseError"
      :title="responseError?.title"
    />
  </div>
</template>
