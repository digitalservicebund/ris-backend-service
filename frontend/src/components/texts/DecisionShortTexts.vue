<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { storeToRefs } from "pinia"
import { Component, Ref, computed } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import InputField from "@/components/input/InputField.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
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
const decisionNames = computed({
  get: () => decision.value?.shortTexts.decisionNames ?? [],
  set: (newValue) => {
    decision.value!.shortTexts.decisionNames = newValue
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
      <CategoryWrapper
        :label="shortTextLabels.decisionNames"
        :should-show-button="!decision?.shortTexts?.decisionNames?.length"
      >
        <InputField
          id="decisionNames"
          v-slot="{ id }"
          :label="shortTextLabels.decisionNames"
        >
          <RisChipsInput
            v-model="decisionNames"
            :aria-label="shortTextLabels.decisionNames"
            :data-testid="shortTextLabels.decisionNames"
            :input-id="id"
          />
        </InputField>
      </CategoryWrapper>

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
