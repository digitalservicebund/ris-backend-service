<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import FileUpload from "@/shared/components/FileUpload.vue"
</script>

<template>
  <KitchensinkPage name="File upload">
    <FileUpload document-unit-uuid="uuid" />
  </KitchensinkPage>
</template>
