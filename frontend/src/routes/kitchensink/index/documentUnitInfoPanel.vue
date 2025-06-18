<script lang="ts" setup>
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import { Decision } from "@/domain/decision"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const documentUnit = new Decision("foo", {
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
    <DocumentUnitInfoPanel :document-unit="documentUnit" />
  </KitchensinkPage>
</template>
