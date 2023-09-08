<script lang="ts" setup>
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Article, DocumentSection, isDocumentSection } from "@/domain/norm"

const props = withDefaults(
  defineProps<{
    documentSections: (Article | DocumentSection)[]
    normGuid?: string
    marginLevel?: number
  }>(),
  {
    normGuid: undefined,
    marginLevel: 0,
  },
)
</script>

<template>
  <div
    v-for="doc in props.documentSections"
    :key="doc.guid"
    class="border-t border-gray-400"
    :class="{ 'last:border-b': marginLevel === 0 }"
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
          class="ds-label-02-bold ml-4 w-full pb-4 text-left hover:underline active:underline"
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
    <div
      v-else
      class="bg-gray-100 hover:bg-blue-300 hover:underline active:bg-blue-500 active:underline"
    >
      <router-link
        class="ds-label-02-reg block py-4"
        :style="{ paddingLeft: `${marginLevel * 24 + 4}px` }"
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
