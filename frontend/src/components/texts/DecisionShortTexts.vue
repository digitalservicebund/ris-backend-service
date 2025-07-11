<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Component, Ref, computed } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { Decision, shortTextLabels } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const decisionName = computed({
  get: () => decision.value?.shortTexts.decisionName,
  set: (newValue) => {
    decision.value!.shortTexts.decisionName =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headline = computed({
  get: () =>
    decision.value?.shortTexts.headline
      ? useValidBorderNumberLinks(
          decision.value.shortTexts.headline,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const guidingPrinciple = computed({
  get: () =>
    decision.value?.shortTexts.guidingPrinciple
      ? useValidBorderNumberLinks(
          decision.value?.shortTexts.guidingPrinciple,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.shortTexts.guidingPrinciple =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headnote = computed({
  get: () =>
    decision.value?.shortTexts.headnote
      ? useValidBorderNumberLinks(
          decision.value?.shortTexts.headnote,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.shortTexts.headnote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const otherHeadnote = computed({
  get: () =>
    decision.value?.shortTexts.otherHeadnote
      ? useValidBorderNumberLinks(
          decision.value?.shortTexts.otherHeadnote,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.shortTexts.otherHeadnote =
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
        :should-show-button="!decision?.shortTexts?.decisionName?.length"
      />

      <TextEditorCategory
        id="headline"
        v-bind="{ registerTextEditorRef }"
        v-model="headline"
        data-testid="headline"
        editable
        field-size="small"
        :label="shortTextLabels.headline"
        :should-show-button="!decision?.shortTexts?.headline?.length"
      />

      <TextEditorCategory
        id="guidingPrinciple"
        v-bind="{ registerTextEditorRef }"
        v-model="guidingPrinciple"
        data-testid="guidingPrinciple"
        editable
        :label="shortTextLabels.guidingPrinciple"
        :should-show-button="!decision?.shortTexts?.guidingPrinciple?.length"
      />

      <TextEditorCategory
        v-bind="{ registerTextEditorRef }"
        id="headnote"
        v-model="headnote"
        data-testid="headnote"
        editable
        :label="shortTextLabels.headnote"
        :should-show-button="!decision?.shortTexts?.headnote?.length"
      />

      <TextEditorCategory
        id="otherHeadnote"
        v-bind="{ registerTextEditorRef }"
        v-model="otherHeadnote"
        data-testid="otherHeadnote"
        editable
        :label="shortTextLabels.otherHeadnote"
        :should-show-button="!decision?.shortTexts?.otherHeadnote?.length"
      />
    </div>
  </div>
</template>
