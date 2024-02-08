<script setup lang="ts">
import { ref, onMounted } from "vue"
import ToggleButton from "@/shared/components/ToggleButton.vue"

interface Props {
  ariaLabel: string
  isOpen?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  isOpen: false,
})

const localIsExpanded = ref(props.isOpen)

onMounted(() => {
  localIsExpanded.value = props.isOpen
})
</script>
<template>
  <div class="flex flex-col">
    <div class="relative flex flex-row">
      <!-- Parent component-->
      <slot />
      <!-- Toggle Button-->
      <ToggleButton
        v-model="localIsExpanded"
        :aria-label="props.ariaLabel"
        class="absolute float-end"
      />
    </div>
    <!-- Children component-->
    <div v-show="localIsExpanded" class="mt-24">
      <slot name="children"></slot>
    </div>
  </div>
</template>
