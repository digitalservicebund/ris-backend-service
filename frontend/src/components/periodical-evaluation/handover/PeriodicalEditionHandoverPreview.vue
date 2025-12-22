<script lang="ts" setup>
import Message from "primevue/message"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Preview } from "@/domain/eventRecord"
import { ResponseError } from "@/services/httpClient"

defineProps<{
  preview?: Preview[]
  previewError?: ResponseError
}>()
</script>

<template>
  <div>
    <!-- Preview -->
    <ExpandableContent
      v-if="preview && preview?.length > 0"
      aria-label="XML Vorschau"
      as-column
      class="border-b-1 border-gray-400 pb-24"
      :data-set="preview"
      header="XML Vorschau"
      header-class="ris-body1-bold"
      :is-expanded="false"
      title="XML Vorschau"
    >
      <CodeSnippet
        v-for="(item, index) in preview"
        :key="index"
        class="mb-16"
        title=""
        :xml="item.xml!"
      />
    </ExpandableContent>
    <Message
      v-else-if="previewError"
      aria-label="Fehler beim Laden der Preview"
      class="mt-8"
      severity="error"
    >
      <p class="ris-body1-bold">{{ previewError.title }}</p>
      <p>{{ previewError.description }}</p>
    </Message>
  </div>
</template>
