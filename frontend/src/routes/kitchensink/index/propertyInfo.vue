<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import PropertyInfo from "@/shared/components/PropertyInfo.vue"
</script>

<template>
  <KitchensinkPage name="Property info">
    <KitchensinkStory name="Row">
      <PropertyInfo direction="row" label="myLabel" value="value" />
    </KitchensinkStory>

    <KitchensinkStory name="Column">
      <PropertyInfo direction="column" label="myLabel" value="value" />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
