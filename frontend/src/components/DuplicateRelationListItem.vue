<script setup lang="ts">
import { storeToRefs } from "pinia"
import { computed, ref } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import DateUtil from "@/utils/dateUtil"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { duplicateRelation } = defineProps<{
  duplicateRelation: DuplicateRelation
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore())

const hasSetStateError = ref(false)

const isIgnored = computed({
  get: () => duplicateRelation.status === DuplicateRelationStatus.IGNORED,
  set: async (shouldBeIgnored) => {
    const newStatus = shouldBeIgnored
      ? DuplicateRelationStatus.IGNORED
      : DuplicateRelationStatus.PENDING
    await updateStatus(newStatus)
  },
})

const updateStatus = async (newStatus: DuplicateRelationStatus) => {
  const docUnitDupRelation =
    documentUnit.value?.managementData?.duplicateRelations.find(
      (rel) => rel.documentNumber === duplicateRelation.documentNumber,
    )
  if (docUnitDupRelation) docUnitDupRelation.status = newStatus

  const { error } = await documentUnitService.setDuplicateRelationStatus(
    documentUnit.value!.documentNumber,
    duplicateRelation.documentNumber,
    newStatus,
  )
  hasSetStateError.value = error
}

const warningIgnoredLabel = computed(() =>
  duplicateRelation.isJdvDuplicateCheckActive
    ? "Warnung ignorieren"
    : `Warnung ignoriert wegen "Dupcode ausschalten" (jDV)`,
)
const coreDataText = computed(() =>
  [
    duplicateRelation.courtLabel,
    duplicateRelation.decisionDate &&
      DateUtil.formatDate(duplicateRelation.decisionDate),
    duplicateRelation.fileNumber,
    duplicateRelation.documentType,
  ]
    .filter(Boolean)
    .join(", "),
)
</script>

<template>
  <div :key="duplicateRelation.documentNumber" class="flex flex-col gap-8">
    <div class="flex flex-row items-center gap-12">
      <span>
        <IconErrorOutline
          :class="isIgnored ? 'invisible' : 'text-red-800'"
          :data-testid="`warning-icon-${duplicateRelation.documentNumber}`"
        />
      </span>

      <DecisionSummary
        class="ds-label-01-reg"
        :document-number="duplicateRelation.documentNumber"
        :status="{
          publicationStatus: duplicateRelation.publicationStatus,
        }"
        :summary="coreDataText"
      ></DecisionSummary>
    </div>

    <InputField
      :id="`is-ignored-${duplicateRelation.documentNumber}`"
      v-slot="{ id }"
      class="whitespace-nowrap"
      :label="warningIgnoredLabel"
      label-class="ds-label-01-reg"
      :label-position="LabelPosition.RIGHT"
    >
      <CheckboxInput
        :id="id"
        v-model="isIgnored"
        aria-label="Warnung ignorieren"
        class="ds-checkbox-mini"
        :readonly="!duplicateRelation.isJdvDuplicateCheckActive"
      />
    </InputField>

    <InfoModal
      v-if="hasSetStateError"
      data-testid="set-state-error"
      description="Bitte laden Sie die Seite neu und versuchen Sie es erneut."
      :status="InfoStatus.ERROR"
      title="Warnungsstatus konnte nicht gesetzt werden."
    />
  </div>
</template>
