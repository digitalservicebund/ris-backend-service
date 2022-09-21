<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import TextButton from "@/components/TextButton.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

const documentUnits = ref((await documentUnitService.getAll()).data)

const handleDelete = async (documentUnit: DocumentUnit) => {
  const status = (await documentUnitService.delete(documentUnit.uuid)).status
  if (status === 200) {
    documentUnits.value = documentUnits.value.filter(
      (item) => item != documentUnit
    )
  }
}
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex justify-between">
      <h1 class="heading-02-regular">Übersicht Rechtsprechung</h1>

      <TextButton
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'jurisdiction-documentUnit-new' })"
      />
    </div>

    <DocumentUnitList
      class="grow"
      :document-units="documentUnits"
      @delete-document-unit="handleDelete"
    />
  </div>
</template>
