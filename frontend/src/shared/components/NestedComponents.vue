<script setup lang="ts">
import { ref } from "vue"
import ToggleButton from "@/shared/components/ToggleButton.vue"

interface Props {
  ariaLabel: string
  isOpen?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  isOpen: false,
})

const localIsExpanded = ref(props.isOpen.valueOf())
</script>
<template>
  <div class="flex-column">
    <div class="col-auto">
      <!-- Parent component-->
      <slot />
      <!-- Toggle Button-->
      <ToggleButton
        v-model="localIsExpanded"
        :aria-label="props.ariaLabel"
        class="relative float-end"
      />
    </div>
    <!-- Children components-->
    <div v-show="localIsExpanded">
      <slot class="col-auto" name="children"></slot>
    </div>
  </div>
</template>
