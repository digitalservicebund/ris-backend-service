<script lang="ts" setup>
import FlexContainer from "@/components/FlexContainer.vue"
import Reference from "@/domain/reference"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"

const props = defineProps<{
  data: Reference
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-col">
      <RouterLink
        v-if="props.data.documentationUnit?.documentNumber"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: {
            documentNumber: props.data.documentationUnit?.documentNumber,
          },
        }"
      >
        <button
          class="ds-link-02-bold flex flex-row leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
        >
          <FlexContainer flex-direction="flex-row">
            <p>{{ props.data.renderDocumentationUnit }}</p>
            <BaselineArrowOutward />
          </FlexContainer>
        </button>
      </RouterLink>

      <div class="ds-label-01-reg mr-8">
        {{ props.data.renderReference }}
      </div>
    </div>
  </div>
</template>
