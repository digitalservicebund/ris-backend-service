<script lang="ts" setup>
import FileViewer from "@/components/FileViewer.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="File viewer">
    <FileViewer
      file-name="File name"
      file-type="Document type"
      upload-time-stamp="01.01.2022 10:00"
      uuid="uuid"
    />
  </KitchensinkPage>
</template>
