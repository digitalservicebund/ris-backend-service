<script setup lang="ts">
import TextButton from "@/components/input/TextButton.vue"
import router from "@/router"

interface ErrorPageProps {
  title: string
  description?: string
}

const props = defineProps<ErrorPageProps>()
</script>

<template>
  <div class="ml-48 mt-48 gap-16 p-16">
    <h1 class="ds-heading-02-reg mb-32">{{ props.title }}</h1>
    <p class="mb-16">
      <strong>Haben Sie die Internetadresse direkt eingegeben?</strong><br />
      Überprüfen Sie die Schreibweise.
    </p>
    <p class="mb-32">
      <strong>Haben Sie die Internetadresse kopiert?</strong><br />
      Überprüfen Sie, ob Sie die gesamte Adresse kopiert haben.
    </p>
    <TextButton
      aria-label="Zurück zur Startseite"
      class="w-auto"
      label="Zurück zur Startseite"
      @click="router.push({ name: 'caselaw' })"
    />
  </div>
</template>
