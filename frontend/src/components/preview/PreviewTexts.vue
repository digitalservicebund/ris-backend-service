<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
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
  <FlexContainer v-for="item in data" :key="item.id" class="flex-row">
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
  </FlexContainer>
</template>
