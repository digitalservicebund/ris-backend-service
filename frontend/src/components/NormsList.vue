<script lang="ts" setup>
import { sanitizeTableOfContentEntry } from "@/helpers/sanitizer"

interface Norm {
  officialLongTitle: string
  guid: string
}

defineProps<{ norms: Norm[] }>()

function formatTitle(title?: string): string {
  const titleIsMissing = title === undefined || title.length <= 0
  return titleIsMissing ? "Kein Titel" : sanitizeTableOfContentEntry(title)
}
</script>
<template>
  <div v-if="norms.length" class="pl-64">
    <div v-for="norm in norms" :key="norm.guid">
      <div class="mb-24">
        <router-link
          class="ds-heading-03-reg"
          :to="{
            name: 'norms-norm-normGuid-content',
            params: { normGuid: norm.guid },
          }"
        >
          {{ formatTitle(norm.officialLongTitle) }}
        </router-link>
      </div>
    </div>
  </div>
</template>
