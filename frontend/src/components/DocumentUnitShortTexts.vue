<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import DocumentUnitTextField from "@/components/DocumentUnitTextField.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import StringsUtil from "@/utils/stringsUtil"

const store = useDocumentUnitStore()

// const shortTexts = computed(() => {
//   if (store.documentUnit == undefined) return null
//   return useShortTextsWithValidBorderNumberLinks(
//     store.documentUnit.shortTexts,
//     store.documentUnit?.borderNumbers,
//   )
// })
//todo: validate border number links
//todo: manage read-only for externals
//todo: manage collapsed/expanded state
const shortTexts = computed(() => {
  if (store.documentUnit == undefined) return null
  return store.documentUnit.shortTexts
})

const updateDecisionName = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.shortTexts.decisionName = undefined
  } else {
    store.documentUnit!.shortTexts.decisionName = hasContent(text) ? text : ""
  }
}

const decisionName = computed({
  get: () => store.documentUnit?.shortTexts.decisionName,
  set: (newValue) => {
    if (StringsUtil.isEmpty(newValue)) {
      store.documentUnit!.shortTexts.decisionName = undefined
    } else {
      store.documentUnit!.shortTexts.decisionName = hasContent(newValue)
        ? newValue
        : ""
    }
  },
})

const hasDecisionName = ref<boolean>(
  store.documentUnit?.shortTexts?.decisionName
    ? store.documentUnit?.shortTexts?.decisionName?.length > 0
    : false,
)

const updateHeadline = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.shortTexts.headline = undefined
  } else {
    store.documentUnit!.shortTexts.headline = hasContent(text) ? text : ""
  }
}

const updateGuidingPrinciple = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.shortTexts.guidingPrinciple = undefined
  } else {
    store.documentUnit!.shortTexts.guidingPrinciple = hasContent(text)
      ? text
      : ""
  }
}

const updateHeadnote = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.shortTexts.headnote = undefined
  } else {
    store.documentUnit!.shortTexts.headnote = hasContent(text) ? text : ""
  }
}

const updateOtherHeadnote = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.shortTexts.otherHeadnote = undefined
  } else {
    store.documentUnit!.shortTexts.otherHeadnote = hasContent(text) ? text : ""
  }
}

function hasContent(text?: string) {
  const divElem = document.createElement("div")
  if (text == undefined) text = ""
  divElem.innerHTML = text
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  return hasInnerText || hasImgElem || hasTable
}
</script>

<template>
  <h2 class="ds-heading-03-bold mb-16">Kurztexte</h2>
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
          aria-label="decisionName"
          :model-value="decisionName"
          size="medium"
          @update:model-value="updateDecisionName"
        />
      </div>
    </CategoryWrapper>

    <CategoryWrapper label="Titelzeile" should-show-button>
      <DocumentUnitTextField
        field-size="small"
        label="Titelzeile"
        name="headline"
        :value="shortTexts?.headline"
        @update-value="updateHeadline"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Titelzeile" should-show-button>
      <DocumentUnitTextField
        field-size="small"
        label="Titelzeile"
        name="headline"
        :value="shortTexts?.headline"
        @update-value="updateHeadline"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Leitsatz" should-show-button>
      <DocumentUnitTextField
        field-size="medium"
        label="Leitsatz"
        name="guidingPrinciple"
        :value="shortTexts?.guidingPrinciple"
        @update-value="updateGuidingPrinciple"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Orientierungssatz" should-show-button>
      <DocumentUnitTextField
        field-size="medium"
        label="Orientierungssatz"
        name="headnote"
        :value="shortTexts?.headnote"
        @update-value="updateHeadnote"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Sonstiger Orientierungssatz" should-show-button>
      <DocumentUnitTextField
        collapsed-by-default
        field-size="medium"
        label="Sonstiger Orientierungssatz"
        name="otherHeadnote"
        :value="shortTexts?.otherHeadnote"
        @update-value="updateOtherHeadnote"
      />
    </CategoryWrapper>
  </div>
</template>
