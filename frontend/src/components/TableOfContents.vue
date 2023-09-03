<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Article, DocumentSection, isDocumentSection } from "@/domain/norm"

const props = defineProps<{
  documentSections: (Article | DocumentSection)[]
  marginLeft: number
}>()

const articleMargins: { [key: number]: string } = {
  0: "ml-[0px]",
  20: "ml-[8px]",
  40: "ml-[28px]",
  60: "ml-[48px]",
  80: "ml-[68px]",
  100: "ml-[88px]",
  120: "ml-[108px]",
  140: "ml-[128px]",
  160: "ml-[148px]",
}
const isLastItem = (index: number) =>
  index === props.documentSections.length - 1
</script>

<template>
  <div
    v-for="(doc, index) in props.documentSections"
    :key="doc.guid"
    class="border-t border-gray-400"
    :class="{ 'border-b border-gray-400': isLastItem(index) }"
    data-testid="document-sections"
  >
    <ExpandableContent
      v-if="isDocumentSection(doc)"
      class="bg-blue-200 pt-4"
      close-icon-name="expand_more"
      data-testid="document-sections"
      icons-on-left
      is-expanded
      :margin-left="marginLeft"
      open-icon-name="chevron_right"
    >
      <template #header>
        <h2 class="ds-label-02-bold ml-4 w-full text-left">
          {{ doc.marker }} {{ doc.heading }}
        </h2>
      </template>
      <TableOfContents
        v-if="doc.documentation"
        :document-sections="doc.documentation"
        :margin-left="marginLeft + 20"
      />
    </ExpandableContent>
    <div v-else class="bg-gray-100 pb-4 pt-4">
      <p
        class="ds-label-02-reg pl-20 pr-20"
        :class="`${articleMargins[marginLeft]}`"
      >
        {{ doc.marker }} {{ doc.heading }}
      </p>
    </div>
  </div>
</template>
