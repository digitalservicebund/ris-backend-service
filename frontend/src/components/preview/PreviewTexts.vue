<script setup lang="ts">
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { Texts } from "@/domain/documentUnit"

defineProps<{
  texts: Texts
}>()
</script>

<template>
  <div class="mx-16 my-16 h-2 w-5/6 bg-blue-600" />
  <TableView class="table w-full table-fixed">
    <tr>
      <PreviewLeftCell>Entscheidungsname</PreviewLeftCell>
      <PreviewRightCell
        ><TextEditor field-size="max" preview :value="texts.decisionName"
      /></PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Titelzeile</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.headline" />
      </PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Leitsatz</PreviewLeftCell>
      <PreviewRightCell
        ><TextEditor field-size="max" preview :value="texts.guidingPrinciple"
      /></PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Orientierungssatz</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.headnote" />
      </PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Tenor</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.tenor" />
      </PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Gründe</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.reasons" />
      </PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Tatbestand</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.caseFacts" />
      </PreviewRightCell>
    </tr>
    <tr>
      <PreviewLeftCell>Entscheidungsgründe</PreviewLeftCell>
      <PreviewRightCell>
        <TextEditor field-size="max" preview :value="texts.decisionReasons" />
      </PreviewRightCell>
    </tr>
  </TableView>
</template>
