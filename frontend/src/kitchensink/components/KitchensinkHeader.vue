<script lang="ts" setup>
import type { risComponent } from "@/kitchensink/types/types"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  components: risComponent[]
  selectedComponent: risComponent
}>()

defineEmits<{
  setSelectedComponent: [component: risComponent]
}>()

const isSelectedComponent = (component: risComponent) => {
  return component.name === props.selectedComponent.name
}
</script>

<template>
  <!-- Header -->
  <div
    class="flex w-full flex-row flex-nowrap items-center justify-center border-b-4 border-b-black"
  >
    <div
      class="relative flex w-2/3 flex-col flex-nowrap justify-start gap-y-10 overflow-hidden py-10"
    >
      <!-- Title -->
      <h1 class="text-64 font-bold">Kitchensink</h1>
      <!-- Navbar -->
      <div
        class="navbar flex flex-row flex-wrap justify-start gap-x-20 gap-y-20 overflow-y-hidden overflow-x-scroll"
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
