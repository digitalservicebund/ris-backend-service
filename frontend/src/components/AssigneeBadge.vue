<script setup lang="ts">
import IconBadge from "@/components/IconBadge.vue"
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

defineProps<{
  name?: string
}>()
</script>

<template>
  <div class="flex flex-row gap-8">
    <IconBadge
      class="inline-flex whitespace-nowrap"
      data-testid="assigned-person"
      :icon="IconPermIdentity"
      :label="name || '-'"
    />
  </div>
</template>
