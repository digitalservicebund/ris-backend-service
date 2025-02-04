<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitLiteratureReferenceInput from "@/components/DocumentUnitLiteratureReferenceInput.vue"
import DocumentUnitReferenceInput from "@/components/DocumentUnitReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import ReferenceSummary from "@/components/ReferenceSummary.vue"
import TitleElement from "@/components/TitleElement.vue"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const caselawReferences = computed({
  get: () => store.documentUnit!.caselawReferences as Reference[],
  set: (newValues) => {
    store.documentUnit!.caselawReferences = newValues
  },
})

const literatureReferences = computed({
  get: () => store.documentUnit!.literatureReferences as Reference[],
  set: (newValues) => {
    store.documentUnit!.literatureReferences = newValues
  },
})
</script>

<template>
  <div class="flex w-full flex-1 grow flex-col p-24">
    <div aria-label="Fundstellen" class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Fundstellen</TitleElement>
      <div class="flex flex-row" data-testid="caselaw-reference-list">
        <EditableList
          v-model="caselawReferences"
          :edit-component="DocumentUnitReferenceInput"
          :summary-component="ReferenceSummary"
        />
      </div>

      <TitleElement>Literaturfundstellen</TitleElement>
      <div class="flex flex-row" data-testid="literature-reference-list">
        <EditableList
          v-model="literatureReferences"
          :edit-component="DocumentUnitLiteratureReferenceInput"
          :summary-component="ReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
