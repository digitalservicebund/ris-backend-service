<script setup lang="ts" generic="TDocument, TError extends ResponseError">
import { useHead } from "@unhead/vue"
import { Ref, ref, onMounted, onBeforeUnmount } from "vue"
import { LocationQuery, useRoute } from "vue-router"

import DocumentInfoPanel from "@/components/DocumentInfoPanel.vue"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import { Documentable } from "@/components/input/types"
import NavbarSide from "@/components/NavbarSide.vue"
import ErrorPage from "@/components/PageError.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import useQuery from "@/composables/useQueryFromRoute"
import MenuItem from "@/domain/menuItem"
import { ResponseError } from "@/services/httpClient"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  documentNumber: string
  loadDocument: (
    documentNumber: string,
  ) => Promise<{ data?: TDocument; error?: TError }>
  getMenuItems: (
    documentNumber: string,
    query: LocationQuery,
  ) => Ref<MenuItem[]>
}>()

const document = ref<TDocument | undefined>(undefined)
const responseError = ref<TError | undefined>(undefined)

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const textCheck = useFeatureToggle("neuris.text-check-side-panel")

const extraContentSidePanelStore = useExtraContentSidePanelStore()

const route = useRoute()

const { pushQueryToRoute } = useQuery()

const showNavigationPanelRef: Ref<boolean> = ref(
  route.query.showNavigationPanel !== "false",
)

function toggleNavigationPanel(expand?: boolean) {
  showNavigationPanelRef.value =
    expand === undefined ? !showNavigationPanelRef.value : expand
  pushQueryToRoute({
    ...route.query,
    showNavigationPanel: showNavigationPanelRef.value.toString(),
  })
}

async function requestDocumentFromServer() {
  const response = await props.loadDocument(props.documentNumber)
  if (response.data) {
    document.value = response.data
  } else {
    document.value = undefined
    responseError.value = response.error as TError
  }
}

async function attachmentIndexSelected(index: number) {
  extraContentSidePanelStore.togglePanel(true)
  extraContentSidePanelStore.selectAttachments(index)
}

async function attachmentIndexDeleted(index: number) {
  await requestDocumentFromServer()
  extraContentSidePanelStore.onAttachmentDeleted(
    index,
    document.value?.attachments ? document.value.attachments.length - 1 : 0,
  )
}

async function attachmentsUploaded(anySuccessful: boolean) {
  if (anySuccessful) {
    await requestDocumentFromServer()
    extraContentSidePanelStore.togglePanel(true)
    extraContentSidePanelStore.selectAttachments(
      document.value?.attachments ? document.value.attachments.length - 1 : 0,
    )
  }
}

const textEditorRefs = ref<Record<string, typeof TextEditor | null>>({})

const registerTextEditorRef = (
  textCategory: string,
  textEditorComponent: typeof TextEditor,
) => {
  textEditorRefs.value[textCategory] = textEditorComponent
}

function jumpToMatch(match: Match) {
  const textEditor = textEditorRefs.value[match.category]
  if (textEditor) {
    textEditor.jumpToMatch(match)
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  const tagName = (event.target as HTMLElement).tagName.toLowerCase()

  if (
    ["input", "textarea", "select"].includes(tagName) ||
    (event.target as HTMLElement).isContentEditable
  ) {
    if (event.key === "Escape") (event.target as HTMLElement).blur()
    return
  }

  switch (event.key) {
    case "<":
      event.preventDefault()
      toggleNavigationPanel(extraContentSidePanelStore.togglePanel())
      break
    case "n":
      event.preventDefault()
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("note")
      break
    case "d":
      event.preventDefault()
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("attachments")
      break
    case "v":
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("preview")
      break
    case "r":
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("category-import")
      break
    case "t":
      if (!textCheck.value) break
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("text-check")
      break
    default:
      break
  }
}

onBeforeUnmount(() => {
  window.removeEventListener("keydown", handleKeyDown)
})

onMounted(async () => {
  window.addEventListener("keydown", handleKeyDown)
  await requestDocumentFromServer()
})
</script>

<template>
  <div class="flex w-screen grow">
    <div
      v-if="!route.path.includes('preview') && document"
      class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
    >
      <SideToggle
        class="sticky top-0 z-20"
        data-testid="side-toggle-navigation"
        :is-expanded="showNavigationPanelRef"
        label="Navigation"
        tabindex="0"
        test-id="side-toggle-navigation"
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide
          :is-child="false"
          :menu-items="
            props.getMenuItems(props.documentNumber, route.query).value
          "
          :route="route"
        />
      </SideToggle>
    </div>

    <div v-if="document" class="flex w-full min-w-0 flex-col bg-gray-100">
      <slot :document="document" name="info-panel">
        <DocumentInfoPanel
          v-if="!route.path.includes('preview')"
          data-testid="document-unit-info-panel"
          :document="document as Documentable"
        />
      </slot>

      <div class="flex grow flex-col items-start">
        <FlexContainer
          v-if="document"
          class="h-full w-full flex-grow"
          :class="{
            'flex-row bg-white': route.path.includes('preview'),
            'flex-row-reverse': !route.path.includes('preview'),
          }"
        >
          <ExtraContentSidePanel
            v-if="
              document &&
              !route.path.includes('handover') &&
              !route.path.includes('preview')
            "
            v-bind="{ jumpToMatch }"
            :document-unit="document as any"
          ></ExtraContentSidePanel>

          <slot
            :attachment-index-deleted="attachmentIndexDeleted"
            :attachment-index-selected="attachmentIndexSelected"
            :attachments-uploaded="attachmentsUploaded"
            :document="document"
            name="main-content"
            :register-text-editor-ref="registerTextEditorRef"
          >
          </slot>
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
