<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import { computed, onBeforeUnmount, onMounted, Ref, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import ErrorPage from "@/components/PageError.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { usePendingProceedingMenuItems } from "@/composables/usePendingProceedingMenuItems"
import useQuery from "@/composables/useQueryFromRoute"
import { Decision } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import MenuItem from "@/domain/menuItem"
import PendingProceeding from "@/domain/pendingProceeding"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  documentNumber: string
  kind: Kind
}>()

const emit = defineEmits<{
  jumpToMatch: [value: Match]
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const textCheck = useFeatureToggle("neuris.text-check-side-panel")

const store = useDocumentUnitStore()
const extraContentSidePanelStore = useExtraContentSidePanelStore()

const documentUnit = computed<DocumentationUnit | undefined>(() => {
  if (props.kind === Kind.DECISION) {
    return store.documentUnit as Decision
  } else if (props.kind === Kind.PENDING_PROCEEDING) {
    return store.documentUnit as PendingProceeding
  }
  return undefined
})

const processSteps: Ref<DocumentationUnitProcessStep[]> = ref([])

const route = useRoute()

const { pushQueryToRoute } = useQuery()

const menuItems = computed<MenuItem[]>(() => {
  let itemsRef
  if (props.kind === Kind.DECISION) {
    itemsRef = useCaseLawMenuItems(props.documentNumber!, route.query)
  } else if (props.kind === Kind.PENDING_PROCEEDING) {
    itemsRef = usePendingProceedingMenuItems(props.documentNumber!, route.query)
  } else {
    return []
  }
  return itemsRef.value
})

const showNavigationPanelRef: Ref<boolean> = ref(
  route.query.showNavigationPanel !== "false",
)

const responseError = ref<ResponseError>()

function toggleNavigationPanel(expand?: boolean) {
  showNavigationPanelRef.value =
    expand === undefined ? !showNavigationPanelRef.value : expand
  pushQueryToRoute({
    ...route.query,
    showNavigationPanel: showNavigationPanelRef.value.toString(),
  })
}

async function requestDocumentUnitFromServer() {
  const response = await store.loadDocumentUnit(props.documentNumber)

  if (!response.data) {
    responseError.value = response.error
  } else {
    await requestProcessStepsFromServer(response.data.uuid)
  }
}

async function requestProcessStepsFromServer(docUnitId: string) {
  await processStepService.getProcessSteps(docUnitId).then((response) => {
    if (response.data) processSteps.value = response.data.toReversed()
    else processSteps.value = [] // TODO show error?
  })
}

const textEditorRefs = ref<Record<string, typeof TextEditor | null>>({})

/**
 * Gets text category as a key and add its text editor ref to the ref list
 * @param textCategory
 * @param textEditorComponent
 */
const registerTextEditorRef = (
  textCategory: string,
  textEditorComponent: typeof TextEditor,
) => {
  textEditorRefs.value[textCategory] = textEditorComponent
}

const handleKeyDown = (event: KeyboardEvent) => {
  // List of tag names where shortcuts should be disabled
  const tagName = (event.target as HTMLElement).tagName.toLowerCase()

  // Check if the active element is an input, textarea, or any element with contenteditable
  if (
    ["input", "textarea", "select"].includes(tagName) ||
    (event.target as HTMLElement).isContentEditable
  ) {
    if (event.key === "Escape") (event.target as HTMLElement).blur() // Remove focus from the input field
    return // Do nothing if the user is typing in an input field or editable area
  }

  switch (event.key) {
    case "<":
      event.preventDefault()
      toggleNavigationPanel(extraContentSidePanelStore.togglePanel())
      break
    case "n":
      if (props.kind === Kind.DECISION) {
        event.preventDefault()
        extraContentSidePanelStore.togglePanel(true)
        extraContentSidePanelStore.setSidePanelMode("note")
      }
      break
    case "d":
      if (props.kind === Kind.DECISION) {
        event.preventDefault()
        extraContentSidePanelStore.togglePanel(true)
        extraContentSidePanelStore.setSidePanelMode("attachments")
      }
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
      if (!textCheck.value && props.kind === Kind.DECISION) break
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("text-check")
      break
    default:
      break
  }
}

/**
 * Jump to the corresponding text editor and opens the match menu
 * @param match
 */
function jumpToMatch(match: Match) {
  const textEditor = textEditorRefs.value[match.category]
  if (textEditor) {
    textEditor.jumpToMatch(match)
  }
  emit("jumpToMatch", match)
}

onBeforeUnmount(() => {
  // Remove the event listener when the component is unmounted
  window.removeEventListener("keydown", handleKeyDown)
})

onMounted(async () => {
  window.addEventListener("keydown", handleKeyDown)
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
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
      </SideToggle>
    </div>
    <div v-if="documentUnit" class="flex w-full min-w-0 flex-col bg-gray-100">
      <DocumentUnitInfoPanel
        v-if="documentUnit && !route.path.includes('preview')"
        :document-unit="documentUnit"
        :process-steps="processSteps"
      />
      <div class="flex grow flex-col items-start">
        <FlexContainer
          v-if="documentUnit"
          class="h-full w-full flex-grow"
          :class="
            route.path.includes('preview')
              ? 'flex-row bg-white'
              : 'flex-row-reverse'
          "
        >
          <ExtraContentSidePanel
            v-if="
              documentUnit &&
              !route.path.includes('handover') &&
              !route.path.includes('preview')
            "
            v-bind="{ jumpToMatch }"
            :document-unit="documentUnit"
          />
          <slot
            :document-unit="documentUnit"
            :jump-to-match="jumpToMatch"
            :register-text-editor-ref="registerTextEditorRef"
            :request-document-unit-from-server="requestDocumentUnitFromServer"
          ></slot>
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
