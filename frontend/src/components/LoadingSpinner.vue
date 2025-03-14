<script setup lang="ts">
const props = defineProps<{
  size?: "extra-small" | "small" | "medium"
}>()

const sizeClasses = {
  "extra-small": "h-16 w-16",
  small: "h-24 w-24",
  medium: "h-32 w-32",
}
</script>

<template>
  <output
    aria-label="Ladestatus"
    class="inline-block animate-spin rounded-full border-[3px] border-solid border-blue-900 border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
    :class="sizeClasses[props.size || 'medium']"
  ></output>
</template>
