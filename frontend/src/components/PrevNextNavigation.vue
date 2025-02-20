<script setup lang="ts">
import Tooltip from "./Tooltip.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextButton from "@/components/input/TextButton.vue"
import IcOutlineArrowBack from "~icons/ic/outline-arrow-back"
import IcOutlineArrowForward from "~icons/ic/outline-arrow-forward"

const props = defineProps<{
  list: unknown[]
  currentIndex: number
}>()

const emit = defineEmits<(event: "select", index: number) => void>()

const incrementFileIndex = () => {
  let nextIndex: number
  if (props.currentIndex === props.list.length - 1) {
    nextIndex = 0
  } else {
    nextIndex = props.currentIndex + 1
  }
  emit("select", nextIndex)
}
const decreaseFileIndex = () => {
  let nextIndex: number
  if (props.currentIndex === 0) {
    nextIndex = props.list.length - 1
  } else {
    nextIndex = props.currentIndex - 1
  }
  emit("select", nextIndex)
}
</script>

<template>
  <FlexContainer v-if="list.length > 1" class="float-end items-center gap-4">
    <Tooltip text="Zurück">
      <TextButton
        id="decrease"
        aria-label="Vorheriges Dokument anzeigen"
        button-type="ghost"
        :icon="IcOutlineArrowBack"
        size="small"
        @click="decreaseFileIndex"
      />
    </Tooltip>
    <Tooltip text="Weiter">
      <TextButton
        id="increase"
        aria-label="Nächstes Dokument anzeigen"
        button-type="ghost"
        :icon="IcOutlineArrowForward"
        size="small"
        @click="incrementFileIndex"
      />
    </Tooltip>
  </FlexContainer>
</template>
