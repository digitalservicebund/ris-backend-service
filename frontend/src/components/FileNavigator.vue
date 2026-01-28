<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PrevNextNavigation from "@/components/PrevNextNavigation.vue"
import { Attachment } from "@/domain/attachment"

const props = defineProps<{
  attachments: Attachment[]
  currentIndex: number
}>()

const emit = defineEmits<(event: "select", index: number) => void>()
</script>

<template>
  <FlexContainer
    v-if="attachments.length > 1"
    class="float-end items-center gap-4"
  >
    <FlexItem class="ris-label1-bold self-center">
      {{ props.attachments[currentIndex]?.name }}
    </FlexItem>
    <PrevNextNavigation
      :current-index="props.currentIndex"
      :list="props.attachments"
      @select="(index) => emit('select', index)"
    />
  </FlexContainer>
</template>
