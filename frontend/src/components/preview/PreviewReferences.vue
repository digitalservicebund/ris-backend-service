<script setup lang="ts">
import { computed } from "vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  references: Reference[] | undefined
}>()

const primaryReferences = computed(() =>
  props.references?.filter(
    (reference) => reference.legalPeriodical?.primaryReference,
  ),
)
const secondaryReferences = computed(() =>
  props.references?.filter(
    (reference) => !reference.legalPeriodical?.primaryReference,
  ),
)
</script>

<template>
  <PreviewRow
    v-if="primaryReferences && primaryReferences.length > 0"
    data-testid="primary-references-preview"
  >
    <PreviewCategory>Primäre Fundstellen</PreviewCategory>
    <PreviewContent>
      <div v-for="item in primaryReferences" :key="item.legalPeriodical?.uuid">
        {{ item.renderDecision }}
      </div>
    </PreviewContent>
  </PreviewRow>
  <PreviewRow
    v-if="secondaryReferences && secondaryReferences.length > 0"
    data-testid="secondary-references-preview"
  >
    <PreviewCategory>Sekundäre Fundstellen</PreviewCategory>
    <PreviewContent>
      <div
        v-for="item in secondaryReferences"
        :key="item.legalPeriodical?.uuid"
      >
        {{ item.renderDecision }}
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
