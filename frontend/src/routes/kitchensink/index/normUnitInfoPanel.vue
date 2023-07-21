<script lang="ts" setup>
import NormUnitInfoPanel from "@/components/NormUnitInfoPanel.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const propertyInfos = [
  { label: "Fundstelle", value: "BGBl I S. 3" },
  { label: "FNA", value: "315-12" },
  { label: "Inkrafttretedatum", value: "01.01.2007" },
]
</script>

<template>
  <KitchensinkPage name="Norm unit info panel">
    <NormUnitInfoPanel heading="Tierarznei" :property-infos="propertyInfos" />
  </KitchensinkPage>
</template>
