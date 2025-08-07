<script setup lang="ts">
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

defineProps<{
  name?: string
}>()

function getInitials(name?: string): string {
  if (!name) return "-"

  const words = name.trim().toUpperCase().split(/\s+/).filter(Boolean)
  if (words.length === 0) return "-"
  if (words.length === 1) return words[0].charAt(0)
  return words[0].charAt(0) + words[words.length - 1].charAt(0)
}
</script>

<template>
  <div class="flex flex-row gap-8">
    <IconPermIdentity />
    {{ getInitials(name) }}
  </div>
</template>
