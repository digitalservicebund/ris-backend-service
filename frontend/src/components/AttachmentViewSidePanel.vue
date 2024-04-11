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

const content =
  "\n" +
  "In the bustling office of TechCorp Inc., the engineers were buzzing with excitement over the latest project: the development of a cutting-edge document viewing panel component. The component promised to revolutionize the way users interacted with documents, offering a seamless and intuitive experience.\n" +
  "\n" +
  "Emma, a talented front-end developer, was tasked with leading the design and implementation of the Doc View Panel. With her passion for user experience and a knack for innovative design, Emma dove headfirst into the project.\n" +
  "\n" +
  "She started by sketching out various layouts and brainstorming features that would enhance the user's interaction with documents. After several iterations and feedback sessions with her team, Emma finalized the design for the Doc View Panel.\n" +
  "\n" +
  "The component boasted a sleek interface with intuitive navigation controls, allowing users to zoom in, zoom out, and scroll through documents with ease. It also featured advanced search capabilities, enabling users to quickly find specific sections or keywords within a document.\n" +
  "\n" +
  "As Emma began coding the component, she encountered a few challenges along the way. She had to carefully optimize the performance to ensure smooth scrolling and rendering of large documents. Additionally, she implemented robust error handling and responsive design to ensure the component would work seamlessly across various devices and screen sizes.\n" +
  "\n" +
  "Despite the challenges, Emma's dedication and expertise paid off. The Doc View Panel was successfully integrated into TechCorp's flagship product, receiving rave reviews from users and stakeholders alike.\n" +
  "\n" +
  "The new component not only improved the user experience but also boosted productivity among TechCorp's clients. It quickly became a key selling point for the company's software solutions, setting a new standard for document viewing capabilities in the industry.\n" +
  "\n" +
  "Emma's innovative approach and relentless pursuit of excellence had not only delivered a game-changing component but also inspired her team to push the boundaries of what was possible in front-end development.\n" +
  "\n" +
  "As the sun set on another productive day at TechCorp Inc., Emma couldn't help but feel a sense of pride and accomplishment. She had transformed a simple idea into a powerful tool that would make a lasting impact on the way people interacted with documents.\n" +
  "\n" +
  "And so, the story of the Doc View Panel component served as a reminder that with creativity, perseverance, and a little bit of innovation, anything was possible in the world of technology."
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
        v-if="props.attachments"
        id="odoc-panel-element"
        v-model:open="handlePanelExpanded"
        class="bg-white"
        :content="content"
      />
    </SideToggle>
  </FlexItem>
</template>
