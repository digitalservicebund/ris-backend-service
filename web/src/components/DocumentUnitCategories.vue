<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, toRefs, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitPreviousDecisions from "@/components/DocumentUnitPreviousDecisions.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import { ValidationError } from "@/domain"
import DocumentUnit, { CoreData, Texts } from "@/domain/documentUnit"
import { UpdateStatus } from "@/enum/enumUpdateStatus"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()
const updatedDocumentUnit = computed(() => props.documentUnit)

const handleUpdateValueDocumentUnitTexts = async (
  updatedValue: [keyof Texts, string]
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  updatedDocumentUnit.value.texts[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}

const handleUpdateDocumentUnit = async () => {
  updateStatus.value = UpdateStatus.ON_UPDATE
  const response = await documentUnitService.update(updatedDocumentUnit.value)
  if (response.error && response.error.validationErrors) {
    validationErrors.value = response.error.validationErrors
  } else {
    validationErrors.value = []
  }
  setTimeout(() => {
    hasDataChange.value = false
    lastUpdatedDocumentUnit.value = JSON.stringify(props.documentUnit)
    updateStatus.value = response.status
    if (updateStatus.value !== UpdateStatus.SUCCEED) return
  }, 1000)
}
const router = useRouter()
const route = useRoute()

const isOnline = ref(navigator.onLine)
const validationErrors = ref<ValidationError[]>([])
const updateStatus = ref(UpdateStatus.BEFORE_UPDATE)
const lastUpdatedDocumentUnit = ref(JSON.stringify(props.documentUnit))
const fileAsHTML = ref("")
const automaticUpload = ref()
const hasDataChange = ref(false)
const showDocPanel = useToggleStateInRouteQuery(
  "showDocPanel",
  route,
  router.replace,
  false
)

watch(
  showDocPanel,
  () => {
    if (showDocPanel.value && fileAsHTML.value.length == 0) {
      getOriginalDocumentUnit()
    }
  },
  { immediate: true }
)

const coreData = computed({
  get: () => props.documentUnit.coreData,
  set: (newValues) => {
    for (const [key, value] of Object.entries(newValues)) {
      updatedDocumentUnit.value.coreData[key as keyof CoreData] = value
    }
  },
})

const previousDecisions = computed({
  get: () => props.documentUnit.previousDecisions,
  set: (newValue) => (updatedDocumentUnit.value.previousDecisions = newValue),
})

const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const fixedPanelPosition = ref(false)

function onScroll() {
  const element = document.getElementById("odoc-panel-element")
  if (!element) return
  element.getBoundingClientRect().top <= 0 && showDocPanel.value === true
    ? (fixedPanelPosition.value = true)
    : (fixedPanelPosition.value = false)
}

async function getOriginalDocumentUnit() {
  if (fileAsHTML.value.length > 0) return
  if (props.documentUnit.s3path) {
    const htmlResponse = await fileService.getDocxFileAsHtml(
      props.documentUnit.s3path
    )
    if (htmlResponse.error === undefined) fileAsHTML.value = htmlResponse.data
  }
}

/** Overwrite ctrl + S to update documentUnit */
const handleUpdateDocumentUnitWithShortCut = (event: KeyboardEvent) => {
  const OS = navigator.userAgent.indexOf("Mac") != -1 ? "Mac" : "Window"
  if (OS === "Mac") {
    if (!event.metaKey) return
    if (event.key !== "s") return
    handleUpdateDocumentUnit()
    event.preventDefault()
  } else {
    if (!event.ctrlKey) return
    if (event.key !== "s") return
    handleUpdateDocumentUnit()
    event.preventDefault()
  }
}

/** Time interval to automatic update documentUnit every 10sec */
/** Only update documentUnit when there is any change after 10sec and last update is done */
const autoUpdate = () => {
  automaticUpload.value = setInterval(() => {
    hasDataChange.value =
      JSON.stringify(props.documentUnit) !== lastUpdatedDocumentUnit.value
    if (
      isOnline.value &&
      hasDataChange.value &&
      updateStatus.value !== UpdateStatus.ON_UPDATE
    ) {
      handleUpdateDocumentUnit()
    }
    lastUpdatedDocumentUnit.value = JSON.stringify(props.documentUnit)
    /** Offline mode */
    if (isOnline.value && !navigator.onLine) {
      isOnline.value = false
    }
    if (!isOnline.value && navigator.onLine) {
      isOnline.value = true
      handleUpdateDocumentUnit()
    }
  }, 10000)
}
/** Clear time Interval */
const removeAutoUpdate = () => {
  clearInterval(automaticUpload.value)
}

onMounted(async () => {
  window.addEventListener("scroll", onScroll)
  window.addEventListener(
    "keydown",
    handleUpdateDocumentUnitWithShortCut,
    false
  )
  autoUpdate()
  getOriginalDocumentUnit()
})
onUnmounted(() => {
  window.removeEventListener("scroll", onScroll)
  window.removeEventListener("keydown", handleUpdateDocumentUnitWithShortCut)
  removeAutoUpdate()
})
</script>

<template>
  <DocumentUnitWrapper v-slot="{ classes }" :document-unit="documentUnit">
    <div class="flex w-full">
      <div :class="classes">
        <DocumentUnitCoreData
          id="coreData"
          v-model="coreData"
          :update-status="updateStatus"
          :validation-errors="
            validationErrors.filter(
              (err) => err.field.split('\.')[0] === 'coreData'
            )
          "
          @update-document-unit="handleUpdateDocumentUnit"
        />

        <!-- TODO add validationErrors -->
        <DocumentUnitPreviousDecisions
          id="previousDecisions"
          v-model="previousDecisions"
          class="my-16"
        />

        <!-- TODO add validationErrors -->
        <DocumentUnitTexts
          id="texts"
          :texts="documentUnit.texts"
          :update-status="updateStatus"
          @update-document-unit="handleUpdateDocumentUnit"
          @update-value="handleUpdateValueDocumentUnitTexts"
        />
      </div>

      <div
        class="bg-white border-gray-400 border-l-1 border-solid"
        :class="{ full: showDocPanel }"
      >
        <OriginalFileSidePanel
          id="odoc-panel-element"
          v-model:open="showDocPanel"
          class="bg-white"
          :class="classes"
          :file="fileAsHTML"
          :fixed-panel-position="fixedPanelPosition"
          :has-file="documentUnit.hasFile"
        />
      </div>
    </div>
  </DocumentUnitWrapper>
</template>

<style lang="scss" scoped>
.full {
  @apply w-full grow;
}
</style>
