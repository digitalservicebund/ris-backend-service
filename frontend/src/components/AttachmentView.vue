<script setup lang="ts">
import { ref, watch } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import { Docx2HTML } from "@/domain/docx2html"
import fileService from "@/services/fileService"

const props = defineProps<{
  documentUnitUuid?: string
  s3Path?: string
}>()

const fileAsHTML = ref<Docx2HTML>()

const getAttachmentHTML = async () => {
  if (props.documentUnitUuid && props.s3Path) {
    const htmlResponse = await fileService.getAttachmentAsHtml(
      props.documentUnitUuid,
      props.s3Path,
    )

    if (htmlResponse.error === undefined) fileAsHTML.value = htmlResponse.data
  }
}
getAttachmentHTML()

watch(
  () => props.s3Path,
  () => {
    getAttachmentHTML()
  },
)
</script>

<template>
  <FlexContainer
    v-if="fileAsHTML?.html"
    class="sticky top-0 flex w-full flex-col gap-40 bg-white p-24"
    v-bind="$attrs"
  >
    <FlexItem
      class="h-[65vh] overflow-scroll border-1 border-solid border-gray-400"
    >
      <TextEditor
        data-testid="text-editor"
        element-id="text-editor"
        field-size="max"
        :value="fileAsHTML?.html"
      />
    </FlexItem>
  </FlexContainer>
</template>

<style lang="scss" scoped>
.odoc-open {
  display: flex;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  border-radius: 10px;
  margin-right: 40px;
  transform: rotate(-90deg);
  transform-origin: right;
}

.odoc-open-text {
  margin-left: 30px;
}

.odoc-open-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  transform: rotate(90deg) translateY(-25px);
}

.odoc-open-icon {
  margin-top: 8px;
  margin-right: 9px;
}
</style>
