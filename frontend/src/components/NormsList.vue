<script lang="ts" setup>
interface Norm {
  officialLongTitle: string
  guid: string
}

defineProps<{ norms: Norm[] }>()

function formatTitle(title?: string): string {
  const titleIsMissing = title === undefined || title.length <= 0
  return titleIsMissing ? "Kein Titel" : title
}
</script>
<template>
  <div v-if="norms.length" class="pl-64">
    <div v-for="norm in norms" :key="norm.guid">
      <div class="mb-24">
        <router-link
          class="heading-03-regular"
          :to="{
            name: 'norms-norm-:normGuid',
            params: { normGuid: norm.guid },
          }"
        >
          {{ formatTitle(norm.officialLongTitle) }}
        </router-link>
      </div>
    </div>
  </div>
</template>
