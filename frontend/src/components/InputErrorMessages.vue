<script setup lang="ts">
import IconErrorOutline from "~icons/ic/baseline-error-outline"

interface Props {
  errorMessage?: string
}

const props = defineProps<Props>()
</script>
<template>
  <div
    v-if="errorMessage"
    class="mb-10 flex flex-row items-center text-red-800"
    role="alert"
  >
    <IconErrorOutline />
    <p class="ris-label2-regular m-4">
      {{ props.errorMessage }}
    </p>
  </div>
</template>
