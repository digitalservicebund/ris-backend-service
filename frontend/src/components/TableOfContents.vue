<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Article, DocumentSection, isDocumentSection } from "@/domain/norm"

const props = defineProps<{
  documentSections: (Article | DocumentSection)[]
  normGuid?: string
  marginLevel?: number
}>()

const articleMargins: { [key: number]: string } = {
  0: "ml-[0px]",
  1: "ml-[24px]",
  2: "ml-[46px]",
  3: "ml-[68px]",
  4: "ml-[90px]",
  5: "ml-[112px]",
  6: "ml-[134px]",
  7: "ml-[156px]",
  8: "ml-[178px]",
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
      :margin-level="marginLevel"
      open-icon-name="chevron_right"
      prevent-expand-on-click
    >
      <template #header>
        <router-link
          class="ds-label-02-bold ml-4 w-full pb-4 text-left"
          :to="{
            name: 'norms-norm-normGuid-documentation-documentationGuid',
            params: {
              normGuid: props.normGuid,
              documentationGuid: doc.guid,
            },
          }"
        >
          <h2>{{ doc.marker }} {{ doc.heading }}</h2>
        </router-link>
      </template>
      <TableOfContents
        v-if="doc.documentation"
        :document-sections="doc.documentation"
        :margin-level="marginLevel ? marginLevel + 1 : 1"
        :norm-guid="normGuid"
      />
    </ExpandableContent>
    <div v-else class="bg-gray-100">
      <router-link
        class="ds-label-02-reg"
        :class="`${articleMargins[marginLevel ? marginLevel : 0]}`"
        :to="{
          name: 'norms-norm-normGuid-documentation-documentationGuid',
          params: {
            normGuid: props.normGuid,
            documentationGuid: doc.guid,
          },
        }"
      >
        {{ doc.marker }} {{ doc.heading }}
      </router-link>
    </div>
  </div>
</template>
