<script lang="ts" setup>
import FileUpload from "@/components/FileUpload.vue"
</script>

<template>
  <div class="bg-gray-100 flex justify-center">
    <div>
      <div class="pb-[5rem] pt-[4rem]">
        <h1 class="heading-02-regular">Neue Dokumentationseinheit erstellen</h1>
        <p>
          Importieren Sie eine Zip-Datei, um die Dokumentationseinheit zu
          erstellen.
        </p>
      </div>
      <FileUpload />
    </div>
  </div>
</template>
