<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import TextEditor from "@/shared/components/input/TextEditor.vue"
</script>

<template>
  <KitchensinkPage name="Text editor">
    <p class="ds-body-01-reg">
      Resize the window to see different views of text editor.
    </p>

    <KitchensinkStory name="Maximum size">
      <TextEditor
        aria-label="text editor"
        class="overflow-y-auto outline outline-2 outline-blue-900"
        editable
        field-size="max"
        value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
      />
    </KitchensinkStory>

    <KitchensinkStory name="Large size">
      <TextEditor
        aria-label="text editor"
        class="overflow-y-auto outline outline-2 outline-blue-900"
        editable
        field-size="large"
        value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
      />
    </KitchensinkStory>

    <KitchensinkStory name="Medium size">
      <TextEditor
        aria-label="text editor"
        class="overflow-y-auto outline outline-2 outline-blue-900"
        editable
        field-size="medium"
        value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
      />
    </KitchensinkStory>

    <KitchensinkStory name="Small size">
      <TextEditor
        aria-label="text editor"
        class="overflow-y-auto outline outline-2 outline-blue-900"
        editable
        field-size="small"
        value="This texteditor is editable: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
      />
    </KitchensinkStory>

    <KitchensinkStory name="Disabled">
      <TextEditor
        aria-label="text editor"
        class="outline outline-2 outline-blue-900"
        field-size="medium"
        value="This texteditor is disabled: Lorem ipsum dolor sit, amet consectetur adipisicing elit. Minus accusamus temporibus omnis, odio harum necessitatibus voluptate sunt sapiente officia quibusdam totam inventore quaerat consectetur, facere iure fugit dolorem quia pariatur."
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>

<style scoped>
* {
  scrollbar-width: none;
}
</style>
