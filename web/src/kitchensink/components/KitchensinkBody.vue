<script lang="ts" setup>
import { computed } from "vue"
import type { risComponent } from "@/kitchensink/types/types"
import ButtonView from "@/kitchensink/views/ButtonView.vue"
import CodeSnippetView from "@/kitchensink/views/CodeSnippetView.vue"
import ErrorView from "@/kitchensink/views/ErrorView.vue"
import ExpandableContentView from "@/kitchensink/views/ExpandableContentView.vue"
import FileUploadView from "@/kitchensink/views/FileUploadView.vue"
import FileViewerView from "@/kitchensink/views/FileViewerView.vue"
import InfoModalView from "@/kitchensink/views/InfoModalView.vue"
import InputView from "@/kitchensink/views/InputView.vue"
import NavbarView from "@/kitchensink/views/NavbarView.vue"
import OriginalFileSidePanelView from "@/kitchensink/views/OriginalFileSidePanelView.vue"
import PopupModalView from "@/kitchensink/views/PopupModalView.vue"
import SideToggleView from "@/kitchensink/views/SideToggleView.vue"
import TextEditorView from "@/kitchensink/views/TextEditorView.vue"

const props = defineProps<{
  selectedComponent: risComponent
}>()

const component = computed(() => {
  switch (props.selectedComponent.view) {
    case "ButtonView":
      return ButtonView
    case "CodeSnippetView":
      return CodeSnippetView
    case "InfoModalView":
      return InfoModalView
    case "ExpandableContentView":
      return ExpandableContentView
    case "FileUploadView":
      return FileUploadView
    case "FileViewerView":
      return FileViewerView
    case "InputView":
      return InputView
    case "NavbarView":
      return NavbarView
    case "OriginalFileSidePanelView":
      return OriginalFileSidePanelView
    case "PopupModalView":
      return PopupModalView
    case "TextEditorView":
      return TextEditorView
    case "SideToggleView":
      return SideToggleView
    default:
      return ErrorView
  }
})
</script>

<template>
  <div
    class="flex flex-nowrap flex-row items-center justify-center overflow-y-hidden"
  >
    <div
      class="flex flex-col flex-nowrap gap-y-10 justify-start py-10 text-justify w-2/3"
    >
      <div class="content-container flex flex-col">
        <component :is="component" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
* {
  scrollbar-width: none;
}
</style>
