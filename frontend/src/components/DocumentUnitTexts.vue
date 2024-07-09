<script lang="ts" setup>
import { computed } from "vue"
import TextEditor from "../components/input/TextEditor.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useValidBorderNumbers } from "@/composables/useValidBorderNumbers"
import { Texts } from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const data = computed(() => {
  if (store.documentUnit == undefined) return null
  return useValidBorderNumbers(
    store.documentUnit.texts,
    store.documentUnit?.borderNumbers,
  )
})

const handleUpdateValueDocumentUnitTexts = async (
  updatedValue: [keyof Texts, string],
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  store.documentUnit!.texts[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}
</script>

<template>
  <div class="core-data mb-16 flex flex-col gap-24 bg-white p-32">
    <h2 class="ds-heading-03-bold">Kurz- & Langtexte</h2>

    <div class="flex flex-col gap-24">
      <div v-for="item in data" :key="item.id" class="">
        <label class="ds-label-02-reg mb-4" :for="item.id">{{
          item.label
        }}</label>

        <TextEditor
          v-if="item.fieldType == TextAreaInput"
          :id="item.id"
          :aria-label="item.aria"
          class="ml-2 pl-2 outline outline-2 outline-blue-900"
          editable
          :field-size="item.fieldSize"
          :value="item.value"
          @update-value="handleUpdateValueDocumentUnitTexts"
        />

        <TextInput
          v-if="item.fieldType == TextInput"
          :id="item.id"
          :aria-label="item.aria"
          :model-value="item.value"
          size="medium"
          @update:model-value="handleUpdateValueDocumentUnitTexts"
        />
      </div>
    </div>
  </div>
</template>
