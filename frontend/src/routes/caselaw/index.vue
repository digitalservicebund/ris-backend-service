<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import { DocumentUnitListEntry } from "@/domain/documentUnitListEntry"
import service from "@/services/documentUnitService"
import TextButton from "@/shared/components/input/TextButton.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const router = useRouter()
const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()

const itemsPerPage = 30

async function getEntries(page: number) {
  const response = await service.getAllListEntries(page, itemsPerPage)
  if (response.data) {
    documentUnitListEntries.value = response.data.content
    currentPage.value = response.data
  } else {
    console.error("could not load list entries")
  }
}

async function handleDelete(documentUnitListEntry: DocumentUnitListEntry) {
  if (documentUnitListEntries.value) {
    const response = await service.delete(documentUnitListEntry.uuid)
    if (response.status === 200) {
      documentUnitListEntries.value = documentUnitListEntries.value.filter(
        (item) => item != documentUnitListEntry
      )
    } else {
      alert("Fehler beim Löschen der Dokumentationseinheit: " + response.data)
    }
  }
}

onMounted(async () => {
  await getEntries(0)
})
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

    <Pagination
      v-if="currentPage"
      navigation-position="bottom"
      :page="currentPage"
      @update-page="getEntries"
    >
      <DocumentUnitList
        v-if="documentUnitListEntries"
        class="grow"
        :document-unit-list-entries="documentUnitListEntries"
        @delete-document-unit="handleDelete"
      />
    </Pagination>
  </div>
</template>
