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
  props.references?.filter((reference) => reference.primaryReference),
)
const secondaryReferences = computed(() =>
  props.references?.filter((reference) => !reference.primaryReference),
)
</script>

<template>
  <PreviewRow
    v-if="primaryReferences && primaryReferences.length > 0"
    data-testid="primary-references-preview"
  >
    <PreviewCategory>Primäre Fundstellen</PreviewCategory>
    <PreviewContent>
      <div v-for="item in primaryReferences" :key="item.id">
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
      <div v-for="item in secondaryReferences" :key="item.id">
        {{ item.renderDecision }}
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
