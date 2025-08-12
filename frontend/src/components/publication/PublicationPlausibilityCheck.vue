<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { computed, Ref, ref, watch } from "vue"
import { RouterLink, useRouter } from "vue-router"
import {
  contentRelatedIndexingLabels,
  Decision,
  longTextLabels,
} from "@/domain/decision"
import { fieldLabels } from "@/fields/caselaw"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const emits =
  defineEmits<
    (
      event: "updatePlausibilityCheck",
      isPlausibilityCheckValid: boolean,
    ) => void
  >()
const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision>
}

const categoriesRoute = {
  name: "caselaw-documentUnit-documentNumber-categories",
}

type CategoryWithMissingData = {
  field: string
  label: string
  entriesWithMissingDataCount: number
}

const categoriesWithMissingData = computed<CategoryWithMissingData[]>(() => {
  const categories: CategoryWithMissingData[] = []

  if (decision.value?.previousDecisions) {
    const count = decision.value.previousDecisions.filter(
      (docUnit) => docUnit.hasMissingRequiredFields,
    ).length
    if (count > 0) {
      categories.push({
        field: "previousDecisions",
        label: "Vorgehende Entscheidungen",
        entriesWithMissingDataCount: count,
      })
    }
  }

  if (decision.value?.ensuingDecisions) {
    const count = decision.value.ensuingDecisions.filter(
      (docUnit) => docUnit.hasMissingRequiredFields,
    ).length
    if (count > 0) {
      categories.push({
        field: "ensuingDecisions",
        label: "Nachgehende Entscheidungen",
        entriesWithMissingDataCount: count,
      })
    }
  }

  if (decision.value?.contentRelatedIndexing?.activeCitations) {
    const activeCitationsCount =
      decision.value.contentRelatedIndexing.activeCitations.filter(
        (citations) => citations.hasMissingRequiredFields,
      ).length
    if (activeCitationsCount > 0) {
      categories.push({
        field: "activeCitations",
        label: contentRelatedIndexingLabels.activeCitations,
        entriesWithMissingDataCount: activeCitationsCount,
      })
    }
  }

  if (decision.value?.contentRelatedIndexing?.norms) {
    const count = decision.value.contentRelatedIndexing.norms.filter(
      (citations) => citations.hasMissingFieldsInLegalForce,
    ).length
    if (count > 0) {
      categories.push({
        field: "norms",
        label: contentRelatedIndexingLabels.norms,
        entriesWithMissingDataCount: count,
      })
    }
  }

  return categories
})

const missingCoreDataFields = ref(
  decision.value.missingRequiredFields.map((field) => fieldLabels[field]),
)

const isCaseFactsInvalid = computed<boolean>(
  () =>
    !!decision.value?.longTexts.reasons &&
    !!decision.value?.longTexts.caseFacts,
)
const isDecisionReasonsInvalid = computed<boolean>(
  () =>
    !!decision.value?.longTexts.reasons &&
    !!decision.value?.longTexts.decisionReasons,
)
const isPlausibilityCheckValid = computed<boolean>(
  () =>
    missingCoreDataFields.value.length === 0 &&
    categoriesWithMissingData.value.length === 0 &&
    !isCaseFactsInvalid.value &&
    !isDecisionReasonsInvalid.value,
)
watch(
  isPlausibilityCheckValid,
  (isValid) => emits("updatePlausibilityCheck", isValid),
  { immediate: true },
)

const router = useRouter()
async function scrollToCategory(key: string) {
  await router.push(categoriesRoute)
  setTimeout(() => {
    const element = document.getElementById(key)
    if (element) {
      const headerOffset = 80
      const offsetPosition =
        element.getBoundingClientRect().top + window.scrollY - headerOffset
      window.scrollTo({ top: offsetPosition, behavior: "smooth" })
    }
  })
}
</script>

<template>
  <div class="flex flex-col gap-16">
    <h3 class="ris-label1-bold">Plausibilitätsprüfung</h3>

    <div v-if="isPlausibilityCheckValid" class="flex flex-row gap-8">
      <IconCheck class="text-green-700" />
      <p>Alle Pflichtfelder sind korrekt ausgefüllt.</p>
    </div>

    <div v-else class="ris-body1-regular flex flex-col gap-16">
      <div v-if="missingCoreDataFields.length" class="flex flex-row gap-8">
        <IconErrorOutline class="text-red-800" />
        <div class="flex flex-col gap-8">
          <p>Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:</p>
          <ul class="ml-32 list-disc">
            <li v-for="field in missingCoreDataFields" :key="field">
              {{ field }}
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

      <div v-if="isCaseFactsInvalid" class="flex flex-row gap-8">
        <IconErrorOutline class="text-red-800" />
        <p>
          Die Rubriken "{{ longTextLabels.reasons }}" und "{{
            longTextLabels.caseFacts
          }}" sind befüllt.<br />
          Es darf nur eine der beiden Rubriken befüllt sein.
        </p>
      </div>

      <div v-if="isDecisionReasonsInvalid" class="flex flex-row gap-8">
        <IconErrorOutline class="text-red-800" />
        <p>
          Die Rubriken "{{ longTextLabels.reasons }}" und "{{
            longTextLabels.decisionReasons
          }}" sind befüllt.<br />
          Es darf nur eine der beiden Rubriken befüllt sein.
        </p>
      </div>

      <Button
        aria-label="Rubriken bearbeiten"
        class="w-fit"
        label="Rubriken bearbeiten"
        severity="secondary"
        size="small"
        ><RouterLink :to="categoriesRoute"
          >Rubriken bearbeiten</RouterLink
        ></Button
      >
    </div>
  </div>
</template>
