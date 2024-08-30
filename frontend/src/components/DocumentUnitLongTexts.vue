<script lang="ts" setup>
import { computed } from "vue"
import TextEditor from "../components/input/TextEditor.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useExternalUser } from "@/composables/useExternalUser"
import {
  useLongTexts,
  useShortTextsWithValidBorderNumberLinks,
} from "@/composables/useShortTextsWithValidBorderNumberLinks"
import { LongTexts, ShortTexts } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import StringsUtil from "@/utils/stringsUtil"
import TextButton from "@/components/input/TextButton.vue"
import IconAdd from "~icons/material-symbols/add"
import { longTextFields } from "@/fields/caselaw"

const store = useDocumentUnitStore()
const isExternalUser = useExternalUser()

function isReadOnly(item: { name: string }): boolean {
  switch (item.name) {
    case "tenor":
    case "reasons":
    case "caseFacts":
    case "decisionReasons":
    case "dissentingOpinion":
    case "otherLongText":
      return isExternalUser
    default:
      return false
  }
}

const longTexts = computed(() => {
  if (store.documentUnit == undefined) return null
  return longTextFields.map((item) => {
    const divElem = document.createElement("div")
    divElem.innerHTML =
      store.documentUnit?.longTexts[item.name as keyof LongTexts] ?? ""
    return {
      id: item.name as keyof LongTexts,
      name: item.name,
      label: item.label,
      aria: item.label,
      value: divElem.innerHTML,
      collapsed: divElem.innerHTML.length > 0 || item.collapsedByDefault,
      fieldType: item.fieldType,
      fieldSize: item.fieldSize,
    }
  })
})

const updateValueByTextIdLong = async (
  id: keyof LongTexts,
  updatedText?: string,
) => {
  if (StringsUtil.isEmpty(updatedText)) {
    store.documentUnit!.longTexts[id] = undefined
  } else {
    const divElem = document.createElement("div")
    if (updatedText == undefined) updatedText = ""
    divElem.innerHTML = updatedText
    const hasImgElem = divElem.getElementsByTagName("img").length > 0
    const hasTable = divElem.getElementsByTagName("table").length > 0
    const hasInnerText = divElem.innerText.length > 0
    store.documentUnit!.longTexts[id] =
      hasInnerText || hasImgElem || hasTable ? updatedText : ""
  }
}
</script>

<template>
  <h2 class="ds-heading-03-bold">Langtexte</h2>
  <div class="flex flex-col gap-24">
    <div v-for="item in longTexts" :key="item.id" class="">
      <label class="ds-label-02-reg mb-4" :for="item.id">{{
        item.label
      }}</label>

      <TextEditor
        v-if="item.fieldType == TextAreaInput"
        :id="item.id"
        :aria-label="item.aria"
        class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
        :editable="!isReadOnly(item)"
        :field-size="item.fieldSize"
        :value="item.value"
        @update-value="updateValueByTextIdLong(item.id, $event)"
      />
    </div>
  </div>
</template>
