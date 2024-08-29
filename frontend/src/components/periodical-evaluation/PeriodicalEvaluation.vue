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

const title = computed(
  () =>
    `Periodikaauswertung | ${store.edition?.legalPeriodical?.abbreviation}, ${store.edition?.name ? store.edition.name : store.edition?.prefix}`,
)

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
    class="flex h-full flex-col space-y-24 px-16 py-16"
  >
    <h2 class="ds-heading-03-reg">{{ title }}</h2>
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
    :back-router="{ name: 'caselaw-periodical-evaluation' }"
    :error="responseError"
    :title="responseError?.title"
  />
</template>
