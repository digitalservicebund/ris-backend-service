<script setup lang="ts">
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

defineProps<{
  name?: string
}>()
</script>

<template>
  <div class="flex flex-row gap-8">
    <IconPermIdentity />
    {{ name }}
  </div>
</template>
