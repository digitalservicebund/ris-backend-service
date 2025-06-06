<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import { storeToRefs } from "pinia"
import { onBeforeUnmount, onMounted, Ref, ref } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import ErrorPage from "@/components/PageError.vue"
import SideToggle from "@/components/SideToggle.vue"
import { usePendingProceedingMenuItems } from "@/composables/usePendingProceedingMenuItems"
import useQuery from "@/composables/useQueryFromRoute"
import PendingProceeding from "@/domain/pendingProceeding"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

const props = defineProps<{
  documentNumber: string
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const store = useDocumentUnitStore()
const extraContentSidePanelStore = useExtraContentSidePanelStore()

const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<PendingProceeding | undefined>
}
const route = useRoute()
const menuItems = usePendingProceedingMenuItems(
  props.documentNumber,
  route.query,
)
const { pushQueryToRoute } = useQuery()

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

async function requestDocumentFromServer() {
  const response = await store.loadDocumentUnit(props.documentNumber)

  if (!response.data) {
    responseError.value = response.error
  }
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
    case "v":
      extraContentSidePanelStore.togglePanel(true)
      extraContentSidePanelStore.setSidePanelMode("preview")
      break
    default:
      break
  }
}

onBeforeUnmount(() => {
  // Remove the event listener when the component is unmounted
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
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
      </SideToggle>
    </div>
    <div v-if="documentUnit" class="flex w-full min-w-0 flex-col bg-gray-100">
      <DocumentUnitInfoPanel
        v-if="documentUnit && !route.path.includes('preview')"
        :document="documentUnit"
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
            v-if="documentUnit && !route.path.includes('preview')"
            :document="documentUnit"
            side-panel-mode="preview"
          ></ExtraContentSidePanel>
          <router-view v-bind="{ registerTextEditorRef }" />
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
