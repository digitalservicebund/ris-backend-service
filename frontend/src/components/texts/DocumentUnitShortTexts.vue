<script lang="ts" setup>
import { computed, ref } from "vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import TextInputCategory from "@/components/texts/TextInputCategory.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

const store = useDocumentUnitStore()

const hasDecisionName = ref<boolean>(
  store.documentUnit?.shortTexts?.decisionName
    ? store.documentUnit?.shortTexts?.decisionName?.length > 0
    : false,
)

const hasHeadline = ref<boolean>(
  store.documentUnit?.shortTexts?.headline
    ? store.documentUnit?.shortTexts?.headline?.length > 0
    : false,
)

const hasGuidingPrinciple = ref<boolean>(
  store.documentUnit?.shortTexts?.guidingPrinciple
    ? store.documentUnit?.shortTexts?.guidingPrinciple?.length > 0
    : false,
)

const hasHeadnote = ref<boolean>(
  store.documentUnit?.shortTexts?.headnote
    ? store.documentUnit?.shortTexts?.headnote?.length > 0
    : false,
)

const hasOtherHeadnote = ref<boolean>(
  store.documentUnit?.shortTexts?.otherHeadnote
    ? store.documentUnit?.shortTexts?.otherHeadnote?.length > 0
    : false,
)

const decisionName = computed({
  get: () => store.documentUnit?.shortTexts.decisionName,
  set: (newValue) => {
    store.documentUnit!.shortTexts.decisionName =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headline = computed({
  get: () => store.documentUnit?.shortTexts.headline,
  set: (newValue) => {
    store.documentUnit!.shortTexts.headline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const guidingPrinciple = computed({
  get: () =>
    store.documentUnit?.shortTexts.guidingPrinciple
      ? useValidBorderNumberLinks(
          store.documentUnit?.shortTexts.guidingPrinciple,
          store.documentUnit.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.shortTexts.guidingPrinciple =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const headnote = computed({
  get: () => store.documentUnit?.shortTexts.headnote,
  set: (newValue) => {
    store.documentUnit!.shortTexts.headnote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const otherHeadnote = computed({
  get: () => store.documentUnit?.shortTexts.otherHeadnote,
  set: (newValue) => {
    store.documentUnit!.shortTexts.otherHeadnote =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Kurztexte" class="pb-24">
    <h2 class="ds-label-01-bold mb-16">Kurztexte</h2>
    <div class="flex flex-col gap-24">
      <TextInputCategory
        id="decisionName"
        v-model="decisionName"
        editable
        label="Entscheidungsname"
        :should-show-button="!hasDecisionName"
      />

      <TextEditorCategory
        id="headline"
        v-model="headline"
        editable
        field-size="small"
        label="Titelzeile"
        :should-show-button="!hasHeadline"
      />

      <TextEditorCategory
        id="guidingPrinciple"
        v-model="guidingPrinciple"
        editable
        label="Leitsatz"
        :should-show-button="!hasGuidingPrinciple"
      />

      <TextEditorCategory
        id="headnote"
        v-model="headnote"
        editable
        label="Orientierungssatz"
        :should-show-button="!hasHeadnote"
      />

      <TextEditorCategory
        id="otherHeadnote"
        v-model="otherHeadnote"
        editable
        label="Sonstiger Orientierungssatz"
        :should-show-button="!hasOtherHeadnote"
      />
    </div>
  </div>
</template>
