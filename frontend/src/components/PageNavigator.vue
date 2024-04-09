<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import File from "@/domain/file"

const props = defineProps<{
  files: File[]
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
    class="space-x-5 float-end m-16 ml-20 items-center"
  >
    <FlexItem class="ds-label-02-bold">
      {{ props.files[currentIndex].name }}
    </FlexItem>
    <button
      id="decrease"
      class="ds-button ds-button-tertiary m-8"
      :disabled="!hasPrevious.valueOf()"
      @click="decreaseFileIndex"
    >
      &lt;
    </button>
    <button
      id="increase"
      class="ds-button ds-button-tertiary"
      :disabled="!hasNext.valueOf()"
      @click="incrementFileIndex"
    >
      &gt;
    </button>
  </FlexContainer>
</template>
