<script lang="ts" setup>
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ error: ResponseError | undefined }>()
</script>

<template>
  <div class="m-24">
    <h2>{{ props.error?.title }}</h2>
    <p>{{ props.error?.description }}</p>
    <div class="mt-20">
      <router-link class="underline" :to="{ name: 'caselaw' }">
        Zurück zur Übersicht
      </router-link>
    </div>
  </div>
</template>
