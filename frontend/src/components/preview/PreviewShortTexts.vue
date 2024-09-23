<script setup lang="ts">
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { ShortTexts } from "@/domain/documentUnit"

const props = defineProps<{
  shortTexts: ShortTexts
  validBorderNumbers: string[]
}>()

const decisionName = props.shortTexts.decisionName
  ? useValidBorderNumberLinks(
      props.shortTexts.decisionName,
      props.validBorderNumbers,
    )
  : undefined
const headline = props.shortTexts.headline
  ? useValidBorderNumberLinks(
      props.shortTexts.headline,
      props.validBorderNumbers,
    )
  : undefined
const guidingPrinciple = props.shortTexts.guidingPrinciple
  ? useValidBorderNumberLinks(
      props.shortTexts.guidingPrinciple,
      props.validBorderNumbers,
    )
  : undefined
const headnote = props.shortTexts.headnote
  ? useValidBorderNumberLinks(
      props.shortTexts.headnote,
      props.validBorderNumbers,
    )
  : undefined
const otherHeadnote = props.shortTexts.otherHeadnote
  ? useValidBorderNumberLinks(
      props.shortTexts.otherHeadnote,
      props.validBorderNumbers,
    )
  : undefined
</script>

<template>
  <PreviewRow v-if="decisionName">
    <PreviewCategory>Entscheidungsname</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewDecisionName"
        aria-label="Entscheidungsname"
        field-size="max"
        preview
        :value="decisionName"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="headline">
    <PreviewCategory>Titelzeile</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewHeadline"
        aria-label="Titelzeile"
        field-size="max"
        preview
        :value="headline"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="guidingPrinciple">
    <PreviewCategory>Leitsatz</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewGuidingPrinciple"
        aria-label="Leitsatz"
        field-size="max"
        preview
        :value="guidingPrinciple"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="headnote">
    <PreviewCategory>Orientierungssatz</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewHeadnote"
        aria-label="Orientierungssatz"
        field-size="max"
        preview
        :value="headnote"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="otherHeadnote">
    <PreviewCategory>Sonstiger Orientierungssatz</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewItherHeadnote"
        aria-label="Sonstiger Orientierungssatz"
        field-size="max"
        preview
        :value="otherHeadnote"
      />
    </PreviewContent>
  </PreviewRow>
</template>
