<script setup lang="ts">
import Button from "primevue/button"
import FlexContainer from "@/components/FlexContainer.vue"
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
    <Button
      id="decrease"
      v-tooltip.bottom="'Zurück'"
      aria-label="Vorheriges Dokument anzeigen"
      size="small"
      text
      @click="decreaseFileIndex"
      ><template #icon> <IcOutlineArrowBack /> </template
    ></Button>
    <Button
      id="increase"
      v-tooltip.bottom="'Weiter'"
      aria-label="Nächstes Dokument anzeigen"
      size="small"
      text
      @click="incrementFileIndex"
      ><template #icon> <IcOutlineArrowForward /> </template
    ></Button>
  </FlexContainer>
</template>
