<template>
  <va-button> {{ label }} </va-button>
</template>

<script lang="ts" setup>
import { ref } from "vue"
import { VaButton } from "vuestic-ui"

const label = ref("button")
</script>

<style scoped></style>
