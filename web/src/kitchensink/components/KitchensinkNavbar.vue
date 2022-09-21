<script lang="ts" setup>
import TextButton from "@/components/TextButton.vue"
import type { NormComponent } from "@/kitchensink/types/types"

defineProps<{
  components: NormComponent[]
}>()
</script>

<template>
  <!-- Navbar -->
  <div
    class="flex flex-nowrap flex-row gap-x-8 justify-start navbar overflow-x-scroll overflow-y-hidden"
  >
    <TextButton
      v-for="(component, index) in components"
      :key="index"
      button-type="tertiary"
      :label="component.name"
    />
    <TextButton button-type="tertiary" label="Buttons" />
    <TextButton button-type="tertiary" label="Form" />
    <TextButton button-type="tertiary" label="Spinner" />
    <TextButton button-type="tertiary" label="Help" />
    <TextButton button-type="tertiary" label="Icons" />
    <TextButton button-type="tertiary" label="Email Status" />
  </div>
</template>

<style lang="scss" scoped>
* {
  scrollbar-width: none;
}
</style>
