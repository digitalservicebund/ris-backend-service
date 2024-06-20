<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import { onMounted, ref, Ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import { ValidationError } from "@/components/input/types"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import useQuery from "@/composables/useQueryFromRoute"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError, ServiceResponse } from "@/services/httpClient"
import useSessionStore from "@/stores/sessionStore"

const props = defineProps<{
  documentNumber: string
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const route = useRoute()
const { featureToggles } = useSessionStore()
const notesFeatureToggle = ref(featureToggles["neuris.note"] ?? false)
const menuItems = useCaseLawMenuItems(props.documentNumber, route.query)
const { pushQueryToRoute } = useQuery()

const documentUnit = ref<DocumentUnit>()
const updatedDocumentUnit = ref()
const lastUpdatedDocumentUnit = ref()
const validationErrors = ref<ValidationError[]>([])
const showNavigationPanelRef: Ref<boolean> = ref(
  route.query.showNavigationPanel !== "false",
)

const error = ref<ResponseError>()

const extraContentSidePanel = ref<InstanceType<
  typeof ExtraContentSidePanel
> | null>(null)

const toggleNavigationPanel = () => {
  showNavigationPanelRef.value = !showNavigationPanelRef.value
  pushQueryToRoute({
    ...route.query,
    showNavigationPanel: showNavigationPanelRef.value.toString(),
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

function hasDataChange(): boolean {
  const newValue = JSON.stringify(updatedDocumentUnit.value)
  const oldValue = JSON.stringify(lastUpdatedDocumentUnit.value)

  return newValue !== oldValue
}

function updateDocumentUnit(updatedDocumentUnitFromChild: DocumentUnit) {
  updatedDocumentUnit.value = updatedDocumentUnitFromChild
}

async function loadDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber,
  )

  if (response.data) {
    documentUnit.value = response.data
  } else {
    error.value = response.error
  }
}

async function attachmentIndexSelected(index: number) {
  extraContentSidePanel.value?.togglePanel(true)
  extraContentSidePanel.value?.selectAttachments(index)
}

async function attachmentIndexDeleted(index: number) {
  await loadDocumentUnit()
  extraContentSidePanel.value?.onAttachmentDeleted(index)
}

async function attachmentsUploaded(anySuccessful: boolean) {
  if (anySuccessful) {
    await loadDocumentUnit()
    extraContentSidePanel.value?.togglePanel(true)
    extraContentSidePanel.value?.selectAttachments(
      documentUnit.value ? documentUnit.value.attachments.length - 1 : 0,
    )
  }
}

onMounted(async () => {
  await loadDocumentUnit()
})
</script>

<template>
  <div class="flex w-screen grow">
    <div
      class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
    >
      <SideToggle
        class="sticky top-0 z-20"
        :is-expanded="showNavigationPanelRef"
        label="Navigation"
        size="small"
        tabindex="0"
        @keydown.enter="toggleNavigationPanel"
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
      </SideToggle>
    </div>
    <div v-if="documentUnit" class="flex w-full flex-col bg-gray-100">
      <DocumentUnitInfoPanel
        v-if="documentUnit"
        :document-unit="documentUnit"
        :heading="documentUnit?.documentNumber ?? ''"
        :save-callback="
          route.path.includes('categories')
            ? handleUpdateDocumentUnit
            : undefined
        "
      />
      <div class="flex grow flex-col items-start">
        <FlexContainer
          v-if="documentUnit"
          class="w-full flex-grow flex-row-reverse"
        >
          <ExtraContentSidePanel
            v-if="notesFeatureToggle && !route.path.includes('publication')"
            ref="extraContentSidePanel"
            :document-unit="documentUnit"
          ></ExtraContentSidePanel>
          <!-- TODO: Find better event names: load=loadFromServer, save=saveToServer, update=changesToLocalVersion  -->
          <router-view
            :document-unit="documentUnit"
            :validation-errors="validationErrors"
            @attachment-index-deleted="attachmentIndexDeleted"
            @attachment-index-selected="attachmentIndexSelected"
            @attachments-uploaded="attachmentsUploaded"
            @document-unit-save="handleUpdateDocumentUnit"
            @document-unit-update="updateDocumentUnit"
            @load-document-unit="loadDocumentUnit"
          />
        </FlexContainer>
        <ErrorPage v-else :error="error" :title="error?.title" />
      </div>
    </div>
  </div>
</template>
