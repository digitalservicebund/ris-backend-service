<script lang="ts" setup>
import InfoModal from "@/components/InfoModal.vue"
import { InfoStatus } from "@/components/utils/enumInfoStatus"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
</script>

<template>
  <KitchensinkPage name="Info modal">
    <KitchensinkStory name="Error">
      <InfoModal
        description="The messages are shown here."
        title="The messages title is shown here"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Success">
      <InfoModal
        description="The messages are shown here."
        :status="InfoStatus.SUCCEED"
        title="The messages title is shown here"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
