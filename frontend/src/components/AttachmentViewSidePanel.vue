<script setup lang="ts">
import AttachmentView from "@/components/AttachmentView.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FlexItem from "@/components/FlexItem.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import Attachment from "@/domain/attachment"

interface Props {
  isExpanded: boolean
  attachments: Attachment[]
  currentIndex?: number
  label?: string
  documentUnitUuid: string
  openingDirection?: OpeningDirection
}

const props = withDefaults(defineProps<Props>(), {
  openingDirection: OpeningDirection.RIGHT,
  label: "attachment view side panel",
  currentIndex: 0,
})

const emit = defineEmits<{
  (e: "update", isExpanded: boolean): void
  (e: "select", index: number): void
}>()

const handleOnSelect = (index: number) => {
  emit("select", index)
}

const handlePanelExpanded = (isExpanded: boolean) => {
  emit("update", isExpanded)
}

const getAttachment = (index: number): Attachment | undefined => {
  return props.attachments[index]
}
</script>

<template>
  <FlexItem
    class="h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
  >
    <SideToggle
      class="sticky top-[8rem] z-20 w-full"
      :is-expanded="props.isExpanded"
      label="AttachmentViewSideToggle"
      :opening-direction="OpeningDirection.LEFT"
      @update:is-expanded="handlePanelExpanded"
    >
      <FileNavigator
        :current-index="props.currentIndex"
        :files="props.attachments"
        @select="handleOnSelect"
      ></FileNavigator>
      <AttachmentView
        :document-unit-uuid="props.documentUnitUuid"
        :s3-path="getAttachment(currentIndex)?.s3path"
      />
    </SideToggle>
  </FlexItem>
</template>
