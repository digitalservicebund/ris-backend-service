<script lang="ts" setup>
import type { risComponent } from "@/kitchensink/types/types"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  components: risComponent[]
  selectedComponent: risComponent
}>()

defineEmits<{
  (e: "setSelectedComponent", component: risComponent): void
}>()

const isSelectedComponent = (component: risComponent) => {
  return component.name === props.selectedComponent.name
}
</script>

<template>
  <!-- Header -->
  <div
    class="border-b-4 flex flex-nowrap flex-row items-center justify-center w-full"
  >
    <div
      class="flex flex-col flex-nowrap gap-y-10 justify-start overflow-hidden py-10 relative w-2/3"
    >
      <!-- Title -->
      <h1 class="font-sans text-64">Kitchensink</h1>
      <!-- Navbar -->
      <div
        class="flex flex-row flex-wrap gap-x-20 gap-y-20 justify-start navbar overflow-x-scroll overflow-y-hidden"
      >
        <TextButton
          v-for="(component, index) in components"
          :key="index"
          :button-type="isSelectedComponent(component) ? 'primary' : 'tertiary'"
          :label="component.name"
          @click="$emit('setSelectedComponent', component)"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.navbar {
  scrollbar-width: none;
}
</style>
