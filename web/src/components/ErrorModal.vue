<script lang="ts" setup>
defineProps<{ title: string; description: string }>()
</script>

<template>
  <div
    class="bg-red-200 border-l-8 border-l-red-800 flex gap-8 px-4 py-4 w-full"
  >
    <span class="material-icons pt-1 text-red-800">error</span>

    <div class="flex flex-col">
      <span class="font-bold">{{ title }}</span>
      {{ description }}
    </div>
  </div>
</template>
