<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { type Component, computed, Ref } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { Decision, longTextLabels } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}

const tenor = computed({
  get: () =>
    decision.value?.longTexts.tenor
      ? useValidBorderNumberLinks(
          decision.value?.longTexts.tenor,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.longTexts.tenor =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const reasons = computed({
  get: () =>
    decision.value?.longTexts.reasons
      ? useValidBorderNumberLinks(
          decision.value?.longTexts.reasons,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.longTexts.reasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const caseFacts = computed({
  get: () =>
    decision.value?.longTexts.caseFacts
      ? useValidBorderNumberLinks(
          decision.value?.longTexts.caseFacts,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.longTexts.caseFacts =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const decisionReasons = computed({
  get: () =>
    decision.value?.longTexts.decisionReasons
      ? useValidBorderNumberLinks(
          decision.value?.longTexts.decisionReasons,
          decision.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    decision.value!.longTexts.decisionReasons =
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
        :should-show-button="!decision?.longTexts?.tenor?.length"
      />

      <TextEditorCategory
        id="reasons"
        v-bind="{ registerTextEditorRef }"
        v-model="reasons"
        data-testid="reasons"
        :label="longTextLabels.reasons"
        :should-show-button="!decision?.longTexts?.reasons?.length"
      />

      <TextEditorCategory
        id="caseFacts"
        v-bind="{ registerTextEditorRef }"
        v-model="caseFacts"
        data-testid="caseFacts"
        :label="longTextLabels.caseFacts"
        :should-show-button="!decision?.longTexts?.caseFacts?.length"
      />

      <TextEditorCategory
        id="decisionReasons"
        v-bind="{ registerTextEditorRef }"
        v-model="decisionReasons"
        data-testid="decisionReasons"
        :label="longTextLabels.decisionReasons"
        :should-show-button="!decision?.longTexts?.decisionReasons?.length"
      />
    </div>
  </div>
</template>
