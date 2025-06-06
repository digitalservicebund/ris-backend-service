<script lang="ts" setup>
import InputSelect from "primevue/select"
import { Component, computed } from "vue"
import InputField from "@/components/input/InputField.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import admissionOfAppealTypes from "@/data/admissionOfAppealTypes.json"
import appellantTypes from "@/data/appellantTypes.json"
import { shortTextLabels } from "@/domain/documentUnit"
import { pendingProceedingShortTextLabels } from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()

const headline = computed({
  get: () => store.documentUnit?.shortTexts.headline,
  set: (newValue) => {
    store.documentUnit!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const legalIssue = computed({
  get: () => store.documentUnit?.legalIssue,
  set: (newValue) => {
    store.documentUnit!.legalIssue =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const resolutionNote = computed({
  get: () => store.documentUnit?.resolutionNote,
  set: (newValue) => {
    store.documentUnit!.resolutionNote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const appellant = computed({
  get: () => store.documentUnit?.appellant,
  set: (newValue) => {
    store.documentUnit!.appellant =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const admissionOfAppeal = computed({
  get: () => store.documentUnit?.admissionOfAppeal,
  set: (newValue) => {
    store.documentUnit!.admissionOfAppeal =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Kurztexte">
    <h2 class="ris-label1-bold mb-16">Kurztexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="headline"
        v-bind="{ registerTextEditorRef }"
        v-model="headline"
        :data-testid="pendingProceedingShortTextLabels.headline"
        editable
        field-size="small"
        :label="shortTextLabels.headline"
        :should-show-button="!store.documentUnit?.shortTexts?.headline?.length"
      />

      <TextEditorCategory
        id="legalIssue"
        v-model="legalIssue"
        v-bind="{ registerTextEditorRef }"
        :data-testid="pendingProceedingShortTextLabels.legalIssue"
        editable
        field-size="small"
        :label="pendingProceedingShortTextLabels.legalIssue"
        :should-show-button="!store.documentUnit?.legalIssue?.length"
      />
      <div class="flex flex-row gap-24">
        <InputField
          id="appellant"
          v-slot="{ id }"
          :label="pendingProceedingShortTextLabels.appellant"
        >
          <InputSelect
            :id="id"
            v-model="appellant"
            :aria-label="pendingProceedingShortTextLabels.appellant"
            fluid
            option-label="label"
            option-value="value"
            :options="appellantTypes.items"
          />
        </InputField>
        <InputField
          id="admissionOfAppeal"
          v-slot="{ id }"
          :label="pendingProceedingShortTextLabels.admissionOfAppeal"
        >
          <InputSelect
            :id="id"
            v-model="admissionOfAppeal"
            :aria-label="pendingProceedingShortTextLabels.admissionOfAppeal"
            fluid
            option-label="label"
            option-value="value"
            :options="admissionOfAppealTypes.items"
          />
        </InputField>
      </div>

      <TextEditorCategory
        id="resolutionNote"
        v-model="resolutionNote"
        v-bind="{ registerTextEditorRef }"
        :data-testid="pendingProceedingShortTextLabels.resolutionNote"
        editable
        field-size="small"
        :label="pendingProceedingShortTextLabels.resolutionNote"
        :should-show-button="!store.documentUnit?.resolutionNote?.length"
      />
    </div>
  </div>
</template>
