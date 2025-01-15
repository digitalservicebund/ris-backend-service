<script setup lang="ts">
import { computed } from "vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import {
  DuplicationRelation,
  DuplicationRelationStatus,
} from "@/domain/documentUnit"
import DateUtil from "@/utils/dateUtil"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const { duplicateRelation } = defineProps<{
  duplicateRelation: DuplicationRelation
}>()

const isIgnored = computed({
  get: () => duplicateRelation.status === DuplicationRelationStatus.IGNORED,
  set: (shouldBeIgnored) => {
    if (shouldBeIgnored) {
      // TODO set status
    }
  },
})

const warningIgnoredLabel = computed(() =>
  duplicateRelation.isJdvDuplicateCheckActive
    ? "Warnung ignorieren"
    : `Warnung wegen "Dupcode ausschalten" (jDV) ignoriert`,
)
const coreDataText = computed(() =>
  [
    duplicateRelation.courtLabel,
    duplicateRelation.fileNumber,
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
        <IconErrorOutline class="text-red-800" />
      </span>

      <span v-if="coreDataText">
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
      id="isIgnored"
      v-slot="{ id }"
      class="whitespace-nowrap"
      :label="warningIgnoredLabel"
      label-class="ds-label-01-reg"
      :label-position="LabelPosition.RIGHT"
    >
      <CheckboxInput
        :id="id"
        v-model="isIgnored"
        :aria-label="warningIgnoredLabel"
        class="ds-checkbox-mini"
        :readonly="!duplicateRelation.isJdvDuplicateCheckActive"
      />
    </InputField>
  </div>
</template>
