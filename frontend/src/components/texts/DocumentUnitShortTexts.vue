<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import TextInput from "@/components/input/TextInput.vue"
import DocumentUnitTextField from "@/components/texts/DocumentUnitTextField.vue"
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
      <CategoryWrapper
        label="Entscheidungsname"
        :should-show-button="!hasDecisionName"
      >
        <div class="flex flex-col">
          <label class="ds-label-02-reg mb-4" for="'decisionName'">
            Entscheidungsname
          </label>

          <TextInput
            id="decisionName"
            v-model="decisionName"
            aria-label="decisionName"
            size="medium"
          />
        </div>
      </CategoryWrapper>

      <CategoryWrapper label="Titelzeile" :should-show-button="!hasHeadline">
        <DocumentUnitTextField
          id="headline"
          v-model="headline"
          editable
          field-size="small"
          label="Titelzeile"
        />
      </CategoryWrapper>

      <CategoryWrapper
        label="Leitsatz"
        :should-show-button="!hasGuidingPrinciple"
      >
        <DocumentUnitTextField
          id="guidingPrinciple"
          v-model="guidingPrinciple"
          editable
          label="Leitsatz"
        />
      </CategoryWrapper>

      <CategoryWrapper
        label="Orientierungssatz"
        :should-show-button="!hasHeadnote"
      >
        <DocumentUnitTextField
          id="headnote"
          v-model="headnote"
          editable
          label="Orientierungssatz"
        />
      </CategoryWrapper>

      <CategoryWrapper
        label="Sonstiger Orientierungssatz"
        :should-show-button="!hasOtherHeadnote"
      >
        <DocumentUnitTextField
          id="otherHeadnote"
          v-model="otherHeadnote"
          editable
          label="Sonstiger Orientierungssatz"
        />
      </CategoryWrapper>
    </div>
  </div>
</template>
