<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { DocumentSection } from "@/domain/norm"

const props = defineProps<{
  documentSections: DocumentSection[]
}>()
</script>

<template>
  <ExpandableContent
    v-for="doc in props.documentSections"
    :key="doc.guid"
    class="border-gray-400"
    close-icon-name="expand_less"
    icons-on-left
    is-expanded
    open-icon-name="expand_more"
  >
    <p class="pl-24">{{ doc.marker }}</p>
    <p class="pl-24">{{ doc.heading }}</p>
  </ExpandableContent>
</template>
