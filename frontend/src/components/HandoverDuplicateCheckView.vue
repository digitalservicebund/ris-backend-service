<script lang="ts" setup>
import dayjs from "dayjs"
import DecisionSummary from "@/components/DecisionSummary.vue"
import TextButton from "@/components/input/TextButton.vue"
import { DuplicateRelation } from "@/domain/documentUnit"
import { PublicationStatus } from "@/domain/publicationStatus"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

defineProps<{
  isDuplicateFeatureActive: boolean
  hasActiveDuplicateWarning: boolean
  pendingDuplicates: DuplicateRelation[]
}>()

function renderSummary(duplicateRelation: DuplicateRelation) {
  return [
    ...(duplicateRelation.courtLabel
      ? [`${duplicateRelation.courtLabel}`]
      : []),
    ...(duplicateRelation.decisionDate
      ? [dayjs(duplicateRelation.decisionDate).format("DD.MM.YYYY")]
      : []),
    ...(duplicateRelation.fileNumber ? [duplicateRelation.fileNumber] : []),
    ...(duplicateRelation.documentType ? [duplicateRelation.documentType] : []),
  ].join(", ")
}
</script>
<template>
  <div
    v-if="isDuplicateFeatureActive"
    aria-label="Dublettenprüfung"
    class="flex flex-col"
  >
    <h2 class="ds-label-01-bold mb-16">Dublettenprüfung</h2>

    <div v-if="hasActiveDuplicateWarning">
      <div class="flex flex-row gap-8">
        <IconErrorOutline class="text-red-800" />
        <div class="ds-body-01-reg flex flex-col gap-8">
          Es besteht Dublettenverdacht.
          <div class="grid grid-cols-[auto_1fr] gap-24">
            <div class="ds-label-02-bold mt-12 self-start">
              {{
                pendingDuplicates.length > 1
                  ? "Dokumentationseinheiten"
                  : "Dokumentationseinheit"
              }}
            </div>
            <div>
              <div
                v-for="duplicateRelation in pendingDuplicates"
                :key="duplicateRelation.fileNumber"
                class="my-8"
              >
                <DecisionSummary
                  :document-number="duplicateRelation.documentNumber"
                  :status="
                    {
                      publicationStatus: duplicateRelation.publicationStatus,
                    } as PublicationStatus
                  "
                  :summary="renderSummary(duplicateRelation)"
                ></DecisionSummary>
              </div>
            </div>
          </div>
        </div>
      </div>
      <RouterLink to="managementData"
        ><TextButton
          aria-label="Dublettenwarnung prüfen"
          button-type="tertiary"
          class="mt-8 w-fit"
          label="Dublettenwarnung prüfen"
          size="small"
      /></RouterLink>
    </div>
    <div v-else class="flex flex-row gap-8">
      <IconCheck class="text-green-700" />
      <p>Es besteht kein Dublettenverdacht.</p>
    </div>
  </div>
</template>
