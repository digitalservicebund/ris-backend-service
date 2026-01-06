<script setup lang="ts">
import { storeToRefs } from "pinia"
import Checkbox from "primevue/checkbox"
import Message from "primevue/message"
import { computed, Ref, ref } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { Decision } from "@/domain/decision"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/managementData"
import { PublicationState } from "@/domain/publicationStatus"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import DateUtil from "@/utils/dateUtil"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { duplicateRelation } = defineProps<{
  duplicateRelation: DuplicateRelation
}>()

const { documentUnit: decision } = storeToRefs(useDocumentUnitStore()) as {
  documentUnit: Ref<Decision | undefined>
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
    decision.value?.managementData?.duplicateRelations.find(
      (rel) => rel.documentNumber === duplicateRelation.documentNumber,
    )
  if (docUnitDupRelation) docUnitDupRelation.status = newStatus

  const { error } = await documentUnitService.setDuplicateRelationStatus(
    decision.value!.documentNumber,
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
    hasDuplicateState(decision.value!.status?.publicationStatus) ||
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

    <Message
      v-if="hasSetStateError"
      data-testid="set-state-error"
      severity="error"
    >
      <p class="ris-body1-bold">Warnungsstatus konnte nicht gesetzt werden.</p>
      <p>Bitte laden Sie die Seite neu und versuchen Sie es erneut.</p>
    </Message>
  </div>
</template>
