<script lang="ts" setup>
import { useInterval } from "@vueuse/core"
import { storeToRefs } from "pinia"
import { computed, onBeforeUnmount, onMounted, Ref, ref, watch } from "vue"
import { useRoute } from "vue-router"
import ExtraContentSidePanel from "@/components/ExtraContentSidePanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import ErrorPage from "@/components/PageError.vue"
import PeriodicalEditionInfoPanel from "@/components/periodical-evaluation/PeriodicalEditionInfoPanel.vue"
import SideToggle from "@/components/SideToggle.vue"
import { usePeriodicalEvaluationMenuItems } from "@/composables/usePeriodicalEvaluationMenuItems"
import useQuery from "@/composables/useQueryFromRoute"
import { DocumentUnit } from "@/domain/documentUnit"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useEditionStore } from "@/stores/editionStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import StringsUtil from "@/utils/stringsUtil"
import IconClear from "~icons/ic/baseline-clear"

const store = useEditionStore()
const documentUnitStore = useDocumentUnitStore()
const extraContentSidePanelStore = useExtraContentSidePanelStore()
const { pushQueryToRoute } = useQuery()

const {
  counter: loadDocumentUnitTimer,
  pause,
  resume,
} = useInterval(10_000, {
  controls: true,
})

const responseError = ref<ResponseError>()
const route = useRoute()

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

const { documentUnit } = storeToRefs(documentUnitStore) as {
  documentUnit: Ref<DocumentUnit | undefined>
}
const infoSubtitle = computed(() =>
  StringsUtil.mergeNonBlankStrings(
    [store.edition?.legalPeriodical?.abbreviation, store.edition?.name],
    " ",
  ),
)

const menuItems = usePeriodicalEvaluationMenuItems(
  store.edition?.id,
  route.query,
)

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
      toggleNavigationPanel()
      break
    case "x":
      extraContentSidePanelStore.togglePanel(false)
      break
    default:
      break
  }
}

const { isExpanded } = storeToRefs(extraContentSidePanelStore)

watch(isExpanded, async () => {
  if (isExpanded.value) {
    resume()
  } else {
    await documentUnitStore.unloadDocumentUnit()
    pause()
  }
})

const showSidePanel = computed(
  () => documentUnit.value && route.path.includes("references"),
)

/**
 * To make sure the latest version is displayed,
 * when timer is resumed it will reload the documentation unit if in store.
 */
watch(loadDocumentUnitTimer, async () => {
  if (documentUnit.value?.documentNumber) {
    await documentUnitStore.loadDocumentUnit(documentUnit.value?.documentNumber)
  }
})

onBeforeUnmount(() => {
  // Remove the event listener when the component is unmounted
  window.removeEventListener("keydown", handleKeyDown)
})

onMounted(async () => {
  window.addEventListener("keydown", handleKeyDown)
  if (route.meta.error) {
    responseError.value = { title: route.meta.error as string }
  }
})
</script>

<template>
  <div class="flex w-screen grow">
    <div
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

    <div class="flex w-full flex-col bg-gray-100">
      <PeriodicalEditionInfoPanel :subtitle="infoSubtitle" />
      <ErrorPage
        v-if="responseError"
        :error="responseError"
        :title="responseError?.title"
      />
      <div v-else class="flex grow flex-row items-start">
        <router-view class="flex-1" />
        <ExtraContentSidePanel
          v-if="showSidePanel"
          :document-unit="documentUnit!"
          hide-panel-mode-bar
          :icon="IconClear"
          show-edit-button
          side-panel-mode="preview"
          side-panel-shortcut="x"
        />
      </div>
    </div>
  </div>
</template>
