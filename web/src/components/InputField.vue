<script lang="ts" setup>
interface Props {
  id: string
  label: string
  iconName?: string
  errorMessage?: string
  required?: boolean
}

defineProps<Props>()
</script>

<template>
  <div class="flex flex-col">
    <label class="flex gap-4 items-center label-02-regular mb-2" :for="id">
      <span class="material-icons">{{ iconName }}</span>
      {{ label }}
      <span v-if="!!required">*</span>
    </label>

    <slot :id="id" />

    <div class="h-16 text-16 text-red-800">
      {{ errorMessage }}
    </div>
  </div>
</template>
