<script lang="ts" setup>
import IncomeTypeSummaryBasic from "@/components/IncomeTypeSummaryBasic.vue"
import IncomeType from "@/domain/incomeType"
import IconOutlinePayments from "~icons/ic/outline-payments"

defineProps<{
  data: IncomeType
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IconOutlinePayments class="mr-8" />
      <div class="ris-label1-regular mr-8">
        <IncomeTypeSummaryBasic :data="data" />
      </div>
    </div>
  </div>
</template>
