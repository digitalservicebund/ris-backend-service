<script lang="ts" setup>
import Reference from "@/domain/reference"

defineProps<{
  data: Reference
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <div class="ds-label-01-reg mr-8">{{ data?.renderDecision }}</div>
    </div>
  </div>
</template>
