<script setup lang="ts">
import IconBadge from "@/components/IconBadge.vue"
import IconPerson from "~icons/ic/baseline-person"

defineProps<{
  name?: string
}>()
</script>

<template>
  <div class="flex flex-row gap-8">
    <IconBadge
      class="inline-flex whitespace-nowrap"
      data-testid="assigned-person"
      :icon="IconPerson"
      :label="name || '-'"
    />
  </div>
</template>
