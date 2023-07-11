<script lang="ts" setup>
import NormUnitInfoPanel from "@/components/NormUnitInfoPanel.vue"

const propertyInfos = [
  { label: "Fundstelle", value: "BGBl I S. 3" },
  { label: "FNA", value: "315-12" },
  { label: "Inkrafttretedatum", value: "01.01.2007" },
]
</script>

<template>
  <NormUnitInfoPanel heading="Tierarznei" :property-infos="propertyInfos">
  </NormUnitInfoPanel>
</template>
