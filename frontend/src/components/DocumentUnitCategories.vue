<script setup lang="ts">
import { computed, ref, onMounted, toRefs, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitContentRelatedIndexing from "@/components/DocumentUnitContentRelatedIndexing.vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import { ValidationError } from "@/components/input/types"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import DocumentUnit, { Texts, CoreData } from "@/domain/documentUnit"
import { Docx2HTML } from "@/domain/docx2html"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()
const updatedDocumentUnit = ref<DocumentUnit>(props.documentUnit)
const validationErrors = ref<ValidationError[]>([])
const router = useRouter()
const route = useRoute()
const fileAsHTML = ref<Docx2HTML>()
const showDocPanel = useToggleStateInRouteQuery(
  "showDocPanel",
  route,
  router.replace,
  false,
)
const lastUpdatedDocumentUnit = ref()

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

  // console.log("newValue:", newValue)
  // console.log("oldValue:", oldValue)

  return newValue !== oldValue
}

async function handleUpdateDocumentUnit(): Promise<ServiceResponse<void>> {
  if (hasDataChange()) {
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
  return { status: 200, data: undefined } as ServiceResponse<void>
}

watch(
  showDocPanel,
  async () => {
    if (showDocPanel.value && fileAsHTML.value?.html.length == 0) {
      await getOriginalDocumentUnit()
    }
  },
  { immediate: true },
)

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

async function getOriginalDocumentUnit() {
  if (fileAsHTML.value?.html && fileAsHTML.value.html.length > 0) return
  if (props.documentUnit.s3path) {
    const htmlResponse = await fileService.getDocxFileAsHtml(
      props.documentUnit.uuid,
    )
    if (htmlResponse.error === undefined) fileAsHTML.value = htmlResponse.data
  }
}

onMounted(async () => {
  await getOriginalDocumentUnit()
})
</script>

<template>
  <DocumentUnitWrapper
    :document-unit="updatedDocumentUnit as DocumentUnit"
    :save-callback="handleUpdateDocumentUnit"
  >
    <template #default="{ classes }">
      <div class="flex w-full flex-grow">
        <div class="flex flex-col bg-gray-100" :class="classes">
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
        </div>

        <div
          class="flex flex-col border-l-1 border-solid border-gray-400 bg-white"
          :class="{ full: showDocPanel }"
        >
          <SideToggle
            v-model:is-expanded="showDocPanel"
            class="sticky top-[8rem] z-20"
            label="Originaldokument"
            :opening-direction="OpeningDirection.LEFT"
          >
            <OriginalFileSidePanel
              id="odoc-panel-element"
              v-model:open="showDocPanel"
              class="bg-white"
              :class="classes"
              :file="fileAsHTML?.html"
              :has-file="documentUnit.hasFile"
            />
          </SideToggle>
        </div>
      </div>
    </template>
  </DocumentUnitWrapper>
</template>

<style lang="scss" scoped>
.full {
  @apply w-full grow;
}
</style>
