<script lang="ts" setup>
import { ref } from "vue"
import SaveDocumentUnitButton from "@/components/SaveDocumentUnitButton.vue"
import { UpdateStatus } from "@/enum/enumUpdateStatus"
import TextButton from "@/shared/components/input/TextButton.vue"

const updateStatus = ref(UpdateStatus)
</script>

<template>
  <div class="flex flex-col gap-20 items-start w-full">
    <h2 class="heading-02-regular">Text Button</h2>

    <div class="flex gap-20 w-full">
      <TextButton button-type="primary" label="Button Primary" />

      <TextButton
        button-type="primary"
        icon="close"
        label="Button Primary With Icon"
      />

      <TextButton button-type="primary" icon="close" />

      <TextButton button-type="primary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 w-full">
      <TextButton button-type="secondary" label="Button Secondary" />

      <TextButton
        button-type="secondary"
        icon="close"
        label="Button Secondary With Icon"
      />

      <TextButton button-type="secondary" icon="close" />

      <TextButton button-type="secondary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 w-full">
      <TextButton button-type="tertiary" label="Button Tertiary" />

      <TextButton
        button-type="tertiary"
        icon="close"
        label="Button Tertiary With Icon"
      />

      <TextButton button-type="tertiary" icon="close" />

      <TextButton button-type="tertiary" disabled label="Disabled" />
    </div>

    <div class="flex gap-20 w-full">
      <TextButton button-type="ghost" label="Button Ghost" />

      <TextButton
        button-type="ghost"
        icon="close"
        label="Button Ghost With Icon"
      />

      <TextButton button-type="ghost" icon="close" />

      <TextButton button-type="ghost" disabled label="Disabled" />
    </div>

    <div class="flex flex-col gap-y-20 items-start">
      <h2 class="heading-02-regular">Save Docunit Button</h2>
      <p class="text-20">Status: Before update</p>
      <SaveDocumentUnitButton
        aria-label="Save Document Unit"
        :update-status="updateStatus.BEFORE_UPDATE"
      />
      <p class="text-20">Status: On update</p>
      <SaveDocumentUnitButton
        aria-label="Save Document Unit"
        :update-status="updateStatus.ON_UPDATE"
      />
      <p class="text-20">Status: Error</p>
      <SaveDocumentUnitButton
        aria-label="Save Document Unit"
        :update-status="updateStatus.ERROR"
      />
      <p class="text-20">Status: Succeed</p>
      <SaveDocumentUnitButton
        aria-label="Save Document Unit"
        :update-status="updateStatus.SUCCEED"
      />
    </div>
  </div>
</template>
