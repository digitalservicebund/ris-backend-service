<script setup lang="ts">
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
  let nextIndex: number
  if (props.currentIndex === props.attachments.length - 1) {
    nextIndex = 0
  } else {
    nextIndex = props.currentIndex + 1
  }
  emit("select", nextIndex)
}
const decreaseFileIndex = () => {
  let nextIndex: number
  if (props.currentIndex === 0) {
    nextIndex = props.attachments.length - 1
  } else {
    nextIndex = props.currentIndex - 1
  }
  emit("select", nextIndex)
}
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
      :icon="IcOutlineArrowBack"
      size="small"
      @click="decreaseFileIndex"
    />

    <TextButton
      id="increase"
      aria-label="Nächstes Dokument anzeigen"
      button-type="ghost"
      :icon="IcOutlineArrowForward"
      size="small"
      @click="incrementFileIndex"
    />
  </FlexContainer>
</template>
