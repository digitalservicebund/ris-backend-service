<script setup lang="ts">
import { computed } from "vue"
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { useLongTexts } from "@/composables/useShortTextsWithValidBorderNumberLinks"
import { LongTexts } from "@/domain/documentUnit"

const props = defineProps<{
  texts: LongTexts
}>()

const data = computed(() => useLongTexts(props.texts).filter((it) => it.value))
</script>

<template>
  <PreviewRow v-for="item in data" :key="item.id">
    <PreviewCategory>{{ item.label }}</PreviewCategory>
    <PreviewContent>
      <TextEditor
        :id="item.id"
        :aria-label="item.aria"
        field-size="max"
        preview
        :value="item.value"
      />
    </PreviewContent>
  </PreviewRow>
</template>
