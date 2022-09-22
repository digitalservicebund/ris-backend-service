<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, toRefs } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocumentUnitPreviousDecisions from "@/components/DocumentUnitPreviousDecisions.vue"
import DocumentUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
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
  const status = (await documentUnitService.update(updatedDocumentUnit.value))
    .status
  setTimeout(() => {
    hasDataChange.value = false
    lastUpdatedDocumentUnit.value = JSON.stringify(props.documentUnit)
    updateStatus.value = status
    if (updateStatus.value !== UpdateStatus.SUCCEED) return
  }, 1000)
}
const router = useRouter()
const route = useRoute()

const isOnline = ref(navigator.onLine)
const updateStatus = ref(UpdateStatus.BEFORE_UPDATE)
const lastUpdatedDocumentUnit = ref(JSON.stringify(props.documentUnit))
const fileAsHTML = ref("")
const automaticUpload = ref()
const hasDataChange = ref(false)
const showDocPanel = ref(useRoute().query.showDocPanel === "true")
const handleToggleFilePanel = async () => {
  showDocPanel.value = !showDocPanel.value
  getOriginalDocumentUnit()
  await router.push({
    ...route,
    query: { ...route.query, showDocPanel: String(showDocPanel.value) },
  })
}

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

const originalOdocPanelYPos = 169

const handleScroll = () => {
  const element = document.getElementById("odoc-panel-element")
  if (!element) return
  const pos = originalOdocPanelYPos - window.scrollY
  const threshold = -40
  element.style.top = (pos < threshold ? threshold : pos) + "px"
}

const getOriginalDocumentUnit = async () => {
  if (fileAsHTML.value.length > 0) return
  fileAsHTML.value = props.documentUnit.s3path
    ? await fileService.getDocxFileAsHtml(props.documentUnit.s3path)
    : ""
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
  window.addEventListener("scroll", handleScroll)
  window.addEventListener(
    "keydown",
    handleUpdateDocumentUnitWithShortCut,
    false
  )
  autoUpdate()
  getOriginalDocumentUnit()
})
onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
  window.removeEventListener("keydown", handleUpdateDocumentUnitWithShortCut)
  removeAutoUpdate()
})
</script>

<template>
  <DocumentUnitWrapper v-slot="{ classes }" :document-unit="documentUnit">
    <div :class="classes">
      <v-row>
        <v-col :cols="showDocPanel ? 7 : 9">
          <DocumentUnitCoreData
            id="coreData"
            v-model="coreData"
            :update-status="updateStatus"
            @update-document-unit="handleUpdateDocumentUnit"
          />

          <DocumentUnitPreviousDecisions
            id="previousDecisions"
            v-model="previousDecisions"
            class="my-16"

          <DocumentUnitTexts
            id="texts"
            :texts="documentUnit.texts"
            :update-status="updateStatus"
            @update-document-unit="handleUpdateDocumentUnit"
            @update-value="handleUpdateValueDocumentUnitTexts"
          />
        </v-col>
        <OriginalFileSidePanel
          :file="fileAsHTML"
          :has-file="documentUnit.hasFile"
          :open="showDocPanel"
          @toggle-panel="handleToggleFilePanel"
        />
      </v-row>
    </div>
  </DocumentUnitWrapper>
</template>

<style scoped>
#previous-decisions {
  padding: 2rem 1.1rem;
}
</style>
