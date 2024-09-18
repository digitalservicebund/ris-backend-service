<script lang="ts" setup>
import { computed, ref } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

const store = useDocumentUnitStore()

const hasTenor = ref<boolean>(
  store.documentUnit?.longTexts?.tenor
    ? store.documentUnit?.longTexts?.tenor?.length > 0
    : false,
)

const hasReasons = ref<boolean>(
  store.documentUnit?.longTexts?.reasons
    ? store.documentUnit?.longTexts?.reasons?.length > 0
    : false,
)

const hasCaseFacts = ref<boolean>(
  store.documentUnit?.longTexts?.caseFacts
    ? store.documentUnit?.longTexts?.caseFacts?.length > 0
    : false,
)

const hasDecisionReasons = ref<boolean>(
  store.documentUnit?.longTexts?.decisionReasons
    ? store.documentUnit?.longTexts?.decisionReasons?.length > 0
    : false,
)

const tenor = computed({
  get: () => store.documentUnit?.longTexts.tenor,
  set: (newValue) => {
    store.documentUnit!.longTexts.tenor =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const reasons = computed({
  get: () => store.documentUnit?.longTexts.reasons,
  set: (newValue) => {
    store.documentUnit!.longTexts.reasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const caseFacts = computed({
  get: () => store.documentUnit?.longTexts.caseFacts,
  set: (newValue) => {
    store.documentUnit!.longTexts.caseFacts =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const decisionReasons = computed({
  get: () => store.documentUnit?.longTexts.decisionReasons,
  set: (newValue) => {
    store.documentUnit!.longTexts.decisionReasons =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Langtexte" class="border-t-1 border-blue-300 pb-24">
    <h2 class="ds-label-01-bold mb-16 mt-24">Langtexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="tenor"
        v-model="tenor"
        label="Tenor"
        :should-show-button="!hasTenor"
      />

      <TextEditorCategory
        id="reasons"
        v-model="reasons"
        label="Gründe"
        :should-show-button="!hasReasons"
      />

      <TextEditorCategory
        id="caseFacts"
        v-model="caseFacts"
        label="Tatbestand"
        :should-show-button="!hasCaseFacts"
      />

      <TextEditorCategory
        id="decisionReasons"
        v-model="decisionReasons"
        label="Entscheidungsgründe"
        :should-show-button="!hasDecisionReasons"
      />
    </div>
  </div>
</template>
