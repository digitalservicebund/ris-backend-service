<script setup lang="ts">
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  references: Reference[]
}>()
</script>

<template>
  <PreviewRow>
    <PreviewCategory>Fundstellen</PreviewCategory>
    <PreviewContent>
      <div v-for="item in props.references" :key="item.rank">
        {{ item.legalPeriodical?.abbreviation }} ({{
          item.legalPeriodical?.title
        }}) - {{ item.citation }}
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
