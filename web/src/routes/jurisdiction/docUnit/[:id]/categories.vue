<script setup lang="ts">
import { ref } from "vue"
import DocUnitDetail from "./index.vue"
import DocUnitCoreData from "@/components/DocUnitCoreData.vue"
import DocUnitTexts from "@/components/DocUnitTexts.vue"
import { CoreData, Texts } from "@/domain/docUnit"
import docUnitService from "@/services/docUnitService"

const props = defineProps<{ id: string }>()
const docUnit = ref(await docUnitService.getById(props.id))

const handleUpdateValue = async (
  updatedValue: [keyof CoreData | keyof Texts, string]
) => {
  docUnit.value[updatedValue[0]] = updatedValue[1]
}

const handleUpdateDocUnit = async () => {
  await docUnitService.update(docUnit.value)
  alert("Dokumentationseinheit wurden gespeichert")
}
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
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
  </DocUnitDetail>
</template>
