<script lang="ts" setup>
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
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

    <KitchensinkStory name="Error multipart">
      <InfoModal
        :description="['The messages', 'are shown here']"
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

    <KitchensinkStory name="Success multipart">
      <InfoModal
        :description="['The messages', 'are shown here']"
        :status="InfoStatus.SUCCEED"
        title="The messages title is shown here"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Info">
      <InfoModal
        description="The messages are shown here"
        :status="InfoStatus.INFO"
        title="The messages title is shown here"
      />
    </KitchensinkStory>

    <KitchensinkStory name="Info multipart">
      <InfoModal
        :description="['The messages', 'are shown here']"
        :status="InfoStatus.INFO"
        title="The messages title is shown here"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
