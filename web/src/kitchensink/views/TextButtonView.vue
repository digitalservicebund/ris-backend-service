<script lang="ts" setup>
import TextButton from "@/components/TextButton.vue"
</script>

<template>
  <div class="flex flex-col gap-y-20 items-start">
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[25%]">
        <TextButton button-type="primary" label="Button Primary" />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="primary"
          disabled
          label="Button Primary Disabled"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Primary With Icon"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton button-type="primary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[25%]">
        <TextButton button-type="secondary" label="Button Secondary" />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="secondary"
          disabled
          label="Button Secondary Disabled"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="secondary"
          icon="close"
          label="Button Secondary With Icon"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton button-type="secondary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[25%]">
        <TextButton button-type="tertiary" label="Button Tertiary" />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="tertiary"
          disabled
          label="Button Tertiary Disabled"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="tertiary"
          icon="close"
          label="Button Tertiary With Icon"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton button-type="tertiary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[25%]">
        <TextButton button-type="ghost" label="Button Ghost" />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="ghost"
          disabled
          label="Button Ghost Disabled"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton
          button-type="ghost"
          icon="close"
          label="Button Ghost With Icon"
        />
      </div>
      <div class="min-w-[25%]">
        <TextButton button-type="ghost" icon="close" label="" />
      </div>
    </div>
  </div>
</template>
