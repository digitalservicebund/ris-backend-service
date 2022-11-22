<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import TextButton from "@/components/TextButton.vue"
import { DocumentUnitListEntry } from "@/domain/DocumentUnitListEntry"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

const documentUnitListEntries = ref(
  (await documentUnitService.getAllListEntries()).data
)

async function handleDelete(documentUnitListEntry: DocumentUnitListEntry) {
  if (documentUnitListEntries.value) {
    const status = (
      await documentUnitService.delete(documentUnitListEntry.uuid)
    ).status
    if (status === 200) {
      documentUnitListEntries.value = documentUnitListEntries.value.filter(
        (item) => item != documentUnitListEntry
      )
    }
  }
}
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex justify-between">
      <h1 class="heading-02-regular">Übersicht Rechtsprechung</h1>

      <TextButton
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'caselaw-documentUnit-new' })"
      />
    </div>

    <DocumentUnitList
      v-if="documentUnitListEntries"
      class="grow"
      :document-unit-list-entries="documentUnitListEntries"
      @delete-document-unit="handleDelete"
    />
  </div>
</template>
