<script setup lang="ts">
import { computed, ref, toRefs, Ref } from "vue"
import { useRoute } from "vue-router"
import AttachmentViewSidePanel from "@/components/AttachmentViewSidePanel.vue"
import DocumentUnitContentRelatedIndexing from "@/components/DocumentUnitContentRelatedIndexing.vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import { ValidationError } from "@/components/input/types"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import { useProvideCourtType } from "@/composables/useCourtType"
import useQuery from "@/composables/useQueryFromRoute"
import { useScrollToHash } from "@/composables/useScrollToHash"
import DocumentUnit, { Texts, CoreData } from "@/domain/documentUnit"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
  showAttachmentPanel?: boolean
  showNavigationPanel: boolean
}>()
const updatedDocumentUnit = ref<DocumentUnit>(props.documentUnit)
const validationErrors = ref<ValidationError[]>([])
const route = useRoute()
const courtTypeRef = ref<string>(props.documentUnit.coreData.court?.type ?? "")

const showAttachmentPanelRef: Ref<boolean> = ref(props.showAttachmentPanel)

const { pushQueryToRoute } = useQuery<"showAttachmentPanel">()

const lastUpdatedDocumentUnit = ref()
const selectedAttachmentIndex: Ref<number> = ref(0)

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

function hasDataChange(): boolean {
  const newValue = JSON.stringify(updatedDocumentUnit.value)
  const oldValue = JSON.stringify(lastUpdatedDocumentUnit.value)

  return newValue !== oldValue
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

async function handleUpdateDocumentUnit(): Promise<ServiceResponse<void>> {
  if (!hasDataChange())
    return {
      status: 304,
      data: undefined,
    } as ServiceResponse<void>

  lastUpdatedDocumentUnit.value = JSON.parse(
    JSON.stringify(updatedDocumentUnit.value),
  )
  const response = await documentUnitService.update(
    lastUpdatedDocumentUnit.value,
  )

  if (response?.error?.validationErrors) {
    validationErrors.value = response.error.validationErrors
  } else {
    validationErrors.value = []
  }

  if (!hasDataChange() && response.data) {
    updatedDocumentUnit.value = response.data as DocumentUnit
  }

  return response as ServiceResponse<void>
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
      handleUpdateDocumentUnit()
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

const toggleAttachmentPanel = () => {
  showAttachmentPanelRef.value = !showAttachmentPanelRef.value
  pushQueryToRoute({
    showAttachmentPanel: showAttachmentPanelRef.value.toString(),
  })
}

const handleOnSelect = (index: number) => {
  selectedAttachmentIndex.value = index
}
</script>

<template>
  <DocumentUnitWrapper
    :document-unit="updatedDocumentUnit as DocumentUnit"
    :save-callback="handleUpdateDocumentUnit"
    :show-navigation-panel="showNavigationPanel"
  >
    <template #default="{ classes }">
      <FlexContainer class="w-full flex-grow flex-row-reverse">
        <AttachmentViewSidePanel
          v-if="props.documentUnit.attachments"
          :attachments="documentUnit.attachments"
          :current-index="selectedAttachmentIndex"
          :document-unit-uuid="props.documentUnit.uuid"
          :is-expanded="showAttachmentPanelRef"
          @select="handleOnSelect"
          @update="toggleAttachmentPanel"
        ></AttachmentViewSidePanel>
        <FlexItem class="flex-1 flex-col bg-gray-100" :class="classes">
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
          <PreviousDecisions
            id="proceedingDecisions"
            v-model="previousDecisions"
          />
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
      </FlexContainer>
    </template>
  </DocumentUnitWrapper>
</template>
