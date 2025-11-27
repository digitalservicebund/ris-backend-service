<script lang="ts" setup>
import IncomeType, { TypeOfIncomeLabels } from "@/domain/incomeType"
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
        <span v-if="data?.typeOfIncome">{{
          TypeOfIncomeLabels[data.typeOfIncome]
        }}</span>
        <span v-if="data?.typeOfIncome && data?.terminology">{{ ", " }}</span>
        <span>{{ data?.terminology }}</span>
      </div>
    </div>
  </div>
</template>
