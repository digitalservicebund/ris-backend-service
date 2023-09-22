<script lang="ts" setup>
import { computed } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import ArticleLink from "@/components/tableOfContents/ArticleLink.vue"
import {
  Article,
  DocumentSection,
  Recitals,
  isDocumentSection,
} from "@/domain/norm"

const props = withDefaults(
  defineProps<{
    documentSections: (Article | DocumentSection)[]
    normGuid?: string
    marginLevel?: number
    hasFormula?: boolean
    recitals?: Recitals
    hasConclusion?: boolean
  }>(),
  {
    normGuid: undefined,
    marginLevel: 0,
    recitals: undefined,
  },
)

const effectiveRecitals = computed(() => {
  if (!props.recitals) {
    return undefined
  } else if (!props.recitals.marker && !props.recitals.heading) {
    return "Präambel"
  } else {
    return `${props.recitals.marker ?? ""} ${
      props.recitals.heading ?? ""
    }`.trim()
  }
})
</script>

<template>
  <ArticleLink
    v-if="hasFormula"
    class="border-t border-gray-400"
    :class="{ 'last:border-b': marginLevel === 0 }"
    title="Eingangsformel"
    :to="{
      name: 'norms-norm-normGuid-documentation-formula',
      params: { normGuid: props.normGuid },
    }"
  />

  <ArticleLink
    v-if="effectiveRecitals"
    class="border-t border-gray-400"
    :class="{ 'last:border-b': marginLevel === 0 }"
    :title="effectiveRecitals"
    :to="{
      name: 'norms-norm-normGuid-documentation-recitals',
      params: { normGuid: props.normGuid },
    }"
  />

  <!-- Sections & articles -->
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

    <ArticleLink
      v-else
      :margin-level="marginLevel"
      :title="`${doc.marker ?? ''} ${doc.heading ?? ''}`"
      :to="{
        name: 'norms-norm-normGuid-documentation-documentationGuid',
        params: { normGuid: props.normGuid, documentationGuid: doc.guid },
      }"
    />
  </div>

  <ArticleLink
    v-if="hasConclusion"
    class="border-t border-gray-400"
    :class="{ 'last:border-b': marginLevel === 0 }"
    title="Schlussformel"
    :to="{
      name: 'norms-norm-normGuid-documentation-conclusion',
      params: { normGuid: props.normGuid },
    }"
  />
</template>
