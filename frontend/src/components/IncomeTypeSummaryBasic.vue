<script lang="ts" setup>
import IncomeType, { TypeOfIncomeLabels } from "@/domain/incomeType"

defineProps<{
  data: IncomeType
}>()
</script>

<template>
  <span v-if="data?.typeOfIncome">{{
    TypeOfIncomeLabels[data.typeOfIncome]
  }}</span>
  <span v-if="data?.typeOfIncome && data?.terminology">{{ ", " }}</span>
  <span>{{ data?.terminology }}</span>
</template>
