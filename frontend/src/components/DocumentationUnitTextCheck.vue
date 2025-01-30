<script lang="ts" setup>
import { ref } from "vue"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { Suggestion } from "@/types/languagetool"

const store = useDocumentUnitStore()

const errors = ref<Suggestion[]>()
const errorCount = ref(0)

const checkAll = async () => {
  if (store.documentUnit) {
    const response = await languageToolService.checkAll(store.documentUnit.uuid)

    errors.value = response.data?.suggestions

    let counter = 0
    response.data?.suggestions.forEach(
      (suggestion) => (counter += suggestion.matches.length),
    )
    errorCount.value = counter
  }
}
await checkAll()
</script>

<template>
  <div class="w-full grow p-24">
    <div>Rechtschreibprüfung ({{ errorCount }})</div>
    <div v-for="error in errors" :key="error.word">
      <div class="mb-16 bg-blue-100 p-24">
        <div class="ds-label-01-bold">
          {{ error.word }}
          <span v-if="error.matches.length > 1">
            {{ error.matches.length }}
          </span>
        </div>
        <div>
          <span v-if="error.matches.length == 1" class="ds-link-01-bold">
            Ignorieren
          </span>
          <span v-if="error.matches.length > 1" class="ds-link-01-bold">
            Alle ignorieren
          </span>
          |
          <span class="ds-link-01-bold">
            Zum globalen Wörterbuch hinzufügen
          </span>
        </div>
        <div>
          {{ error.matches[0].message }}
        </div>
      </div>
    </div>
  </div>
</template>
