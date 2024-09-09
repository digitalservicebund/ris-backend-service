<script lang="ts" setup>
const props = defineProps<{
  title?: string
}>()
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
  >
    <h1 v-if="props.title" class="text font-bold">{{ props.title }}</h1>
    <span class="flex-grow"></span>
  </div>
</template>
