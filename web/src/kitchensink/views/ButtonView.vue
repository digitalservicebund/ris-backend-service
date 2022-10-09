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
    <h2 class="heading-02-regular">Text Button</h2>
    <div class="flex flex-row flex-wrap gap-8 items-start w-full">
      <div>
        <TextButton button-type="primary" label="Button Primary" />
      </div>
      <div>
        <TextButton
          button-type="primary"
          icon="close"
          label="Button Primary With Icon"
        />
      </div>
      <div>
        <TextButton button-type="primary" icon="close" label="" />
      </div>
    </div>

    <div class="flex flex-row flex-wrap gap-8 items-start w-full">
      <div>
        <TextButton button-type="secondary" label="Button Secondary" />
      </div>
      <div>
        <TextButton
          button-type="secondary"
          icon="close"
          label="Button Secondary With Icon"
        />
      </div>
      <div>
        <TextButton button-type="secondary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-8 items-start w-full">
      <div>
        <TextButton button-type="tertiary" label="Button Tertiary" />
      </div>
      <div>
        <TextButton
          button-type="tertiary"
          icon="close"
          label="Button Tertiary With Icon"
        />
      </div>
      <div>
        <TextButton button-type="tertiary" icon="close" label="" />
      </div>
    </div>
    <div class="flex flex-row flex-wrap gap-8 items-start w-full">
      <div>
        <TextButton button-type="ghost" label="Button Ghost" />
      </div>
      <div>
        <TextButton
          button-type="ghost"
          icon="close"
          label="Button Ghost With Icon"
        />
      </div>
      <div>
        <TextButton button-type="ghost" icon="close" label="" />
      </div>
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
    <div class="flex flex-col gap-y-20 items-start">
      <h2 class="heading-02-regular">File Upload Button</h2>
      <FileInputButton id="file-upload-after-fail" aria-label="Upload File">
        <span class="material-icons">search</span>
        Festplatte durchsuchen
      </FileInputButton>
    </div>
  </div>
</template>
