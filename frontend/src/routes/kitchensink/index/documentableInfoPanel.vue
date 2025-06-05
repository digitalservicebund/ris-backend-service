<script lang="ts" setup>
import DocumentableInfoPanel from "@/components/DocumentableInfoPanel.vue"
import DocumentUnit from "@/domain/documentUnit"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const documentUnit = new DocumentUnit("foo", {
  documentNumber: "XXRE202300002",
  coreData: {
    court: {
      type: "AG",
      location: "Test",
      label: "AG Test",
    },
  },
  shortTexts: {},
  longTexts: {},
  previousDecisions: undefined,
  ensuingDecisions: undefined,
  contentRelatedIndexing: {},
})
</script>

<template>
  <KitchensinkPage name="Document unit info panel">
    <DocumentableInfoPanel :document="documentUnit" />
  </KitchensinkPage>
</template>
