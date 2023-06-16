<script lang="ts" setup>
import TextButton from "@/shared/components/input/TextButton.vue"
</script>

<template>
  <div class="flex flex-col gap-20 items-start w-full">
    <h2 class="heading-02-regular">Text Button</h2>

    <div class="flex gap-20 items-start w-full">
      <TextButton button-type="primary" label="Button Primary" />

      <TextButton
        button-type="primary"
        icon="close"
        label="Button Primary With Icon"
      />

      <TextButton button-type="primary" icon="close" />

      <TextButton button-type="primary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 items-start w-full">
      <TextButton
        button-type="primary"
        icon="close"
        label="Button Small"
        size="small"
      />
      <TextButton
        button-type="primary"
        icon="close"
        label="Button Medium"
        size="medium"
      />
    </div>

    <div class="flex gap-20 items-start w-full">
      <TextButton button-type="secondary" label="Button Secondary" />

      <TextButton
        button-type="secondary"
        icon="close"
        label="Button Secondary With Icon"
      />

      <TextButton button-type="secondary" icon="close" />

      <TextButton button-type="secondary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 items-start w-full">
      <TextButton button-type="tertiary" label="Button Tertiary" />

      <TextButton
        button-type="tertiary"
        icon="close"
        label="Button Tertiary With Icon"
      />

      <TextButton button-type="tertiary" icon="close" />

      <TextButton button-type="tertiary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 items-start w-full">
      <TextButton button-type="ghost" label="Button Ghost" />

      <TextButton
        button-type="ghost"
        icon="close"
        label="Button Ghost With Icon"
      />

      <TextButton button-type="ghost" icon="close" />

      <TextButton button-type="ghost" disabled label="Disabled" />
    </div>
  </div>
</template>
