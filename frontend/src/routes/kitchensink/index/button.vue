<script lang="ts" setup>
import TextButton from "@/components/input/TextButton.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import IconClear from "~icons/ic/baseline-clear"
</script>

<template>
  <KitchensinkPage name="Button">
    <KitchensinkStory name="Primary">
      <div class="flex gap-20">
        <TextButton button-type="primary" label="Button Primary" />
        <TextButton
          button-type="primary"
          :icon="IconClear"
          label="Button Primary With Icon"
        />
        <TextButton
          button-type="primary"
          :icon="IconClear"
          icon-position="right"
          label="Button Primary With Icon on the Right"
        />
        <TextButton button-type="primary" :icon="IconClear" />
        <TextButton button-type="primary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Secondary">
      <div class="flex gap-20">
        <TextButton button-type="secondary" label="Button Secondary" />
        <TextButton
          button-type="secondary"
          :icon="IconClear"
          label="Button Secondary With Icon"
        />
        <TextButton
          button-type="secondary"
          :icon="IconClear"
          icon-position="right"
          label="Button Primary With Icon on the Right"
        />
        <TextButton button-type="secondary" :icon="IconClear" />
        <TextButton button-type="secondary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Tertiary">
      <div class="flex gap-20">
        <TextButton button-type="tertiary" label="Button Tertiary" />
        <TextButton
          button-type="tertiary"
          :icon="IconClear"
          label="Button Tertiary With Icon"
        />
        <TextButton
          button-type="tertiary"
          :icon="IconClear"
          icon-position="right"
          label="Button Primary With Icon on the Right"
        />
        <TextButton button-type="tertiary" :icon="IconClear" />
        <TextButton button-type="tertiary" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Ghost">
      <div class="flex gap-20">
        <TextButton button-type="ghost" label="Button Ghost" />
        <TextButton
          button-type="ghost"
          :icon="IconClear"
          label="Button Ghost With Icon"
        />
        <TextButton
          button-type="ghost"
          :icon="IconClear"
          icon-position="right"
          label="Button Primary With Icon on the Right"
        />
        <TextButton button-type="ghost" :icon="IconClear" />
        <TextButton button-type="ghost" disabled label="Disabled" />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Medium">
      <div class="flex gap-20">
        <TextButton
          button-type="primary"
          :icon="IconClear"
          label="Button Medium"
          size="medium"
        />
      </div>
    </KitchensinkStory>

    <KitchensinkStory name="Small">
      <div class="flex gap-20">
        <TextButton
          button-type="primary"
          :icon="IconClear"
          label="Button Small"
          size="small"
        />
      </div>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
