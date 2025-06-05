<script lang="ts" setup>
import type { Component } from "vue"
import { computed } from "vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
import { pendingProceedingShortTextLabels } from "@/domain/pendingProceeding"
import { usePendingProceedingStore } from "@/stores/pendingProceedingStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = usePendingProceedingStore()

const headline = computed({
  get: () => store.pendingProceeding?.shortTexts.headline,
  set: (newValue) => {
    store.pendingProceeding!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const legalIssue = computed({
  get: () => store.pendingProceeding?.legalIssue,
  set: (newValue) => {
    store.pendingProceeding!.legalIssue =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const resolutionNote = computed({
  get: () => store.pendingProceeding?.resolutionNote,
  set: (newValue) => {
    store.pendingProceeding!.resolutionNote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

// const admissionOfAppeal = computed({
//   get: () => store.pendingProceeding?.admissionOfAppeal,
//   set: (newValue) => {
//     store.pendingProceeding!.admissionOfAppeal =
//       TextEditorUtil.getEditorContentIfPresent(newValue)
//   },
// })
//
// const appellant = computed({
//   get: () => store.pendingProceeding?.appellant,
//   set: (newValue) => {
//     store.pendingProceeding!.appellant =
//       TextEditorUtil.getEditorContentIfPresent(newValue)
//   },
// })
</script>

<template>
  <div aria-label="Kurztexte">
    <h2 class="ris-label1-bold mb-16">Kurztexte</h2>
    <div class="flex flex-col gap-24">
      <TextInputCategory
        id="headline"
        v-model="headline"
        :data-testid="pendingProceedingShortTextLabels.headline"
        editable
        :label="pendingProceedingShortTextLabels.headline"
        :should-show-button="
          !store.pendingProceeding?.shortTexts.headline?.length
        "
      />

      <TextInputCategory
        id="legalIssue"
        v-model="legalIssue"
        :data-testid="pendingProceedingShortTextLabels.legalIssue"
        editable
        :label="pendingProceedingShortTextLabels.legalIssue"
        :should-show-button="!store.pendingProceeding?.legalIssue?.length"
      />

      <TextInputCategory
        id="resolutionNote"
        v-model="resolutionNote"
        :data-testid="pendingProceedingShortTextLabels.resolutionNote"
        editable
        :label="pendingProceedingShortTextLabels.resolutionNote"
        :should-show-button="!store.pendingProceeding?.resolutionNote?.length"
      />
    </div>
  </div>
</template>
