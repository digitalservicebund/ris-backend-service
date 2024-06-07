<script setup lang="ts">
import { FieldOfLaw } from "@/domain/fieldOfLaw"

defineProps<{
  node: FieldOfLaw
}>()
</script>

<template>
  <FieldOfLawNodeView v-if="node.parent" :node="node.parent"
    >→
  </FieldOfLawNodeView>
  <span> {{ node.text }} </span>
  <slot></slot>
</template>
