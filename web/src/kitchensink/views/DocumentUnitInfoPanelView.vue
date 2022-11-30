<script lang="ts" setup>
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"

const documentUnit = new DocumentUnit("123", {
  documentNumber: "KORE202200466",
  coreData: {
    fileNumber: ["AZ-98230234"],
    court: {
      type: "BVerfG",
      location: "Karlsruhe",
      label: "BVerfG Karlsruhe",
    },
    decisionDate: "1668349205",
  },
})
</script>

<template>
  <DocumentUnitInfoPanel :document-unit="documentUnit"> </DocumentUnitInfoPanel>
</template>
