<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, toRefs } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import DocUnitPreviousDecisions from "@/components/DocumentUnitPreviousDecisions.vue"
import DocUnitTexts from "@/components/DocumentUnitTexts.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import DocumentUnit, { CoreData, Texts } from "@/domain/documentUnit"
import { UpdateStatus } from "@/enum/enumUpdateStatus"
import docUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()
const updatedDocUnit = computed(() => props.documentUnit)

const handleUpdateValueDocUnitTexts = async (
  updatedValue: [keyof CoreData | keyof Texts, string]
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  updatedDocUnit.value[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}

const handleUpdateDocUnit = async () => {
  updateStatus.value = UpdateStatus.ON_UPDATE
  const status = (await docUnitService.update(updatedDocUnit.value)).status
  setTimeout(() => {
    hasDataChange.value = false
    lastUpdatedDocUnit.value = JSON.stringify(props.documentUnit)
    updateStatus.value = status
    if (updateStatus.value !== UpdateStatus.SUCCEED) return
  }, 1000)
}
const router = useRouter()
const route = useRoute()

const isOnline = ref(navigator.onLine)
const updateStatus = ref(UpdateStatus.BEFORE_UPDATE)
const lastUpdatedDocUnit = ref(JSON.stringify(props.documentUnit))
const fileAsHTML = ref("")
const automaticUpload = ref()
const hasDataChange = ref(false)
const showDocPanel = ref(useRoute().query.showDocPanel === "true")
const handleToggleFilePanel = async () => {
  showDocPanel.value = !showDocPanel.value
  getOrignalDocUnit()
  await router.push({
    ...route,
    query: { ...route.query, showDocPanel: String(showDocPanel.value) },
  })
}

const coreData = computed({
  get: () => props.documentUnit.coreData,
  set: (newValues) => {
    for (const [key, value] of Object.entries(newValues)) {
      updatedDocUnit.value[key as keyof CoreData] = value
    }
  },
})

const previousDecisions = computed({
  get: () => props.documentUnit.previousDecisions,
  set: (newValue) => (updatedDocUnit.value.previousDecisions = newValue),
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

const getOrignalDocUnit = async () => {
  if (fileAsHTML.value.length > 0) return
  fileAsHTML.value = props.documentUnit.s3path
    ? await fileService.getDocxFileAsHtml(props.documentUnit.s3path)
    : ""
}

/** Overwrite ctrl + S to update docunit */
const handleUpdateDocUnitWithShortCut = (event: KeyboardEvent) => {
  const OS = navigator.userAgent.indexOf("Mac") != -1 ? "Mac" : "Window"
  if (OS === "Mac") {
    if (!event.metaKey) return
    if (event.key !== "s") return
    handleUpdateDocUnit()
    event.preventDefault()
  } else {
    if (!event.ctrlKey) return
    if (event.key !== "s") return
    handleUpdateDocUnit()
    event.preventDefault()
  }
}

/** Time interval to automatic update docunit every 30sec */
/** Only update Docunit when there is any change after 30sec and last update is done */
const autoUpdate = () => {
  automaticUpload.value = setInterval(() => {
    hasDataChange.value =
      JSON.stringify(props.documentUnit) !== lastUpdatedDocUnit.value
    if (
      isOnline.value &&
      hasDataChange.value &&
      updateStatus.value !== UpdateStatus.ON_UPDATE
    ) {
      handleUpdateDocUnit()
    }
    lastUpdatedDocUnit.value = JSON.stringify(props.documentUnit)
    /** Offline mode */
    if (isOnline.value && !navigator.onLine) {
      isOnline.value = false
    }
    if (!isOnline.value && navigator.onLine) {
      isOnline.value = true
      handleUpdateDocUnit()
    }
  }, 30000)
}
/** Clear time Interval */
const removeAutoUpdate = () => {
  clearInterval(automaticUpload.value)
}

onMounted(async () => {
  window.addEventListener("scroll", handleScroll)
  window.addEventListener("keydown", handleUpdateDocUnitWithShortCut, false)
  autoUpdate()
  getOrignalDocUnit()
})
onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
  window.removeEventListener("keydown", handleUpdateDocUnitWithShortCut)
  removeAutoUpdate()
})
</script>

<template>
  <DocumentUnitWrapper :document-unit="documentUnit">
    <v-row>
      <v-col :cols="showDocPanel ? 7 : 9">
        <DocUnitCoreData
          id="coreData"
          v-model="coreData"
          :update-status="updateStatus"
          @update-doc-unit="handleUpdateDocUnit"
        />

        <DocUnitPreviousDecisions
          id="previous-decisions"
          v-model="previousDecisions"
        />

        <DocUnitTexts
          id="texts"
          :texts="documentUnit.texts"
          :update-status="updateStatus"
          @update-value="handleUpdateValueDocUnitTexts"
          @update-doc-unit="handleUpdateDocUnit"
        />
      </v-col>
      <OriginalFileSidePanel
        :open="showDocPanel"
        :has-file="documentUnit.hasFile"
        :file="fileAsHTML"
        @toggle-panel="handleToggleFilePanel"
      />
    </v-row>
  </DocumentUnitWrapper>
</template>

<style scoped>
#previous-decisions {
  padding: 2rem 1.1rem;
}
</style>
