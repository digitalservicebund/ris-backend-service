<script setup lang="ts">
import dayjs from "dayjs"
import BorderNumberLinkView from "@/components/BorderNumberLinkView.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { longTextLabels, LongTexts } from "@/domain/decision"

defineProps<{
  longTexts: LongTexts
}>()

function formatDate(date: string) {
  return dayjs(date, "YYYY-MM-DD", true).format("DD.MM.YYYY")
}
</script>

<template>
  <PreviewRow v-if="longTexts.outline">
    <PreviewCategory>{{ longTextLabels.outline }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewOutline"
        :aria-label="longTextLabels.outline"
        field-size="max"
        preview
        :value="longTexts.outline"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.tenor">
    <PreviewCategory>{{ longTextLabels.tenor }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        :aria-label="longTextLabels.tenor"
        field-size="max"
        preview
        :value="longTexts.tenor"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.reasons">
    <PreviewCategory>{{ longTextLabels.reasons }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewReasons"
        :aria-label="longTextLabels.reasons"
        field-size="max"
        preview
        :value="longTexts.reasons"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.caseFacts">
    <PreviewCategory>{{ longTextLabels.caseFacts }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewCaseFacts"
        :aria-label="longTextLabels.caseFacts"
        field-size="max"
        preview
        :value="longTexts.caseFacts"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.decisionReasons">
    <PreviewCategory>{{ longTextLabels.decisionReasons }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewDecisionReasons"
        :aria-label="longTextLabels.decisionReasons"
        field-size="max"
        preview
        :value="longTexts.decisionReasons"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.otherLongText">
    <PreviewCategory>{{ longTextLabels.otherLongText }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewOtherLongText"
        :aria-label="longTextLabels.otherLongText"
        field-size="max"
        preview
        :value="longTexts.otherLongText"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.dissentingOpinion">
    <PreviewCategory>{{ longTextLabels.dissentingOpinion }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        id="previewDissentingOpinion"
        :aria-label="longTextLabels.dissentingOpinion"
        field-size="max"
        preview
        :value="longTexts.dissentingOpinion"
      />
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.participatingJudges?.length">
    <PreviewCategory>{{ longTextLabels.participatingJudges }}</PreviewCategory>
    <PreviewContent>
      <div
        v-for="participatingJudge in longTexts.participatingJudges"
        :key="participatingJudge.id"
      >
        {{ participatingJudge.renderSummary }}
      </div>
    </PreviewContent>
  </PreviewRow>
  <PreviewRow v-if="longTexts.corrections?.length">
    <PreviewCategory>Berichtigung</PreviewCategory>
    <PreviewContent>
      <div v-for="correction in longTexts.corrections" :key="correction.id">
        <div class="flex flex-row items-center">
          {{ correction.type }}
          <span v-if="correction.description"
            >, {{ correction.description }}</span
          >
          <span v-if="correction.date"
            >, {{ formatDate(correction.date) }}</span
          >
          <span v-if="correction.borderNumbers?.length" class="mx-4">|</span>
          <span v-if="correction.borderNumbers" class="flex flex-row gap-4">
            <BorderNumberLinkView
              v-for="borderNumber in correction.borderNumbers"
              :key="borderNumber"
              :border-number="borderNumber"
            />
          </span>
        </div>
        <TextEditor
          v-if="correction.content"
          aria-label="Vorschau"
          field-size="max"
          preview
          :value="correction.content"
        />
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
