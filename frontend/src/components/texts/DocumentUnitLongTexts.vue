<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { type Component, computed, Ref } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { DocumentUnit, longTextLabels } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentUnit | undefined>
}

const tenor = computed({
  get: () =>
    documentUnit.value?.longTexts.tenor
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.tenor,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.tenor =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const reasons = computed({
  get: () =>
    documentUnit.value?.longTexts.reasons
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.reasons,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.reasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const caseFacts = computed({
  get: () =>
    documentUnit.value?.longTexts.caseFacts
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.caseFacts,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.caseFacts =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const decisionReasons = computed({
  get: () =>
    documentUnit.value?.longTexts.decisionReasons
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.decisionReasons,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.decisionReasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Langtexte">
    <h2 class="ris-label1-bold mb-16">Langtexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="tenor"
        v-bind="{ registerTextEditorRef }"
        v-model="tenor"
        data-testid="tenor"
        :label="longTextLabels.tenor"
        :should-show-button="!documentUnit?.longTexts?.tenor?.length"
      />

      <TextEditorCategory
        id="reasons"
        v-bind="{ registerTextEditorRef }"
        v-model="reasons"
        data-testid="reasons"
        :label="longTextLabels.reasons"
        :should-show-button="!documentUnit?.longTexts?.reasons?.length"
      />

      <TextEditorCategory
        id="caseFacts"
        v-bind="{ registerTextEditorRef }"
        v-model="caseFacts"
        data-testid="caseFacts"
        :label="longTextLabels.caseFacts"
        :should-show-button="!documentUnit?.longTexts?.caseFacts?.length"
      />

      <TextEditorCategory
        id="decisionReasons"
        v-bind="{ registerTextEditorRef }"
        v-model="decisionReasons"
        data-testid="decisionReasons"
        :label="longTextLabels.decisionReasons"
        :should-show-button="!documentUnit?.longTexts?.decisionReasons?.length"
      />
    </div>
  </div>
</template>
