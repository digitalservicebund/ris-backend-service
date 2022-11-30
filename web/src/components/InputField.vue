<script lang="ts" setup>
interface Props {
  id: string
  label?: string
  errorMessage?: string
  required?: boolean
}

defineProps<Props>()
</script>

<template>
  <div class="flex flex-col flex-start">
    <label
      v-if="label"
      class="flex gap-4 items-center label-03-regular mb-2 text-gray-900"
      :for="id"
    >
      {{ label }}
      <span v-if="!!required">*</span>
    </label>

    <slot :id="id" />

    <div class="h-16 text-16 text-red-800">
      {{ errorMessage }}
    </div>
  </div>
</template>
