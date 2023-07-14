<script lang="ts" setup>
import TextEditor from "@/shared/components/input/TextEditor.vue"
</script>

<template>
  <div class="flex flex-col gap-y-20 bg-white">
    <span class="text-30"
      >Resize the windown to see different views of text editor</span
    >
    <span class="text-30">Size="max"</span>
    <span>
      <div>
        <TextEditor
          aria-label="text editor"
          class="overflow-y-auto outline outline-2 outline-blue-900"
          editable
          field-size="max"
          value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
        />
      </div>
    </span>
    <span class="text-30">Size="large"</span>
    <span>
      <div>
        <TextEditor
          aria-label="text editor"
          class="overflow-y-auto outline outline-2 outline-blue-900"
          editable
          field-size="large"
          value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
        />
      </div>
    </span>
    <span class="text-30">Size="medium"</span>
    <span>
      <div>
        <TextEditor
          aria-label="text editor"
          class="overflow-y-auto outline outline-2 outline-blue-900"
          editable
          field-size="medium"
          value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
        />
      </div>
    </span>
    <span class="text-30">Size="small"</span>
    <span>
      <div>
        <TextEditor
          aria-label="text editor"
          class="overflow-y-auto outline outline-2 outline-blue-900"
          editable
          field-size="small"
          value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
        />
      </div>
    </span>
    <span class="text-30">This text editor is disable</span>
    <span>
      <div>
        <TextEditor
          aria-label="text editor"
          class="outline outline-2 outline-blue-900"
          field-size="medium"
          value="This texteditor is disable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
        />
      </div>
    </span>
  </div>
</template>

<style scoped>
* {
  scrollbar-width: none;
}
</style>
