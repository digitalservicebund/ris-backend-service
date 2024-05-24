<script setup lang="ts">
import { RouterLink } from "vue-router"
import FlexContainer from "@/components/FlexContainer.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"

interface Props {
  decision: RelatedDocumentation
}

const props = defineProps<Props>()
</script>

<template>
  <FlexContainer
    :data-testid="'document-number-link-' + props.decision.documentNumber"
  >
    <span class="ds-label-01-reg ml-8 mr-8">|</span>
    <div v-if="decision.hasForeignSource">
      <RouterLink
        v-if="props.decision.documentNumber"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: { documentNumber: props.decision.documentNumber },
        }"
      >
        <button>
          <FlexContainer flex-direction="flex-row">
            <FlexContainer
              class="ds-link-03-bold flex flex-row border-b-2 border-blue-800 leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
              flex-direction="flex-row"
            >
              <p>{{ props.decision.documentNumber }}</p>
              <BaselineArrowOutward />
            </FlexContainer>
          </FlexContainer>
        </button>
      </RouterLink>
    </div>

    <div v-else>
      <p>
        {{ props.decision.documentNumber }}
      </p>
    </div>
  </FlexContainer>
</template>
