<script setup lang="ts">
import { computed } from "vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  references: Reference[]
}>()

const primaryReferences = computed(() =>
  props.references.filter((reference) => reference.primaryReference),
)
const secondaryReferences = computed(() =>
  props.references.filter((reference) => !reference.primaryReference),
)
</script>

<template>
  <PreviewRow
    v-if="props.references && props.references.length > 0"
    data-testid="references-preview"
  >
    <PreviewCategory>Fundstellen</PreviewCategory>
    <PreviewContent>
      <div v-if="primaryReferences.length > 0">
        <span class="ds-body-01-bold">Amtliche Fundstellen</span>

        <div v-for="item in primaryReferences" :key="item.legalPeriodicalId">
          {{ item.legalPeriodicalAbbreviation }}
          - {{ item.citation }}
        </div>
      </div>
      <div
        v-if="primaryReferences.length > 0 && secondaryReferences.length > 0"
      >
        <br />
      </div>
      <div v-if="secondaryReferences.length > 0" class="pt-4">
        <span class="ds-body-01-bold">Sekundäre Fundstellen</span>
        <div v-for="item in secondaryReferences" :key="item.legalPeriodicalId">
          {{ item.legalPeriodicalAbbreviation }}
          - {{ item.citation }}
        </div>
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
