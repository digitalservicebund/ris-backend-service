<script lang="ts" setup>
import { ref } from "vue"
import FileInputButton from "@/components/FileInputButton.vue"
import SaveDocumentUnitButton from "@/components/SaveDocumentUnitButton.vue"
import TextButton from "@/components/TextButton.vue"
import { UpdateStatus } from "@/enum/enumUpdateStatus"

const updateStatus = ref(UpdateStatus)
</script>

<template>
  <div class="flex flex-col gap-y-20 items-start">
    <h1 class="font-bold text-32">Text Button</h1>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[30%]">
        <TextButton button-type="primary" label="Button Primary" />
      </div>
      <div class="min-w-[30%]">
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Primary With Icon"
        />
      </div>
      <div class="min-w-[30%]">
        <TextButton button-type="primary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[30%]">
        <TextButton button-type="secondary" label="Button Secondary" />
      </div>
      <div class="min-w-[30%]">
        <TextButton
          button-type="secondary"
          icon="close"
          label="Button Secondary With Icon"
        />
      </div>
      <div class="min-w-[30%]">
        <TextButton button-type="secondary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[30%]">
        <TextButton button-type="tertiary" label="Button Tertiary" />
      </div>
      <div class="min-w-[30%]">
        <TextButton
          button-type="tertiary"
          icon="close"
          label="Button Tertiary With Icon"
        />
      </div>
      <div class="min-w-[30%]">
        <TextButton button-type="tertiary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-x-5 gap-y-20 items-start w-full">
      <div class="min-w-[30%]">
        <TextButton button-type="ghost" label="Button Ghost" />
      </div>
      <div class="min-w-[30%]">
        <TextButton
          button-type="ghost"
          icon="close"
          label="Button Ghost With Icon"
        />
      </div>
      <div class="min-w-[30%]">
        <TextButton button-type="ghost" icon="close" label="" />
      </div>
    </div>

    <div class="flex flex-col gap-y-20 items-start">
      <h1 class="font-bold text-32">Save Docunit Button</h1>
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
    <div class="flex flex-col gap-y-20 items-start">
      <h1 class="font-bold text-32">File Upload Button</h1>
      <FileInputButton id="file-upload-after-fail" aria-label="Upload File">
        <span class="material-icons">search</span>
        Festplatte durchsuchen
      </FileInputButton>
    </div>
  </div>
</template>
