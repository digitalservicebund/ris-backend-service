<script lang="ts" setup>
import { computed } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import DocumentUnitTextField from "@/components/DocumentUnitTextField.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import StringsUtil from "@/utils/stringsUtil"

const store = useDocumentUnitStore()

//todo: manage read-only for externals
//todo: manage collapsed/expanded state
const longTexts = computed(() => {
  if (store.documentUnit == undefined) return null
  return store.documentUnit.longTexts
})

const updateTenor = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.tenor = undefined
  } else {
    store.documentUnit!.longTexts.tenor = hasContent(text) ? text : ""
  }
}

const updateReasons = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.reasons = undefined
  } else {
    store.documentUnit!.longTexts.reasons = hasContent(text) ? text : ""
  }
}

const updateCaseFacts = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.caseFacts = undefined
  } else {
    store.documentUnit!.longTexts.caseFacts = hasContent(text) ? text : ""
  }
}

const updateDecisionReasons = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.decisionReasons = undefined
  } else {
    store.documentUnit!.longTexts.decisionReasons = hasContent(text) ? text : ""
  }
}

const updateDissentingOpinion = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.dissentingOpinion = undefined
  } else {
    store.documentUnit!.longTexts.dissentingOpinion = hasContent(text)
      ? text
      : ""
  }
}

const updateOutline = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.outline = undefined
  } else {
    store.documentUnit!.longTexts.outline = hasContent(text) ? text : ""
  }
}

const updateOtherLongText = async (text?: string) => {
  if (StringsUtil.isEmpty(text)) {
    store.documentUnit!.longTexts.otherLongText = undefined
  } else {
    store.documentUnit!.longTexts.otherLongText = hasContent(text) ? text : ""
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
  <h2 class="ds-heading-03-bold mb-16 mt-24">Langtexte</h2>
  <div class="flex flex-col gap-24">
    <CategoryWrapper label="Tenor" should-show-button>
      <DocumentUnitTextField
        label="Tenor"
        name="tenor"
        :value="longTexts?.tenor"
        @update-value="updateTenor"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Gründe" should-show-button>
      <DocumentUnitTextField
        label="Gründe"
        name="reasons"
        :value="longTexts?.reasons"
        @update-value="updateReasons"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Tatbestand" should-show-button>
      <DocumentUnitTextField
        label="Tatbestand"
        name="caseFacts"
        :value="longTexts?.caseFacts"
        @update-value="updateCaseFacts"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Entscheidungsgründe" should-show-button>
      <DocumentUnitTextField
        collapsed-by-default
        label="Entscheidungsgründe"
        name="decisionReasons"
        :value="longTexts?.decisionReasons"
        @update-value="updateDecisionReasons"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Sonstiger Langtext" should-show-button>
      <DocumentUnitTextField
        label="Sonstiger Langtext"
        name="otherLongText"
        :value="longTexts?.otherLongText"
        @update-value="updateOtherLongText"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Abweichende Meinung" should-show-button>
      <DocumentUnitTextField
        label="Abweichende Meinung"
        name="dissentingOpinion"
        :value="longTexts?.dissentingOpinion"
        @update-value="updateDissentingOpinion"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Gliederung" should-show-button>
      <DocumentUnitTextField
        label="Gliederung"
        name="outline"
        :value="longTexts?.outline"
        @update-value="updateOutline"
      />
    </CategoryWrapper>
  </div>
</template>
