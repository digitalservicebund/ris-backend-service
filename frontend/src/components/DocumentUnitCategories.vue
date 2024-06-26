<script setup lang="ts">
import { computed, ref, toRefs, watch } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitContentRelatedIndexing from "@/components/DocumentUnitContentRelatedIndexing.vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import FlexItem from "@/components/FlexItem.vue"
import { ValidationError } from "@/components/input/types"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import { useProvideCourtType } from "@/composables/useCourtType"
import { useScrollToHash } from "@/composables/useScrollToHash"
import DocumentUnit, { Texts, CoreData } from "@/domain/documentUnit"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"

const props = defineProps<{
  documentUnit: DocumentUnit
  validationErrors: ValidationError[]
}>()

const emits = defineEmits<{
  documentUnitUpdatedLocally: [DocumentUnit]
  saveDocumentUnitToServer: []
}>()

const updatedDocumentUnit = ref<DocumentUnit>(props.documentUnit)
const route = useRoute()
const courtTypeRef = ref<string>(props.documentUnit.coreData.court?.type ?? "")

watch(
  updatedDocumentUnit,
  () => {
    emits(
      "documentUnitUpdatedLocally",
      updatedDocumentUnit.value as DocumentUnit,
    )
  },
  { deep: true },
)

watch(
  () => props.documentUnit,
  (newValue) => {
    updatedDocumentUnit.value = newValue
  },
)

const courtTypesWithLegalForce = [
  "BVerfG",
  "VerfGH",
  "VerfG",
  "StGH",
  "VGH",
  "OVG",
]

/**
 * Determines whether legal forces should be deleted based on the court type and presence of a selected court.
 * @returns boolean
 */
const shouldDeleteLegalForces = computed(() => {
  return (
    !courtTypesWithLegalForce.includes(courtTypeRef.value) ||
    !updatedDocumentUnit.value.coreData.court
  )
})

const handleUpdateValueDocumentUnitTexts = async (
  updatedValue: [keyof Texts, string],
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  updatedDocumentUnit.value.texts[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}

/**
 * Deletes the legal forces from all single norms in the norms of the updated document unit.
 */
function deleteLegalForces() {
  const norms = updatedDocumentUnit.value.contentRelatedIndexing.norms

  norms?.forEach((norm) => {
    norm.singleNorms = norm.singleNorms?.filter((singleNorm) => {
      if (singleNorm.legalForce) {
        singleNorm.legalForce = undefined
      }
      return !singleNorm.isEmpty
    })
  })
}

const coreData = computed({
  get: () => updatedDocumentUnit.value.coreData,
  set: (newValues) => {
    let triggerSaving = false
    if (
      (["court", "procedure"] as (keyof CoreData)[]).some(
        (property) =>
          updatedDocumentUnit.value.coreData[property] !== newValues[property],
      )
    ) {
      triggerSaving = true
    }

    // --- TRANSFORMATION OF DATA

    newValues.fileNumbers = newValues.fileNumbers ?? []
    newValues.deviatingFileNumbers = newValues.deviatingFileNumbers ?? []
    newValues.deviatingCourts = newValues.deviatingCourts ?? []
    newValues.deviatingEclis = newValues.deviatingEclis ?? []
    newValues.deviatingDecisionDates = newValues.deviatingDecisionDates ?? []
    newValues.leadingDecisionNormReferences =
      newValues.leadingDecisionNormReferences ?? []

    // ---

    Object.assign(updatedDocumentUnit.value.coreData, newValues)
    courtTypeRef.value = updatedDocumentUnit.value.coreData.court?.type ?? ""
    // When the user changes the court to one that doesn't allow "Gesetzeskraft" all existing legal forces are deleted
    if (shouldDeleteLegalForces.value) {
      deleteLegalForces()
    }
    if (triggerSaving) {
      emits("saveDocumentUnitToServer")
    }
  },
})

const previousDecisions = computed({
  get: () => updatedDocumentUnit.value.previousDecisions as PreviousDecision[],
  set: (newValues) => {
    updatedDocumentUnit.value.previousDecisions = newValues
  },
})

const ensuingDecisions = computed({
  get: () => updatedDocumentUnit.value.ensuingDecisions as EnsuingDecision[],
  set: (newValues) => {
    updatedDocumentUnit.value.ensuingDecisions = newValues
  },
})

const contentRelatedIndexing = computed({
  get: () => (updatedDocumentUnit.value as DocumentUnit).contentRelatedIndexing,
  set: (newValues) => {
    Object.assign(updatedDocumentUnit.value.contentRelatedIndexing, newValues)
  },
})

const { hash: routeHash } = toRefs(route)
const headerOffset = 145
useScrollToHash(routeHash, headerOffset)

useProvideCourtType(courtTypeRef)
</script>
<template>
  <FlexItem class="w-full flex-1 grow flex-col bg-gray-100 p-24">
    <DocumentUnitCoreData
      id="coreData"
      v-model="coreData"
      class="mb-24"
      :validation-errors="
        validationErrors.filter(
          (err) => err.instance.split('\.')[0] === 'coreData',
        )
      "
    />
    <PreviousDecisions id="proceedingDecisions" v-model="previousDecisions" />
    <EnsuingDecisions v-model="ensuingDecisions" class="mb-24" />

    <DocumentUnitContentRelatedIndexing
      id="contentRelatedIndexing"
      v-model="contentRelatedIndexing"
      class="mb-24"
    />

    <DocumentUnitTexts
      id="texts"
      :texts="updatedDocumentUnit.texts"
      :valid-border-numbers="updatedDocumentUnit.borderNumbers"
      @update-value="handleUpdateValueDocumentUnitTexts"
    />
  </FlexItem>
</template>
