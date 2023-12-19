<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import PaginationButton from "./PaginationButton.vue"
import { ServiceResponse } from "@/services/httpClient"
import IconArrowBack from "~icons/ic/baseline-arrow-back"
import IconArrowForward from "~icons/ic/baseline-arrow-forward"

const props = withDefaults(
  defineProps<{
    page?: Page<any> // eslint-disable-line @typescript-eslint/no-explicit-any
    navigationPosition?: "top" | "bottom"
    isLoading?: boolean
  }>(),
  { page: undefined, navigationPosition: "top", isLoading: false },
)

const emits = defineEmits<(e: "updatePage", page: number) => void>()

async function nextPage() {
  props.page && !props.page.last && emits("updatePage", props.page.number + 1)
}

async function previousPage() {
  props.page && !props.page?.first && emits("updatePage", props.page.number - 1)
}
</script>

<script lang="ts">
export type Page<T> = {
  content: T[]
  size: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export type PageableService<TResult, TQuery = TResult> = (
  page: number,
  size: number,
  query?: TQuery,
) => Promise<ServiceResponse<Page<TResult>>>
</script>

<template>
  <slot v-if="props.navigationPosition == 'bottom'"></slot>
  <div
    v-if="page?.content && !isLoading"
    class="my-32 flex flex-col items-center"
  >
    <div class="flex items-center">
      <div class="relative flex flex-grow items-center justify-center">
        <PaginationButton
          aria-label="vorherige Ergebnisse"
          :disabled="page?.first"
          @click="previousPage"
          @keydown.enter="previousPage"
        >
          <IconArrowBack />
          <span class="underline">zurück</span>
        </PaginationButton>
        <span v-if="!page?.empty">
          {{ page.numberOfElements }} Ergebniss(e) auf Seite
          {{ page.number + 1 }}
        </span>
        <span v-else> Keine Ergebnisse </span>
        <PaginationButton
          aria-label="nächste Ergebnisse"
          :disabled="page?.last"
          @click="nextPage"
          @keydown.enter="nextPage"
        >
          <span class="underline">vor</span>
          <IconArrowForward />
        </PaginationButton>
      </div>
    </div>
  </div>
  <slot v-if="props.navigationPosition == 'top'"></slot>
</template>
