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

const props = defineProps<{
  documentNumber: string
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const route = useRoute()
const menuItems = useCaseLawMenuItems(props.documentNumber, route.query)
const { pushQueryToRoute } = useQuery()

const documentUnit = ref<DocumentUnit>()
const lastUpdatedDocumentUnit = ref()
const validationErrors = ref<ValidationError[]>([])
const showNavigationPanelRef: Ref<boolean> = ref(
  route.query.showNavigationPanel !== "false",
)

const responseError = ref<ResponseError>()

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

async function saveDocumentUnitToServer(): Promise<ServiceResponse<void>> {
  if (!hasDataChange())
    return {
      status: 304,
      data: undefined,
    } as ServiceResponse<void>

  lastUpdatedDocumentUnit.value = JSON.parse(JSON.stringify(documentUnit.value))
  const response = await documentUnitService.update(
    lastUpdatedDocumentUnit.value,
  )

  if (response?.error?.validationErrors) {
    validationErrors.value = response.error.validationErrors
  } else {
    validationErrors.value = []
  }

  if (!hasDataChange() && response.data) {
    documentUnit.value = response.data as DocumentUnit
  }

  return response as ServiceResponse<void>
}

function hasDataChange(): boolean {
  const newValue = JSON.stringify(documentUnit.value)
  const oldValue = JSON.stringify(lastUpdatedDocumentUnit.value)

  return newValue !== oldValue
}

function updateLocalDocumentUnit(updatedDocumentUnitFromChild: DocumentUnit) {
  documentUnit.value = updatedDocumentUnitFromChild
}

async function requestDocumentUnitFromServer() {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber,
  )

  if (response.data) {
    documentUnit.value = response.data
  } else {
    responseError.value = response.error
  }
}

async function attachmentIndexSelected(index: number) {
  extraContentSidePanel.value?.togglePanel(true)
  extraContentSidePanel.value?.selectAttachments(index)
}

async function attachmentIndexDeleted(index: number) {
  await requestDocumentUnitFromServer()
  extraContentSidePanel.value?.onAttachmentDeleted(index)
}

async function attachmentsUploaded(anySuccessful: boolean) {
  if (anySuccessful) {
    await requestDocumentUnitFromServer()
    extraContentSidePanel.value?.togglePanel(true)
    extraContentSidePanel.value?.selectAttachments(
      documentUnit.value ? documentUnit.value.attachments.length - 1 : 0,
    )
  }
}

onMounted(async () => {
  await requestDocumentUnitFromServer()
})
</script>

<template>
  <div class="flex w-screen grow">
    <div
      v-if="!route.path.includes('preview') && documentUnit"
      class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
    >
      <SideToggle
        class="sticky top-0 z-20"
        data-testid="side-toggle-navigation"
        :is-expanded="showNavigationPanelRef"
        label="Navigation"
        tabindex="0"
        test-id="side-toggle-navigation"
        @keydown.enter="toggleNavigationPanel"
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
      </SideToggle>
    </div>
    <div v-if="documentUnit" class="flex w-full flex-col bg-gray-100">
      <DocumentUnitInfoPanel
        v-if="documentUnit && !route.path.includes('preview')"
        data-testid="document-unit-info-panel"
        :document-unit="documentUnit"
        :heading="documentUnit?.documentNumber ?? ''"
        :save-callback="
          route.path.includes('categories')
            ? saveDocumentUnitToServer
            : undefined
        "
      />
      <div class="flex grow flex-col items-start">
        <FlexContainer
          v-if="documentUnit"
          class="w-full flex-grow"
          :class="
            route.path.includes('preview')
              ? 'flex-row bg-white'
              : 'flex-row-reverse'
          "
        >
          <ExtraContentSidePanel
            v-if="
              !(
                route.path.includes('publication') ||
                route.path.includes('preview')
              )
            "
            ref="extraContentSidePanel"
            :document-unit="documentUnit"
          ></ExtraContentSidePanel>
          <router-view
            :document-unit="documentUnit"
            :validation-errors="validationErrors"
            @attachment-index-deleted="attachmentIndexDeleted"
            @attachment-index-selected="attachmentIndexSelected"
            @attachments-uploaded="attachmentsUploaded"
            @document-unit-updated-locally="updateLocalDocumentUnit"
            @request-document-unit-from-server="requestDocumentUnitFromServer"
            @save-document-unit-to-server="saveDocumentUnitToServer"
          />
        </FlexContainer>
      </div>
    </div>
    <ErrorPage
      v-if="responseError"
      :error="responseError"
      :title="responseError?.title"
    />
  </div>
</template>
