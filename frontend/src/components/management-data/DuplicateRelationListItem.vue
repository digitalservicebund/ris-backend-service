<script setup lang="ts">
import { storeToRefs } from "pinia"
import Checkbox from "primevue/checkbox"
import { computed, Ref, ref } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import DocumentUnit, {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/documentUnit"
import { PublicationState } from "@/domain/publicationStatus"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import DateUtil from "@/utils/dateUtil"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { duplicateRelation } = defineProps<{
  duplicateRelation: DuplicateRelation
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore()) as {
  documentUnit: Ref<DocumentUnit | undefined>
}

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

const duplicateStates = [PublicationState.DUPLICATED, PublicationState.LOCKED]
const hasDuplicateState = (state?: PublicationState) =>
  state && duplicateStates.includes(state)
const isAutomaticallyIgnored = computed(
  () =>
    hasDuplicateState(duplicateRelation.publicationStatus) ||
    hasDuplicateState(documentUnit.value!.status?.publicationStatus) ||
    !duplicateRelation.isJdvDuplicateCheckActive,
)
const autoIgnoreLabel = computed(() =>
  !duplicateRelation.isJdvDuplicateCheckActive
    ? `Warnung ignoriert wegen "Dupcode ausschalten" (jDV)`
    : `Warnung ignoriert wegen Status "Dublette" oder "Gesperrt"`,
)
const ignoreWarningCheckboxLabel = computed(() =>
  isAutomaticallyIgnored.value ? autoIgnoreLabel.value : "Warnung ignorieren",
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
        class="ris-label1-regular"
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
      :label="ignoreWarningCheckboxLabel"
      label-class="ris-label1-regular"
      :label-position="LabelPosition.RIGHT"
    >
      <Checkbox
        v-model="isIgnored"
        aria-label="Warnung ignorieren"
        binary
        :disabled="isAutomaticallyIgnored"
        :input-id="id"
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
