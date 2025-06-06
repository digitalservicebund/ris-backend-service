<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Component, Ref, computed } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import DocumentUnit, { shortTextLabels } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentUnit | undefined>
}
const decisionName = computed({
  get: () => documentUnit.value?.shortTexts.decisionName,
  set: (newValue) => {
    documentUnit.value!.shortTexts.decisionName =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headline = computed({
  get: () =>
    documentUnit.value?.shortTexts.headline
      ? useValidBorderNumberLinks(
          documentUnit.value.shortTexts.headline,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const guidingPrinciple = computed({
  get: () =>
    documentUnit.value?.shortTexts.guidingPrinciple
      ? useValidBorderNumberLinks(
          documentUnit.value?.shortTexts.guidingPrinciple,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.shortTexts.guidingPrinciple =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headnote = computed({
  get: () =>
    documentUnit.value?.shortTexts.headnote
      ? useValidBorderNumberLinks(
          documentUnit.value?.shortTexts.headnote,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.shortTexts.headnote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const otherHeadnote = computed({
  get: () =>
    documentUnit.value?.shortTexts.otherHeadnote
      ? useValidBorderNumberLinks(
          documentUnit.value?.shortTexts.otherHeadnote,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.shortTexts.otherHeadnote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Kurztexte">
    <h2 class="ris-label1-bold mb-16">Kurztexte</h2>
    <div class="flex flex-col gap-24">
      <TextInputCategory
        id="decisionName"
        v-model="decisionName"
        :data-testid="shortTextLabels.decisionName"
        editable
        :label="shortTextLabels.decisionName"
        :should-show-button="!documentUnit?.shortTexts?.decisionName?.length"
      />

      <TextEditorCategory
        id="headline"
        v-bind="{ registerTextEditorRef }"
        v-model="headline"
        data-testid="headline"
        editable
        field-size="small"
        :label="shortTextLabels.headline"
        :should-show-button="!documentUnit?.shortTexts?.headline?.length"
      />

      <TextEditorCategory
        id="guidingPrinciple"
        v-bind="{ registerTextEditorRef }"
        v-model="guidingPrinciple"
        data-testid="guidingPrinciple"
        editable
        :label="shortTextLabels.guidingPrinciple"
        :should-show-button="
          !documentUnit?.shortTexts?.guidingPrinciple?.length
        "
      />

      <TextEditorCategory
        v-bind="{ registerTextEditorRef }"
        id="headnote"
        v-model="headnote"
        data-testid="headnote"
        editable
        :label="shortTextLabels.headnote"
        :should-show-button="!documentUnit?.shortTexts?.headnote?.length"
      />

      <TextEditorCategory
        id="otherHeadnote"
        v-bind="{ registerTextEditorRef }"
        v-model="otherHeadnote"
        data-testid="otherHeadnote"
        editable
        :label="shortTextLabels.otherHeadnote"
        :should-show-button="!documentUnit?.shortTexts?.otherHeadnote?.length"
      />
    </div>
  </div>
</template>
