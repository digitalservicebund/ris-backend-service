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
      :aria-label="id"
      class="flex gap-4 items-center label-03-reg mb-2 text-gray-900"
      :for="id"
    >
      {{ label }}
      <span v-if="!!required">*</span>
    </label>

    <slot :id="id" />

    <div class="h-16 label-03-reg text-red-800">
      {{ errorMessage }}
    </div>
  </div>
</template>
