<script lang="ts" setup>
import Abusefee from "@/domain/abuseFee"
import Payment from "~icons/ic/round-payments"

defineProps<{
  data: Abusefee
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center" data-testId="abuse-fee-summary">
      <Payment class="mr-8" />
      {{ data.renderSummary }}
    </div>
  </div>
</template>
