<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextButton from "@/components/input/TextButton.vue"
import Attachment from "@/domain/attachment"
import IcOutlineArrowBack from "~icons/ic/outline-arrow-back"
import IcOutlineArrowForward from "~icons/ic/outline-arrow-forward"

const props = defineProps<{
  attachments: Attachment[]
  currentIndex: number
}>()

const emit = defineEmits<(event: "select", index: number) => void>()

const incrementFileIndex = () => {
  if (hasNext.value) {
    emit("select", props.currentIndex.valueOf() + 1)
  }
}
const decreaseFileIndex = () => {
  if (hasPrevious.value) {
    emit("select", props.currentIndex.valueOf() - 1)
  }
}

const hasNext = computed(() => {
  return props.currentIndex.valueOf() < props.attachments.length - 1
})

const hasPrevious = computed(() => {
  return props.currentIndex.valueOf() > 0
})
</script>

<template>
  <FlexContainer v-if="attachments.length > 1" class="float-end items-center">
    <FlexItem class="ds-label-01-bold self-center">
      {{ props.attachments[currentIndex].name }}
    </FlexItem>
    <TextButton
      id="decrease"
      aria-label="Vorheriges Dokument anzeigen"
      button-type="ghost"
      :disabled="!hasPrevious.valueOf()"
      :icon="IcOutlineArrowBack"
      size="small"
      @click="decreaseFileIndex"
    />

    <TextButton
      id="increase"
      aria-label="Nächstes Dokument anzeigen"
      button-type="ghost"
      :disabled="!hasNext.valueOf()"
      :icon="IcOutlineArrowForward"
      size="small"
      @click="incrementFileIndex"
    />
  </FlexContainer>
</template>
