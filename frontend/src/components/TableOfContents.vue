<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Article, DocumentSection, isDocumentSection } from "@/domain/norm"

const props = defineProps<{
  documentSections: (Article | DocumentSection)[]
  marginLeft?: number
}>()

const articleMargins: { [key: number]: string } = {
  0: "ml-[0px]",
  20: "ml-[24px]",
  40: "ml-[44px]",
  60: "ml-[64px]",
  80: "ml-[74px]",
  100: "ml-[94px]",
  120: "ml-[114px]",
  140: "ml-[134px]",
  160: "ml-[154px]",
}
</script>

<template>
  <div
    v-for="doc in props.documentSections"
    :key="doc.guid"
    class="border-t border-gray-400"
    data-testid="document-sections"
  >
    <ExpandableContent
      v-if="isDocumentSection(doc)"
      class="bg-blue-200 pt-4"
      close-icon-name="expand_more"
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
        :margin-left="marginLeft ? marginLeft + 20 : 20"
      />
    </ExpandableContent>
    <div v-else class="bg-gray-100 pb-4 pt-4">
      <p
        class="ds-label-02-reg pl-4 pr-4"
        :class="`${articleMargins[marginLeft ? marginLeft : 0]}`"
      >
        {{ doc.marker }} {{ doc.heading }}
      </p>
    </div>
  </div>
</template>
