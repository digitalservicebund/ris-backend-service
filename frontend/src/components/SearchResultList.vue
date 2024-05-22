<script setup lang="ts">
import InlineDecision from "./InlineDecision.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextButton from "@/components/input/TextButton.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import IconAdd from "~icons/ic/baseline-add"

defineProps<{
  searchResults?: SearchResults<RelatedDocumentation>
  isLoading: boolean
}>()

const emits =
  defineEmits<(event: "linkDecision", decision: RelatedDocumentation) => void>()
</script>

<script lang="ts">
export type SearchResults<Type extends RelatedDocumentation> = {
  decision: Type
  isLinked: boolean
}[]
</script>

<template>
  <div class="bg-blue-200">
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
        <p>Keine Ergebnisse gefunden</p>
      </FlexContainer>
    </div>

    <FlexContainer
      v-else-if="searchResults"
      class="p-24"
      flex-direction="flex-col"
    >
      <p class="font-bold">Passende Suchergebnisse:</p>
      <FlexContainer
        v-for="searchResult in searchResults"
        :key="searchResult.decision.uuid"
        class="mt-16"
      >
        <FlexContainer align-items="items-center">
          <TextButton
            aria-label="Treffer übernehmen"
            class="mr-16"
            :disabled="searchResult.isLinked"
            :icon="IconAdd"
            @click.stop="emits('linkDecision', searchResult.decision)"
          />
          <InlineDecision :decision="searchResult.decision" />

          <span
            v-if="searchResult.isLinked"
            class="ds-label-02-reg ml-8 rounded-full bg-yellow-400 px-8 py-2"
            >Bereits hinzugefügt</span
          >
        </FlexContainer>
      </FlexContainer>
    </FlexContainer>
  </div>
</template>
