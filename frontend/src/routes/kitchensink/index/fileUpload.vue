<script lang="ts" setup>
import FileUpload from "@/components/FileUpload.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="File upload">
    <FileUpload document-unit-uuid="uuid" />
  </KitchensinkPage>
</template>
