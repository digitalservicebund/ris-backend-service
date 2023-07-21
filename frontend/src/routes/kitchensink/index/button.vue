<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
</script>

<template>
  <KitchensinkPage name="Button">
    <KitchensinkStory name="Primary">
      <div class="flex gap-20">
        <TextButton button-type="primary" label="Button Primary" />
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Primary With Icon"
        />
        <TextButton button-type="primary" icon="close" />
        <TextButton button-type="primary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Secondary">
      <div class="flex gap-20">
        <TextButton button-type="secondary" label="Button Secondary" />
        <TextButton
          button-type="secondary"
          icon="close"
          label="Button Secondary With Icon"
        />
        <TextButton button-type="secondary" icon="close" />
        <TextButton button-type="secondary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Tertiary">
      <div class="flex gap-20">
        <TextButton button-type="tertiary" label="Button Tertiary" />
        <TextButton
          button-type="tertiary"
          icon="close"
          label="Button Tertiary With Icon"
        />
        <TextButton button-type="tertiary" icon="close" />
        <TextButton button-type="tertiary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Ghost">
      <div class="flex gap-20">
        <TextButton button-type="ghost" label="Button Ghost" />
        <TextButton
          button-type="ghost"
          icon="close"
          label="Button Ghost With Icon"
        />
        <TextButton button-type="ghost" icon="close" />
        <TextButton button-type="ghost" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Medium">
      <div class="flex gap-20">
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Medium"
          size="medium"
        />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Small">
      <div class="flex gap-20">
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Small"
          size="small"
        />
      </div>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
