<script lang="ts" setup>
import { ref } from "vue"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { Suggestion } from "@/types/languagetool"

const store = useDocumentUnitStore()

const errors = ref<Suggestion[]>()

const checkAll = async () => {
  if (store.documentUnit) {
    const response = await languageToolService.checkAll(store.documentUnit.uuid)

    errors.value = response.data?.suggestions
  }
}
await checkAll()
</script>

<template>
  <div class="w-full grow p-24">
    <div v-for="error in errors" :key="error.word">
      <div class="mb-16 bg-blue-100 p-24">
        {{ error.word }}
      </div>
    </div>
  </div>
</template>
