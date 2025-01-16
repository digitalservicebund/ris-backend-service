<script setup lang="ts">
import { storeToRefs } from "pinia"
import { computed, ref } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import {
  DuplicateRelation,
  DuplicationRelationStatus,
} from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import DateUtil from "@/utils/dateUtil"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { duplicateRelation } = defineProps<{
  duplicateRelation: DuplicateRelation
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore())

const hasSetStateError = ref(false)

const isIgnored = computed({
  get: () => duplicateRelation.status === DuplicationRelationStatus.IGNORED,
  set: async (shouldBeIgnored) => {
    const newStatus = shouldBeIgnored
      ? DuplicationRelationStatus.IGNORED
      : DuplicationRelationStatus.PENDING
    await updateStatus(newStatus)
  },
})

const updateStatus = async (newStatus: DuplicationRelationStatus) => {
  const { error } = await documentUnitService.setDuplicationRelationStatus(
    documentUnit.value!.documentNumber,
    duplicateRelation.documentNumber,
    newStatus,
  )
  hasSetStateError.value = error
  if (!error) {
    const docUnitDupRelation =
      documentUnit.value?.managementData?.duplicateRelations.find(
        (rel) => rel.documentNumber === duplicateRelation.documentNumber,
      )
    if (docUnitDupRelation) docUnitDupRelation.status = newStatus
  }
}

const warningIgnoredLabel = computed(() =>
  duplicateRelation.isJdvDuplicateCheckActive
    ? "Warnung ignorieren"
    : `Warnung ignoriert wegen "Dupcode ausschalten" (jDV)`,
)
const coreDataText = computed(() =>
  [
    duplicateRelation.courtLabel,
    duplicateRelation.fileNumber,
    duplicateRelation.decisionDate &&
      DateUtil.formatDate(duplicateRelation.decisionDate),
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

      <span
        v-if="coreDataText"
        class="ds-label-01-reg"
        data-testid="core-data-text"
      >
        {{ coreDataText }}
      </span>

      <RouterLink
        v-if="duplicateRelation.documentNumber"
        class="ds-link-01-bold flex items-center whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
        :data-testid="
          'document-number-link-' + duplicateRelation.documentNumber
        "
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: { documentNumber: duplicateRelation.documentNumber },
        }"
      >
        {{ duplicateRelation.documentNumber }}
        <BaselineArrowOutward class="mb-4 inline w-24" />
      </RouterLink>
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
