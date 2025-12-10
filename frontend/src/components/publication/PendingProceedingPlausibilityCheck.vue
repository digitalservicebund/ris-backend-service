<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { computed, Ref, watch } from "vue"
import { RouterLink, useRouter } from "vue-router"
import { useScroll } from "@/composables/useScroll"
import PendingProceeding, {
  pendingProceedingLabels,
} from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const emits =
  defineEmits<
    (
      event: "plausibilityCheckUpdated",
      hasPlausibilityCheckPassed: boolean,
    ) => void
  >()
const store = useDocumentUnitStore()
const { documentUnit: pendingProceeding } = storeToRefs(store) as {
  documentUnit: Ref<PendingProceeding>
}

const categoriesRoute = {
  name: "caselaw-pending-proceeding-documentNumber-categories",
}

type CategoryWithMissingData = {
  field: string
  label: string
  entriesWithMissingDataCount: number
}

const categoriesWithMissingData = computed<CategoryWithMissingData[]>(() => {
  const categories: CategoryWithMissingData[] = []

  if (pendingProceeding.value?.previousDecisions) {
    const count = pendingProceeding.value.previousDecisions.filter(
      (docUnit) => docUnit.hasMissingRequiredFields,
    ).length
    if (count > 0) {
      categories.push({
        field: "previousDecisions",
        label: pendingProceedingLabels.previousDecisions,
        entriesWithMissingDataCount: count,
      })
    }
  }

  return categories
})

type MissingRequiredField = { field: string; label: string }
const missingRequiredFields = computed<MissingRequiredField[]>(() => {
  const missingFields = []

  const requiredCoreFields = ["court", "decisionDate", "fileNumbers"] as const
  for (const field of requiredCoreFields) {
    const value = pendingProceeding.value.coreData?.[field]
    if (!value || (Array.isArray(value) && value.length === 0)) {
      const label = pendingProceedingLabels[field]
      missingFields.push({ field, label })
    }
  }

  if (!pendingProceeding.value.shortTexts.legalIssue) {
    missingFields.push({
      field: "legalIssue",
      label: pendingProceedingLabels.legalIssue,
    })
  }

  return missingFields
})

const hasPlausibilityCheckPassed = computed<boolean>(
  () =>
    missingRequiredFields.value.length === 0 &&
    categoriesWithMissingData.value.length === 0,
)
watch(
  hasPlausibilityCheckPassed,
  (hasPassed) => emits("plausibilityCheckUpdated", hasPassed),
  { immediate: true },
)

const router = useRouter()
const { scrollIntoViewportById } = useScroll()
async function scrollToCategory(key: string) {
  await router.push(categoriesRoute)
  await scrollIntoViewportById(key)
}
</script>

<template>
  <div class="flex flex-col gap-16">
    <h3 class="ris-label1-bold">Plausibilitätsprüfung</h3>

    <div v-if="hasPlausibilityCheckPassed" class="flex flex-row gap-8">
      <IconCheck class="text-green-700" />
      <p>Alle Pflichtfelder sind korrekt ausgefüllt.</p>
    </div>

    <div v-else class="ris-body1-regular flex flex-col gap-16">
      <div v-if="missingRequiredFields.length" class="flex flex-row gap-8">
        <IconErrorOutline class="text-red-800" />
        <div class="flex flex-col">
          <p>Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:</p>
          <ul class="list-disc">
            <li v-for="{ field, label } in missingRequiredFields" :key="field">
              <Button
                class="h-auto border-none p-0!"
                text
                variant="link"
                @click="scrollToCategory(field)"
                >{{ label }}</Button
              >
            </li>
          </ul>
        </div>
      </div>

      <div
        v-if="categoriesWithMissingData?.length > 0"
        class="flex flex-row gap-8"
      >
        <IconErrorOutline class="text-red-800" />
        <div class="flex flex-col gap-8">
          <p>In folgenden Rubriken fehlen Daten:</p>

          <dl
            v-for="categoryWithMissingData in categoriesWithMissingData"
            :key="categoryWithMissingData.field"
            class="mb-8"
          >
            <div class="grid grid-cols-2 gap-24 px-0">
              <dt class="ris-label2-bold self-center">Rubrik</dt>
              <dd class="ris-body2-regular">
                <Button
                  class="h-auto border-none p-0!"
                  text
                  variant="link"
                  @click="scrollToCategory(categoryWithMissingData.field)"
                >
                  {{ categoryWithMissingData.label }}</Button
                >
              </dd>
            </div>
            <div class="grid grid-cols-2 gap-24 px-0">
              <dt class="ris-label2-bold self-center">
                Einträge mit fehlenden Daten
              </dt>
              <dd class="ris-body2-regular">
                {{ categoryWithMissingData.entriesWithMissingDataCount }}
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <Button
        aria-label="Rubriken bearbeiten"
        class="w-fit"
        label="Rubriken bearbeiten"
        severity="secondary"
        size="small"
        ><RouterLink tabindex="-1" :to="categoriesRoute"
          >Rubriken bearbeiten</RouterLink
        ></Button
      >
    </div>
  </div>
</template>
