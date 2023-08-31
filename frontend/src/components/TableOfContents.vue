<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Article, DocumentSection, isDocumentSection } from "@/domain/norm"

const props = defineProps<{
  documentSections: (Article | DocumentSection)[] | undefined
}>()
</script>

<template>
  <div v-for="doc in props.documentSections" :key="doc.guid">
    <ExpandableContent
      v-if="isDocumentSection(doc)"
      class="bg-blue-200 p-16"
      close-icon-name="expand_less"
      icons-on-left
      is-expanded
      open-icon-name="expand_more"
    >
      <template #header>
        <h2 class="ds-label-02-bold w-[15rem] flex-none text-left">
          {{ doc.marker }} {{ doc.heading }}
        </h2>
      </template>
      <TableOfContents :document-sections="doc.documentation" />
    </ExpandableContent>
    <div v-else class="border-b border-t border-gray-400 pb-4 pt-4">
      <p class="pl-20">{{ doc.marker }} {{ doc.heading }}</p>
    </div>
  </div>
</template>
