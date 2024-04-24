<script lang="ts" setup>
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="Loading Spinner">
    <div class="flex w-full flex-col items-center">
      <LoadingSpinner />
    </div>
  </KitchensinkPage>
</template>
