<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import InfoModal from "@/shared/components/InfoModal.vue"
</script>

<template>
  <KitchensinkPage name="Error">
    <InfoModal
      description="Diese Seite wird gerade eingebaut. Vesuchen Sie es später erneut!"
      title="Es ist ein Fehler aufgetreten"
    />
  </KitchensinkPage>
</template>
