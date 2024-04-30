<script setup lang="ts">
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { useValidBorderNumbers } from "@/composables/useValidBorderNumbers"
import { Texts } from "@/domain/documentUnit"

const props = defineProps<{
  texts: Texts
  validBorderNumbers: string[]
}>()

const data = useValidBorderNumbers(
  props.texts,
  props.validBorderNumbers,
).filter((it) => it.value)
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
