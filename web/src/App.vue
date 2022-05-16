<script setup lang="ts">
import { ref } from "vue"
import { VaButton } from "vuestic-ui"

const label = ref("button")

</script>

<template>
  <h1 class="text-2xl text-center text-blue-800 m-4">Hello DigitalService!</h1>
  <va-button> {{ label }} </va-button>
  <va-button disabled size="large"> {{ label }} </va-button>
  <va-button size="small"> {{ label }} </va-button>
</template>
