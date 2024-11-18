<script setup lang="ts">
import FieldOfLawListEntry from "@/components/FieldOfLawListEntry.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import errorMessages from "@/i18n/errors.json"

defineProps<{
  currentPage?: Page<FieldOfLaw>
  results?: FieldOfLaw[]
}>()

const emit = defineEmits<{
  search: [page: number]
  "node:select": [node: FieldOfLaw]
  "linkedField:select": [node: FieldOfLaw]
}>()
</script>

<template>
  <div v-if="currentPage" class="flex flex-1 flex-col">
    <Pagination
      navigation-position="bottom"
      :page="currentPage"
      @update-page="(page: number) => emit('search', page)"
    >
      <FieldOfLawListEntry
        v-for="(fieldOfLawNode, idx) in results"
        :key="idx"
        :field-of-law="fieldOfLawNode"
        @linked-field:select="emit('linkedField:select', $event)"
        @node:select="emit('node:select', fieldOfLawNode)"
      />
    </Pagination>
    <div v-if="!currentPage?.content || currentPage?.content?.length == 0">
      {{ errorMessages.SEARCH_RESULTS_NOT_FOUND.title }}
    </div>
  </div>
</template>
