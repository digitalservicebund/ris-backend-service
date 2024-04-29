<script setup lang="ts">
import { computed } from "vue"
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { Texts } from "@/domain/documentUnit"
import { texts as textsFields } from "@/fields/caselaw"

const props = defineProps<{
  texts: Texts
  validBorderNumbers: string[]
}>()

const validateBorderNumberLinks = (divElem: HTMLDivElement) => {
  const linkTags = Array.from(
    divElem.getElementsByTagName("border-number-link"),
  )

  linkTags.forEach((linkTag) => {
    const borderNumber = linkTag.getAttribute("nr")
    const hasBorderNumber = borderNumber
      ? props.validBorderNumbers.includes(borderNumber)
      : false
    linkTag.setAttribute("valid", hasBorderNumber.toString())
  })
  return divElem
}

const data = computed(() =>
  textsFields.map((item) => {
    const divElem = document.createElement("div")
    divElem.innerHTML = props.texts[item.name as keyof Texts] ?? ""
    const validatedContent = validateBorderNumberLinks(divElem).innerHTML
    return {
      id: item.name as keyof Texts,
      label: item.label,
      aria: item.label,
      value: validatedContent,
    }
  }),
)
</script>

<template>
  <div class="mx-16 my-16 h-2 w-5/6 bg-blue-600" />
  <TableView class="table w-full table-fixed">
    <tr v-for="item in data" :key="item.id" class="">
      <PreviewLeftCell>{{ item.label }}</PreviewLeftCell>
      <PreviewRightCell
        ><TextEditor
          :id="item.id"
          :aria-label="item.aria"
          field-size="max"
          preview
          :value="item.value"
      /></PreviewRightCell>
    </tr>
  </TableView>
</template>
