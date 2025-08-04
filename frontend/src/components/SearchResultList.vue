<script setup lang="ts">
import Button from "primevue/button"
import { watch } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import FlexContainer from "@/components/FlexContainer.vue"
import IconBadge from "@/components/IconBadge.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { useScroll } from "@/composables/useScroll"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import errorMessages from "@/i18n/errors.json"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  searchResults?: SearchResults<RelatedDocumentation>
  isLoading: boolean
  allowMultipleLinks?: boolean
  displayMode?: DisplayMode
}>()
const emits =
  defineEmits<(event: "linkDecision", decision: RelatedDocumentation) => void>()
const { scrollIntoViewportById } = useScroll()
watch(
  () => props.searchResults,
  async () => {
    if (props.searchResults) await scrollIntoViewportById("search-results")
  },
  { immediate: true },
)
</script>

<script lang="ts">
export type SearchResults<Type extends RelatedDocumentation> = {
  decision: Type
  isLinked: boolean
}[]
</script>

<template>
  <div id="search-results">
    <FlexContainer
      v-if="isLoading"
      class="m-24"
      justify-content="justify-center"
    >
      <LoadingSpinner />
    </FlexContainer>

    <div
      v-else-if="searchResults?.length === 0"
      class="grid justify-items-center"
    >
      <FlexContainer class="m-24" justify-content="justify-center">
        <p>{{ errorMessages.SEARCH_RESULTS_NOT_FOUND.title }}</p>
      </FlexContainer>
    </div>

    <FlexContainer
      v-else-if="searchResults"
      class="p-24"
      data-testid="search-results"
      flex-direction="flex-col"
    >
      <p class="ris-label1-bold">Passende Suchergebnisse:</p>
      <FlexContainer
        v-for="searchResult in searchResults"
        :key="searchResult.decision.uuid"
        class="mt-16"
      >
        <FlexContainer align-items="items-center">
          <Button
            aria-label="Treffer übernehmen"
            class="mr-16"
            data-testid="add-decision-button"
            :disabled="!allowMultipleLinks && searchResult.isLinked"
            size="small"
            @click.stop="emits('linkDecision', searchResult.decision)"
            ><template #icon> <IconAdd /> </template
          ></Button>
          <span class="flex w-full flex-row flex-wrap items-center">
            <DecisionSummary
              :display-mode="displayMode"
              :document-number="searchResult.decision.documentNumber"
              :status="searchResult.decision.status"
              :summary="searchResult.decision.renderSummary"
            ></DecisionSummary>
            <IconBadge
              v-if="searchResult.isLinked"
              background-color="bg-yellow-300"
              label="Bereits hinzugefügt"
              text-color="text-yellow-900"
            />
          </span>
        </FlexContainer>
      </FlexContainer>
    </FlexContainer>
  </div>
</template>
