<script lang="ts" setup>
import FileUpload from "@/components/FileUpload.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="File upload">
    <FileUpload id="id" aria-label="label" document-unit-uuid="uuid" />
  </KitchensinkPage>
</template>
