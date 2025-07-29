<script lang="ts" setup>
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import { Decision } from "@/domain/decision"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
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

const docUnitProcessSteps: DocumentationUnitProcessStep[] = [
  {
    id: "a-id",
    userId: "user1-id",
    createdAt: new Date(),
    processStep: { uuid: "neu-id", name: "Neu", abbreviation: "N" },
  },
  {
    id: "b-id",
    userId: "user1-id",
    createdAt: new Date(),
    processStep: {
      uuid: "blockiert-id",
      name: "Blockiert",
      abbreviation: "B",
    },
  },
  {
    id: "c-id",
    userId: "user2-id",
    createdAt: new Date(),
    processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
  },
]
</script>

<template>
  <KitchensinkPage name="Document unit info panel">
    <DocumentUnitInfoPanel
      :document-unit="documentUnit"
      :process-steps="docUnitProcessSteps"
    />
  </KitchensinkPage>
</template>
