<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocUnitDetail from "./index.vue"
import DocUnitCoreData from "@/components/DocUnitCoreData.vue"
import DocUnitTexts from "@/components/DocUnitTexts.vue"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"
import { CoreData, Texts } from "@/domain/docUnit"
import docUnitService from "@/services/docUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  await docUnitService.getByDocumentNumber(props.documentNumber)
)

const handleUpdateValue = async (
  updatedValue: [keyof CoreData | keyof Texts, string]
) => {
  docUnit.value[updatedValue[0]] = updatedValue[1]
}

const handleUpdateDocUnit = async () => {
  await docUnitService.update(docUnit.value)
  alert("Dokumentationseinheit wurden gespeichert")
}

const router = useRouter()
const route = useRoute()

const fetchOriginalFile = async () => {
  if (
    !docUnit.value.originalFileAsHTML &&
    showDocPanel.value &&
    docUnit.value.s3path
  ) {
    docUnit.value.originalFileAsHTML = await fileService.getDocxFileAsHtml(
      docUnit.value.s3path
    )
  }
}

const showDocPanel = ref(useRoute().query.showDocPanel === "true")
const handleToggleFilePanel = async () => {
  showDocPanel.value = !showDocPanel.value
  fetchOriginalFile()
  await router.push({
    ...route,
    query: { ...route.query, showDocPanel: String(showDocPanel.value) },
  })
}

onMounted(async () => {
  fetchOriginalFile()
})

const originalOdocPanelYPos = 169 // read this dynamically, see onUpdated() TODO

const handleScroll = () => {
  const element = document.getElementById("odoc-panel-element")
  if (!element) return
  const pos = originalOdocPanelYPos - window.scrollY
  const threshold = -40 // this should also not be hardwired TODO
  element.style.top = (pos < threshold ? threshold : pos) + "px"
}

onMounted(() => window.addEventListener("scroll", handleScroll))
onUnmounted(() => window.removeEventListener("scroll", handleScroll))
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <v-row>
      <v-col :cols="showDocPanel ? 7 : 9">
        <DocUnitCoreData
          id="coreData"
          :core-data="docUnit.coreData"
          @update-value="handleUpdateValue"
          @update-doc-unit="handleUpdateDocUnit"
        />
        <DocUnitTexts
          id="texts"
          :texts="docUnit.texts"
          @update-value="handleUpdateValue"
          @update-doc-unit="handleUpdateDocUnit"
        />
      </v-col>
      <OriginalFileSidePanel
        :visible="showDocPanel"
        :file="docUnit.originalFileAsHTML"
        @toggle-panel="handleToggleFilePanel"
      />
    </v-row>
  </DocUnitDetail>
</template>
