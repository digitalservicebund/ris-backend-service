<script setup lang="ts">
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import Reference from "@/domain/reference"

defineProps<{
  literatureReferences: Reference[] | undefined
}>()
</script>

<template>
  <PreviewRow
    v-if="literatureReferences && literatureReferences.length > 0"
    data-testid="literature-references-preview"
  >
    <PreviewCategory>Literaturfundstellen</PreviewCategory>

    <PreviewContent>
      <div v-for="item in literatureReferences" :key="item.id">
        {{ item.renderDecision }}
      </div>
    </PreviewContent>
  </PreviewRow>
</template>
