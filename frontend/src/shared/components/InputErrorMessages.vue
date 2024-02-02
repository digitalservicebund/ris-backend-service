<script setup lang="ts">
import IconErrorOutline from "~icons/ic/baseline-error-outline"

interface Props {
  errorMessage?: string
}

const props = defineProps<Props>()
</script>
<template>
  <div v-if="errorMessage" class="mb-10 flex flex-row items-center">
    <IconErrorOutline class="leading-default text-gray-900" />
    <p class="ds-label-02-reg m-4 text-gray-900">
      {{ props.errorMessage }}
    </p>
  </div>
</template>
