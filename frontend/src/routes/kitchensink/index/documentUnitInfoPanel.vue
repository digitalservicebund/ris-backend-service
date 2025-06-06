<script lang="ts" setup>
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
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
    <DocumentUnitInfoPanel :document="documentUnit" />
  </KitchensinkPage>
</template>
