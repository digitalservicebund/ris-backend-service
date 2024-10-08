<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    size?: number
  }>(),
  { size: 32 },
)
</script>

<template>
  <div
    aria-label="Ladestatus"
    class="inline-block animate-spin rounded-full border-[3px] border-solid border-blue-900 border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
    :class="`h-${props.size} w-${props.size}`"
  ></div>
</template>
