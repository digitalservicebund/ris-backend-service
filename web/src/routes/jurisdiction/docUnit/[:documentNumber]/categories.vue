<script setup lang="ts">
import { ref, onMounted, onUnmounted, toRefs } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useScrollToHash } from "../../../../composables/useScrollToHash"
import DocUnitDetail from "./index.vue"
import DocUnitCoreData from "@/components/DocUnitCoreData.vue"
import DocUnitTexts from "@/components/DocUnitTexts.vue"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import { CoreData, Texts } from "@/domain/docUnit"
import { UpdateStatus } from "@/enum/enumUpdateStatus"
import docUnitService from "@/services/docUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  await docUnitService.getByDocumentNumber(props.documentNumber)
)

const handleUpdateValueDataCores = async (
  updatedValue: [keyof CoreData | keyof Texts, string]
) => {
  docUnit.value[updatedValue[0]] = updatedValue[1]
}

const handleUpdateValueDocUnitTexts = async (
  updatedValue: [keyof CoreData | keyof Texts, string]
) => {
  const divElem = document.createElement("div")
  divElem.innerHTML = updatedValue[1]
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.length > 0
  docUnit.value[updatedValue[0]] =
    hasInnerText || hasImgElem || hasTable ? updatedValue[1] : ""
}

const handleUpdateDocUnit = async () => {
  updateStatus.value = UpdateStatus.ON_UPDATE
  const status = await docUnitService.update(docUnit.value)
  setTimeout(() => {
    hasDataChange.value = false
    lastUpdatedDocUnit.value = JSON.stringify(docUnit.value)
    updateStatus.value = status
    if (updateStatus.value !== UpdateStatus.SUCCEED) return
  }, 3000)
}
const router = useRouter()
const route = useRoute()

const updateStatus = ref(UpdateStatus.BEFORE_UPDATE)
const lastUpdatedDocUnit = ref(JSON.stringify(docUnit.value))
const fileAsHTML = ref("")
const automaticUpload = ref()
const hasDataChange = ref(false)
const showDocPanel = ref(useRoute().query.showDocPanel === "true")
const handleToggleFilePanel = async () => {
  showDocPanel.value = !showDocPanel.value
  await router.push({
    ...route,
    query: { ...route.query, showDocPanel: String(showDocPanel.value) },
  })
}

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
      JSON.stringify(docUnit.value) !== lastUpdatedDocUnit.value
    if (hasDataChange.value && updateStatus.value !== UpdateStatus.ON_UPDATE) {
      handleUpdateDocUnit()
    }
    lastUpdatedDocUnit.value = JSON.stringify(docUnit.value)
  }, 30000)
}
/** Clear time Interval */
const removeAutoUpdate = () => {
  clearInterval(automaticUpload.value)
}

onMounted(async () => {
  window.addEventListener("scroll", handleScroll)
  window.addEventListener("keydown", handleUpdateDocUnitWithShortCut, false)
  fileAsHTML.value = docUnit.value.s3path
    ? await fileService.getDocxFileAsHtml(docUnit.value.s3path)
    : ""
  autoUpdate()
})
onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
  window.removeEventListener("keydown", handleUpdateDocUnitWithShortCut)
  removeAutoUpdate()
})
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <v-row>
      <v-col :cols="showDocPanel ? 7 : 9">
        <DocUnitCoreData
          id="coreData"
          :core-data="docUnit.coreData"
          :update-status="updateStatus"
          @update-value="handleUpdateValueDataCores"
          @update-doc-unit="handleUpdateDocUnit"
        />
        <DocUnitTexts
          id="texts"
          :texts="docUnit.texts"
          :update-status="updateStatus"
          @update-value="handleUpdateValueDocUnitTexts"
          @update-doc-unit="handleUpdateDocUnit"
        />
      </v-col>
      <OriginalFileSidePanel
        :open="showDocPanel"
        :has-file="docUnit.hasFile"
        :file="fileAsHTML"
        @toggle-panel="handleToggleFilePanel"
      />
    </v-row>
  </DocUnitDetail>
</template>
