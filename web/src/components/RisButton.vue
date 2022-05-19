<script lang="ts" setup>
import { ref } from "vue"

const label = ref("button")
</script>

<template>
  <v-btn :ripple="false" :flat="true" color="blue800"> {{ label }} </v-btn>
</template>

<style scoped lang="scss">
.v-btn {
  border-radius: $btn-border-radius;
}
</style>
