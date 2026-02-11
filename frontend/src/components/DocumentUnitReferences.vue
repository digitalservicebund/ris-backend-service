<script lang="ts" setup>
import Button from "primevue/button"
import { computed, ref } from "vue"
import DocumentUnitLiteratureReferenceInput from "@/components/DocumentUnitLiteratureReferenceInput.vue"
import DocumentUnitReferenceInput from "@/components/DocumentUnitReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import ReferenceSummary from "@/components/ReferenceSummary.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useScroll } from "@/composables/useScroll"
import { DocumentationUnit } from "@/domain/documentationUnit"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDecision } from "@/utils/typeGuards"
import IconAdd from "~icons/material-symbols/add"

const store = useDocumentUnitStore()
const caselawReferenceListRef = ref()
const literatureReferenceListRef = ref()
const { scrollIntoViewportById } = useScroll()

const caselawReferences = computed({
  get: () => (store.documentUnit?.caselawReferences ?? []) as Reference[],
  set: (newValues) => {
    if (store.documentUnit) {
      store.documentUnit.caselawReferences = newValues
    }
  },
})

const literatureReferences = computed({
  get: () => (store.documentUnit?.literatureReferences ?? []) as Reference[],
  set: (newValues) => {
    if (store.documentUnit) {
      store.documentUnit.literatureReferences = newValues
    }
  },
})

async function addNewEntry(entryType: "caselaw" | "literature") {
  const referenceMap = {
    caselaw: {
      ref: caselawReferenceListRef,
      elementId: "caselaw-reference-input",
    },
    literature: {
      ref: literatureReferenceListRef,
      elementId: "literature-reference-input",
    },
  }

  const { ref, elementId } = referenceMap[entryType]

  await ref.value.toggleNewEntry(true)

  await scrollIntoViewportById(elementId)
}
</script>

<template>
  <div class="flex w-full flex-1 grow flex-col p-24">
    <div
      v-if="caselawReferences.length + literatureReferences.length > 3"
      class="flex flex-row gap-24 pb-24"
    >
      <Button
        aria-label="Weitere Angabe Rechtsprechung Top"
        label="Weitere Rechtsprechungsfundstelle"
        severity="secondary"
        size="small"
        @click="addNewEntry('caselaw')"
        ><template #icon> <IconAdd /> </template
      ></Button>
      <Button
        aria-label="Weitere Angabe Literatur Top"
        label="Weitere Literaturfundstelle"
        severity="secondary"
        size="small"
        @click="addNewEntry('literature')"
        ><template #icon> <IconAdd /> </template
      ></Button>
    </div>

    <div
      aria-label="Rechtsprechungsfundstellen"
      class="flex flex-col gap-24 bg-white p-24"
    >
      <TitleElement>Rechtsprechungsfundstellen</TitleElement>
      <div class="flex flex-row" data-testid="caselaw-reference-list">
        <EditableList
          ref="caselawReferenceListRef"
          v-model="caselawReferences"
          :create-entry="() => new Reference()"
          :edit-component="DocumentUnitReferenceInput"
          :summary-component="ReferenceSummary"
        />
      </div>

      <div v-if="isDecision(store.documentUnit as DocumentationUnit)">
        <TitleElement>Literaturfundstellen</TitleElement>

        <div class="flex flex-row" data-testid="literature-reference-list">
          <EditableList
            ref="literatureReferenceListRef"
            v-model="literatureReferences"
            :create-entry="() => new Reference()"
            :edit-component="DocumentUnitLiteratureReferenceInput"
            :summary-component="ReferenceSummary"
          />
        </div>
      </div>
    </div>
  </div>
</template>
