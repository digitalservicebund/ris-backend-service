<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextButton from "@/components/input/TextButton.vue"
import Attachment from "@/domain/attachment"
import IcOutlineArrowBack from "~icons/ic/outline-arrow-back"
import IcOutlineArrowForward from "~icons/ic/outline-arrow-forward"

const props = defineProps<{
  files: Attachment[]
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
  return props.currentIndex.valueOf() < props.files.length - 1
})

const hasPrevious = computed(() => {
  return props.currentIndex.valueOf() > 0
})
</script>

<template>
  <FlexContainer
    v-if="files.length > 1"
    class="float-end m-16 ml-20 items-center space-x-8 px-8"
  >
    <FlexItem class="ds-label-02-bold">
      {{ props.files[currentIndex].name }}
    </FlexItem>
    <TextButton
      id="decrease"
      aria-label="Vorheriges Dokument anzeigen"
      button-type="tertiary"
      :disabled="!hasPrevious.valueOf()"
      :icon="IcOutlineArrowBack"
      @click="decreaseFileIndex"
    />

    <TextButton
      id="increase"
      aria-label="Nächstes Dokument anzeigen"
      button-type="tertiary"
      :disabled="!hasNext.valueOf()"
      :icon="IcOutlineArrowForward"
      @click="incrementFileIndex"
    />
  </FlexContainer>
</template>
