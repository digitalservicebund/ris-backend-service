<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import EditionEvaluationReference from "./EditionReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import ErrorPage from "@/components/PageError.vue"
import EditionReferenceSummary from "@/components/periodical-evaluation/EditionReferenceSummary.vue"
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
  if (response.error) {
    responseError.value = response.error
  }
})

onMounted(async () => {
  const response = await store.loadEdition()
  if (response.error) {
    responseError.value = response.error
  }
})
</script>

<template>
  <div
    v-if="!responseError"
    class="flex h-full w-full flex-col space-y-24 px-16 py-16"
  >
    <h1 class="ds-heading-02-reg">Fundstellen</h1>
    <div aria-label="Fundstellen">
      <EditableList
        v-model="references"
        class="px-16"
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
</template>
