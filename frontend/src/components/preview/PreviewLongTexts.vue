<script setup lang="ts">
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { LongTexts } from "@/domain/documentUnit"

defineProps<{
  longTexts: LongTexts
}>()
</script>

<template>
  <PreviewRow v-if="longTexts.outline">
    <PreviewCategory>Gliederung</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewOutline"
        aria-label="Gliederung"
        field-size="max"
        preview
        :value="longTexts.outline"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.tenor">
    <PreviewCategory>Tenor</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewTenor"
        aria-label="Tenor"
        field-size="max"
        preview
        :value="longTexts.tenor"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.reasons">
    <PreviewCategory>Gründe</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewReasons"
        aria-label="Gründe"
        field-size="max"
        preview
        :value="longTexts.reasons"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.caseFacts">
    <PreviewCategory>Tatbestand</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewCaseFacts"
        aria-label="Tatbestand"
        field-size="max"
        preview
        :value="longTexts.caseFacts"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.decisionReasons">
    <PreviewCategory>Entscheidungsgründe</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewDecisionReasons"
        aria-label="Entscheidungsgründe"
        field-size="max"
        preview
        :value="longTexts.decisionReasons"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.otherLongText">
    <PreviewCategory>Sonstiger Langtext</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewOtherLongText"
        aria-label="Sonstiger Langtext"
        field-size="max"
        preview
        :value="longTexts.otherLongText"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.dissentingOpinion">
    <PreviewCategory>Abweichende Meinung</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewDissentingOpinion"
        aria-label="Abweichende Meinung"
        field-size="max"
        preview
        :value="longTexts.dissentingOpinion"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.participatingJudges?.length">
    <PreviewCategory>Mitwirkende Richter</PreviewCategory>
    <PreviewContent>
      <div
        v-for="participatingJudge in longTexts.participatingJudges"
        :key="participatingJudge.id"
      >
        {{ participatingJudge.renderDecision }}
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
