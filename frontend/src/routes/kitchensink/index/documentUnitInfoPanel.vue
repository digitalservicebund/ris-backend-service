<script lang="ts" setup>
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import IconPublished from "~icons/ic/outline-campaign"

const firstRowInfos = [
  {
    label: "Veröffentlicht",
    icon: IconPublished,
    color: "black",
    backgroundColor: "bg-green-300",
  },
  {
    label: "Dokumentationsstelle",
    value: "BVerfG",
  },
]

const secondRowInfos = [
  { label: "Aktenzeichen", value: "AZ-98230234" },
  { label: "Entscheidungsdatum", value: "01.01.2000" },
  { label: "Gericht", value: "BVerfG" },
]
</script>

<template>
  <KitchensinkPage name="Document unit info panel">
    <DocumentUnitInfoPanel
      :first-row="firstRowInfos"
      heading="XXRE202300002"
      :second-row="secondRowInfos"
    />
  </KitchensinkPage>
</template>
