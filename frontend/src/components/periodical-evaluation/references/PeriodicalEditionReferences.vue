<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import PeriodicalEditionReferenceInput from "./PeriodicalEditionReferenceInput.vue"
import PeriodicalEditionReferenceSummary from "./PeriodicalEditionReferenceSummary.vue"
import EditableList from "@/components/EditableList.vue"
import InfoModal from "@/components/InfoModal.vue"
import TitleElement from "@/components/TitleElement.vue"
import Reference from "@/domain/reference"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"

const store = useEditionStore()
const responseError = ref<ResponseError | undefined>()

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
</script>

<template>
  <div class="flex w-full p-24">
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement data-testid="references-title">Fundstellen</TitleElement>
      <div v-if="responseError" class="mb-24">
        <InfoModal
          :description="responseError.description"
          :title="responseError.title"
        />
      </div>
      <div aria-label="Fundstellen">
        <EditableList
          v-model="references"
          :default-value="defaultValue"
          :edit-component="PeriodicalEditionReferenceInput"
          :summary-component="PeriodicalEditionReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
