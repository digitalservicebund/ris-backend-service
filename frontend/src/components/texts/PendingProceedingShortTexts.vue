<script lang="ts" setup>
import { storeToRefs } from "pinia"
import InputSelect from "primevue/select"
import { Component, computed, Ref, watch } from "vue"
import InputField from "@/components/input/InputField.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import admissionOfAppealTypes from "@/data/admissionOfAppealTypes.json"
import appellantTypes from "@/data/appellantTypes.json"
import PendingProceeding, {
  pendingProceedingLabels,
} from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit: pendingProceeding } = storeToRefs(store) as {
  documentUnit: Ref<PendingProceeding | undefined>
}
const headline = computed({
  get: () => store.documentUnit?.shortTexts.headline,
  set: (newValue) => {
    store.documentUnit!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const legalIssue = computed({
  get: () => pendingProceeding.value?.shortTexts.legalIssue,
  set: (newValue) => {
    pendingProceeding.value!.shortTexts.legalIssue =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const resolutionNote = computed({
  get: () => pendingProceeding.value?.shortTexts.resolutionNote,
  set: (newValue) => {
    pendingProceeding.value!.shortTexts.resolutionNote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const appellant = computed({
  get: () => pendingProceeding.value?.shortTexts.appellant,
  set: (newValue) => {
    pendingProceeding.value!.shortTexts.appellant =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const admissionOfAppeal = computed({
  get: () => pendingProceeding.value?.shortTexts.admissionOfAppeal,
  set: (newValue) => {
    pendingProceeding.value!.shortTexts.admissionOfAppeal =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
watch(
  () => pendingProceeding.value?.coreData.court?.label,
  (newCourtLabel, oldCourtLabel) => {
    if (newCourtLabel && newCourtLabel !== oldCourtLabel) {
      store.documentUnit!.shortTexts.headline =
        "Anhängiges Verfahren beim " + newCourtLabel
    }
  },
)
</script>

<template>
  <div aria-label="Kurztexte">
    <h2 class="ris-label1-bold mb-16">Kurztexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="headline"
        v-bind="{ registerTextEditorRef }"
        v-model="headline"
        :data-testid="pendingProceedingLabels.headline"
        editable
        field-size="small"
        :label="pendingProceedingLabels.headline"
        :should-show-button="!pendingProceeding?.shortTexts?.headline?.length"
      />

      <TextEditorCategory
        id="legalIssue"
        v-model="legalIssue"
        v-bind="{ registerTextEditorRef }"
        :data-testid="pendingProceedingLabels.legalIssue"
        editable
        field-size="small"
        :label="pendingProceedingLabels.legalIssue"
        :should-show-button="!pendingProceeding?.shortTexts.legalIssue?.length"
      />
      <div class="flex flex-row gap-24">
        <InputField
          id="appellant"
          v-slot="{ id }"
          :label="pendingProceedingLabels.appellant"
        >
          <InputSelect
            :id="id"
            v-model="appellant"
            :aria-label="pendingProceedingLabels.appellant"
            fluid
            option-label="label"
            option-value="value"
            :options="appellantTypes.items"
          />
        </InputField>
        <InputField
          id="admissionOfAppeal"
          v-slot="{ id }"
          :label="pendingProceedingLabels.admissionOfAppeal"
        >
          <InputSelect
            :id="id"
            v-model="admissionOfAppeal"
            :aria-label="pendingProceedingLabels.admissionOfAppeal"
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
        :data-testid="pendingProceedingLabels.resolutionNote"
        editable
        field-size="small"
        :label="pendingProceedingLabels.resolutionNote"
        :should-show-button="
          !pendingProceeding?.shortTexts.resolutionNote?.length
        "
      />
    </div>
  </div>
</template>
