<script setup lang="ts">
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

function addWordToDocOffice() {
  console.log(props.match.word)
}
</script>
<template>
  <div>
    <button class="ds-link-01-bold" @click="addWordToDocOffice">
      Zum globalen Wörterbuch hinzufügen
    </button>
  </div>
</template>
