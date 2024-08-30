<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitTextField from "@/components/DocumentUnitTextField.vue"
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
const shortTexts = computed(() => {
  if (store.documentUnit == undefined) return null
  return store.documentUnit.shortTexts
})

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
  <h2 class="ds-heading-03-bold">Kurztexte</h2>
  <div class="flex flex-col gap-24">
    <!--add decision name -->

    <DocumentUnitTextField
      field-size="small"
      label="Titelzeile"
      name="headline"
      :value="shortTexts?.headline"
      @update-value="updateHeadline"
    />

    <DocumentUnitTextField
      field-size="medium"
      label="Leitsatz"
      name="guidingPrinciple"
      :value="shortTexts?.guidingPrinciple"
      @update-value="updateGuidingPrinciple"
    />

    <DocumentUnitTextField
      field-size="medium"
      label="Orientierungssatz"
      name="headnote"
      :value="shortTexts?.headnote"
      @update-value="updateHeadnote"
    />

    <DocumentUnitTextField
      collapsed-by-default
      field-size="medium"
      label="Sonstiger Orientierungssatz"
      name="otherHeadnote"
      :value="shortTexts?.otherHeadnote"
      @update-value="updateOtherHeadnote"
    />
  </div>
</template>
