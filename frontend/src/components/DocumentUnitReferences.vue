<script lang="ts" setup>
import { computed, ref } from "vue"
import DocumentUnitLiteratureReferenceInput from "@/components/DocumentUnitLiteratureReferenceInput.vue"
import DocumentUnitReferenceInput from "@/components/DocumentUnitReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import TextButton from "@/components/input/TextButton.vue"
import ReferenceSummary from "@/components/ReferenceSummary.vue"
import TitleElement from "@/components/TitleElement.vue"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconAdd from "~icons/material-symbols/add"

const store = useDocumentUnitStore()
const caselawReferenceListRef = ref()
const literatureReferenceListRef = ref()

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
  const element = document.getElementById(elementId)
  setTimeout(() => {
    if (!element) return
    const headerOffset = 110
    const scrollPosition =
      element.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({
      top: scrollPosition,
      behavior: "smooth",
    })
  })
}
</script>

<template>
  <div class="flex w-full flex-1 grow flex-col p-24">
    <div aria-label="Fundstellen" class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Fundstellen</TitleElement>
      <TextButton
        v-if="caselawReferences.length > 5"
        aria-label="Weitere Angabe Rechtsprechung Top"
        button-type="tertiary"
        :icon="IconAdd"
        label="Weitere Angabe"
        size="small"
        @click="addNewEntry('caselaw')"
      />
      <div class="flex flex-row" data-testid="caselaw-reference-list">
        <EditableList
          ref="caselawReferenceListRef"
          v-model="caselawReferences"
          :create-entry="() => new Reference()"
          :edit-component="DocumentUnitReferenceInput"
          :summary-component="ReferenceSummary"
        />
      </div>

      <TitleElement>Literaturfundstellen</TitleElement>
      <TextButton
        v-if="literatureReferences.length > 5"
        aria-label="Weitere Angabe Literatur Top"
        button-type="tertiary"
        :icon="IconAdd"
        label="Weitere Angabe"
        size="small"
        @click="addNewEntry('literature')"
      />
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
</template>
