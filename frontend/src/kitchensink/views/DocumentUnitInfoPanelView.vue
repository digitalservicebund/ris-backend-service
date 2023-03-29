<script lang="ts" setup>
import DocumentUnit from "@/domain/documentUnit"
import DocumentUnitInfoPanel from "@/shared/components/DocumentUnitInfoPanel.vue"

const documentUnit = new DocumentUnit("123", {
  documentNumber: "KORE202200466",
  coreData: {
    fileNumbers: ["AZ-98230234"],
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
