<script lang="ts" setup>
import { type Component, computed } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { longTextLabels } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()

const tenor = computed({
  get: () =>
    store.documentUnit?.longTexts.tenor
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.tenor,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.tenor =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const reasons = computed({
  get: () =>
    store.documentUnit?.longTexts.reasons
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.reasons,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.reasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const caseFacts = computed({
  get: () =>
    store.documentUnit?.longTexts.caseFacts
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.caseFacts,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.caseFacts =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const decisionReasons = computed({
  get: () =>
    store.documentUnit?.longTexts.decisionReasons
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.decisionReasons,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.decisionReasons =
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
        :should-show-button="!store.documentUnit?.longTexts?.tenor?.length"
      />

      <TextEditorCategory
        id="reasons"
        v-bind="{ registerTextEditorRef }"
        v-model="reasons"
        data-testid="reasons"
        :label="longTextLabels.reasons"
        :should-show-button="!store.documentUnit?.longTexts?.reasons?.length"
      />

      <TextEditorCategory
        id="caseFacts"
        v-bind="{ registerTextEditorRef }"
        v-model="caseFacts"
        data-testid="caseFacts"
        :label="longTextLabels.caseFacts"
        :should-show-button="!store.documentUnit?.longTexts?.caseFacts?.length"
      />

      <TextEditorCategory
        id="decisionReasons"
        v-bind="{ registerTextEditorRef }"
        v-model="decisionReasons"
        data-testid="decisionReasons"
        :label="longTextLabels.decisionReasons"
        :should-show-button="
          !store.documentUnit?.longTexts?.decisionReasons?.length
        "
      />
    </div>
  </div>
</template>
