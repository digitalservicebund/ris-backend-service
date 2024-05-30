<script setup lang="ts">
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

defineProps<{
  node: FieldOfLawNode
}>()
</script>

<template>
  <FieldOfLawNodeView v-if="node.parent" :node="node.parent"
    >→
  </FieldOfLawNodeView>
  <span> {{ node.text }} </span> <slot></slot>
</template>
