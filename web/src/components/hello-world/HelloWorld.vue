<script lang="ts" setup>
import { ref } from "vue"

const message = ref("Hello NeuRIS")
</script>

<template>
  <v-container>
    <v-row class="text-center">
      <v-col class="mb-4">
        <h1 class="display-2 font-weight-bold mb-3">
          {{ message }}
        </h1>
      </v-col>
    </v-row>
  </v-container>
</template>
