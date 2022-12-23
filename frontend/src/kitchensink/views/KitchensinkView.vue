<script lang="ts" setup>
import { ref } from "vue"
import KitchensinkBody from "@/kitchensink/components/KitchensinkBody.vue"
import KitchensinkHeader from "@/kitchensink/components/KitchensinkHeader.vue"
import { risComponents } from "@/kitchensink/data/risComponents"
import type { risComponent } from "@/kitchensink/types/types"

const components = ref(risComponents)
const selectedComponent = ref(
  !!risComponents ? risComponents[0] : { name: "Error", view: "ErrorView" }
)
const setSelectedComponent = (component: risComponent) => {
  selectedComponent.value = component
}
</script>

<template>
  <div
    id="kitchensink"
    class="absolute bg-white box-border h-full left-0 overflow-y-scroll top-0 w-full"
  >
    <KitchensinkHeader
      :components="components"
      :selected-component="selectedComponent"
      @set-selected-component="setSelectedComponent"
    />
    <KitchensinkBody :selected-component="selectedComponent" />
  </div>
</template>

<style lang="scss" scoped>
#kitchensink {
  scrollbar-width: none;
}
</style>
