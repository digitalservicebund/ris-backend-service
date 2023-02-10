<script lang="ts" setup>
function expand() {
  console.log("expand")
}
// TODO: service call to fetch tree data
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <button class="align-middle pr-4 text-white" @click="expand">
    <span
      aria-label="Sachgebietsbaum aufklappen"
      class="bg-blue-800 material-icons rounded-full w-icon"
      >add</span
    >
  </button>
  Alle Sachgebiete anzeigen
</template>
