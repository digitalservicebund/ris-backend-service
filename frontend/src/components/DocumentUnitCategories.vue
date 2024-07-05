<script setup lang="ts">
import { computed, ref, toRefs, Ref, onMounted } from "vue"
import { useRoute } from "vue-router"
import AttachmentViewSidePanel from "@/components/AttachmentViewSidePanel.vue"
import DocumentUnitContentRelatedIndexing from "@/components/DocumentUnitContentRelatedIndexing.vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import { ValidationError } from "@/components/input/types"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import { useProvideCourtType } from "@/composables/useCourtType"
import useQuery from "@/composables/useQueryFromRoute"
import { useScrollToHash } from "@/composables/useScrollToHash"
import DocumentUnit, {
  ContentRelatedIndexing,
  Texts,
} from "@/domain/documentUnit"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import FeatureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  documentUnit: DocumentUnit
  showNavigationPanel: boolean
}>()
const updatedDocumentUnit = ref<DocumentUnit>(props.documentUnit)
const validationErrors = ref<ValidationError[]>([])
const route = useRoute()
const courtTypeRef = ref<string>(props.documentUnit.coreData.court?.type ?? "")
const notesFeatureToggle = ref(false)
const store = useDocumentUnitStore()

const showExtraContentPanelRef: Ref<boolean> = ref(false)

const { pushQueryToRoute } = useQuery<"showAttachmentPanel">()

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
  // Create patch and update
  const response = await store.updateDocumentUnit()

  if (response?.error?.validationErrors) {
    validationErrors.value = response.error.validationErrors
  } else {
    validationErrors.value = []
  }

  // if (response.data) {
  //   // Update store with current backend version
  //   store.documentUnit = response.data as DocumentUnit
  // }

  return response as ServiceResponse<void>
}

const handleUpdateValueDocumentUnitTexts = async (
  updatedValue: [keyof Texts, string],
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  store.documentUnit!.texts[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}

const coreData = computed({
  get: () => store.documentUnit!.coreData,
  set: async (newValues) => {
    store.documentUnit!.coreData = newValues
    courtTypeRef.value = updatedDocumentUnit.value.coreData.court?.type ?? ""
    // When the user changes the court to one that doesn't allow "Gesetzeskraft" all existing legal forces are deleted
    if (shouldDeleteLegalForces.value) {
      deleteLegalForces()
    }
  },
})

const previousDecisions = computed({
  get: () => store.documentUnit!.previousDecisions as PreviousDecision[],
  set: (newValues) => {
    store.documentUnit!.previousDecisions = newValues
  },
})

const ensuingDecisions = computed({
  get: () => store.documentUnit!.ensuingDecisions as EnsuingDecision[],
  set: (newValues) => {
    store.documentUnit!.ensuingDecisions = newValues
  },
})

const contentRelatedIndexing = computed({
  get: () =>
    store.documentUnit!.contentRelatedIndexing as ContentRelatedIndexing,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing = newValues
  },
})

const { hash: routeHash } = toRefs(route)
const headerOffset = 145
useScrollToHash(routeHash, headerOffset)

useProvideCourtType(courtTypeRef)

const toggleExtraContentPanel = () => {
  showExtraContentPanelRef.value = !showExtraContentPanelRef.value
  pushQueryToRoute({
    showAttachmentPanel: showExtraContentPanelRef.value.toString(),
  })
}

const handleOnSelectAttachment = (index: number) => {
  selectedAttachmentIndex.value = index
}

onMounted(async () => {
  notesFeatureToggle.value =
    (await FeatureToggleService.isEnabled("neuris.note")).data ?? false
  if (route.query.showAttachmentPanel) {
    showExtraContentPanelRef.value = route.query.showAttachmentPanel === "true"
  } else if (notesFeatureToggle.value) {
    showExtraContentPanelRef.value =
      !!props.documentUnit.note || props.documentUnit.hasAttachments
  }
})
</script>

<template>
  <DocumentUnitWrapper
    :document-unit="props.documentUnit as DocumentUnit"
    :save-callback="handleUpdateDocumentUnit"
    :show-navigation-panel="showNavigationPanel"
  >
    <template #default="{ classes }">
      <FlexContainer class="w-full flex-grow flex-row-reverse">
        <ExtraContentSidePanel
          v-if="notesFeatureToggle"
          :current-index="selectedAttachmentIndex"
          :document-unit="documentUnit"
          :document-unit-uuid="documentUnit.uuid"
          :is-expanded="showExtraContentPanelRef"
          @select="handleOnSelectAttachment"
          @toggle="toggleExtraContentPanel"
        ></ExtraContentSidePanel>
        <AttachmentViewSidePanel
          v-if="props.documentUnit.attachments && !notesFeatureToggle"
          :attachments="documentUnit.attachments"
          :current-index="selectedAttachmentIndex"
          :document-unit-uuid="props.documentUnit.uuid"
          :is-expanded="showExtraContentPanelRef"
          @select="handleOnSelectAttachment"
          @update="toggleExtraContentPanel"
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
