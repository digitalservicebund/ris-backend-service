<script lang="ts" setup>
import { computed } from "vue"
import type { NormComponent } from "@/kitchensink/types/types"
import ErrorView from "@/kitchensink/views/ErrorView.vue"
import IndexView from "@/kitchensink/views/IndexView.vue"
import SaveDocumentUnitButtonView from "@/kitchensink/views/SaveDocumentUnitButtonView.vue"
import TextButtonView from "@/kitchensink/views/TextButtonView.vue"

const props = defineProps<{
  selectedComponent: NormComponent
}>()

const component = computed(() => {
  switch (props.selectedComponent.view) {
    case "IndexView":
      return IndexView
    case "TextButtonView":
      return TextButtonView
    case "SaveDocumentUnitButtonView":
      return SaveDocumentUnitButtonView
    default:
      return ErrorView
  }
})
</script>

<template>
  <div
    class="flex flex-nowrap flex-row items-center justify-center overflow-y-hidden"
  >
    <div
      class="flex flex-col flex-nowrap gap-y-10 justify-start py-10 text-justify w-2/3"
    >
      <!-- Title -->
      <h1 class="font-bold text-32">{{ selectedComponent.name }}</h1>
      <!-- Content -->
      <div class="content-container flex flex-col">
        <component :is="component" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
* {
  scrollbar-width: none;
}
</style>
