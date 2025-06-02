<script lang="ts" setup>
import DocumentInfoPanel from "@/components/DocumentInfoPanel.vue"
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
    <DocumentInfoPanel :document="documentUnit" />
  </KitchensinkPage>
</template>
