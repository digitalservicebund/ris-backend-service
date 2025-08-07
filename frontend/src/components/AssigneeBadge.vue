<script setup lang="ts">
import StringsUtil from "@/utils/stringsUtil"
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

defineProps<{
  name?: string
}>()
</script>

<template>
  <div class="flex flex-row gap-8">
    <IconPermIdentity />
    {{ StringsUtil.getFirstAndLastChars(name) }}
  </div>
</template>
