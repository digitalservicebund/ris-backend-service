<script setup lang="ts">
import AttachmentView from "@/components/AttachmentView.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FlexItem from "@/components/FlexItem.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import Attachment from "@/domain/attachment"

interface Props {
  isExpanded: boolean
  attachments: Attachment[]
  currentIndex: number
  documentUnitUuid: string
  label?: string
  openingDirection?: OpeningDirection
}

const props = withDefaults(defineProps<Props>(), {
  label: "attachment view side panel",
  openingDirection: OpeningDirection.RIGHT,
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
</script>

<template>
  <FlexItem
    v-if="attachments.length > 0"
    class="h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
    :class="[props.isExpanded ? 'flex-1' : '', props.isExpanded ? 'w-1/2' : '']"
    data-testid="attachment-view-side-panel"
  >
    <SideToggle
      class="sticky top-[8rem] z-20"
      :is-expanded="props.isExpanded"
      label="Dokumentansicht"
      :opening-direction="OpeningDirection.LEFT"
      size="medium"
      @update:is-expanded="handlePanelExpanded"
    >
      <FileNavigator
        :current-index="props.currentIndex"
        :files="props.attachments"
        @select="handleOnSelect"
      ></FileNavigator>
      <AttachmentView
        v-if="
          props.documentUnitUuid &&
          attachments &&
          props.currentIndex != null &&
          attachments[props.currentIndex] &&
          attachments[props.currentIndex]?.s3path
        "
        :document-unit-uuid="props.documentUnitUuid"
        :s3-path="attachments[props.currentIndex].s3path"
      />
    </SideToggle>
  </FlexItem>
</template>
