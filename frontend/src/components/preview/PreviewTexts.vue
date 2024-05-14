<script setup lang="ts">
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { useValidBorderNumbers } from "@/composables/useValidBorderNumbers"
import { Texts } from "@/domain/documentUnit"

const props = defineProps<{
  texts: Texts
  validBorderNumbers: string[]
}>()

const data = useValidBorderNumbers(
  props.texts,
  props.validBorderNumbers,
).filter((it) => it.value)
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
