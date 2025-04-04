<script setup lang="ts">
import { getCategoryLabel } from "./categoryLabels"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PrevNextNavigation from "@/components/PrevNextNavigation.vue"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  matches: Match[]
  currentIndex: number
}>()

const emit = defineEmits<(event: "select", index: number) => void>()
</script>

<template>
  <FlexContainer v-if="matches.length > 1" class="float-end items-center gap-4">
    <FlexItem class="ris-label1-bold self-center">
      {{ getCategoryLabel(props.matches[currentIndex]?.category) }}
    </FlexItem>
    <PrevNextNavigation
      :current-index="props.currentIndex"
      :list="props.matches"
      @select="(index) => emit('select', index)"
    />
  </FlexContainer>
</template>
