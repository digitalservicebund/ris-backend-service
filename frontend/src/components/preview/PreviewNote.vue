<script setup lang="ts">
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import StringsUtil from "@/utils/stringsUtil"

const props = defineProps<{
  note?: string
}>()
</script>

<template>
  <PreviewRow v-if="!StringsUtil.isEmpty(props.note)">
    <PreviewCategory>Notiz</PreviewCategory>
    <PreviewContent>
      {{ props.note }}
    </PreviewContent>
  </PreviewRow>
</template>
