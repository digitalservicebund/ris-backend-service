<script lang="ts" setup>
import ObjectValue from "@/domain/objectValue"
import Payment from "~icons/ic/round-payments"

defineProps<{
  data: ObjectValue
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center" data-testId="object-value-summary">
      <Payment class="mr-8" />
      {{ data.renderSummary }}
    </div>
  </div>
</template>
ic:round-payments
