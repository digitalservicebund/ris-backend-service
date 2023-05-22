<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { withDefaults } from "vue"
import PaginationButton from "./PaginationButton.vue"
import { ServiceResponse } from "@/services/httpClient"

const props = withDefaults(
  defineProps<{
    page: Page<any> // eslint-disable-line @typescript-eslint/no-explicit-any
    navigationPosition?: "top" | "bottom"
  }>(),
  { navigationPosition: "top" }
)

const emits = defineEmits<{
  (e: "updatePage", page: number): void
}>()

async function nextPage() {
  !props.page.last && emits("updatePage", props.page.number + 1)
}

async function previousPage() {
  !props.page.first && emits("updatePage", props.page.number - 1)
}
</script>

<script lang="ts">
export type Page<T> = {
  content: T[]
  size: number
  totalElements: number
  totalPages: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
}

export type PageableService<TResult, TQuery = TResult> = {
  (page: number, size: number, query?: TQuery): Promise<
    ServiceResponse<Page<TResult>>
  >
}
</script>

<template>
  <slot v-if="props.navigationPosition == 'bottom'"></slot>
  <div v-if="page?.content" class="flex flex-col items-center my-32">
    <div class="flex items-center">
      <div class="flex flex-grow items-center justify-center relative">
        <PaginationButton
          aria-label="vorherige Ergebnisse"
          :disabled="page?.first"
          @click="previousPage"
          @keydown.enter="previousPage"
        >
          <span class="material-icons no-">arrow_back</span
          ><span class="underline">zurück</span>
        </PaginationButton>
        <span v-if="page" class="pr-20">
          {{ page.number + 1 }} von {{ page.totalPages }}
        </span>
        <PaginationButton
          aria-label="nächste Ergebnisse"
          :disabled="page?.last"
          @click="nextPage"
          @keydown.enter="nextPage"
        >
          <span class="underline">vor</span
          ><span class="material-icons">arrow_forward</span>
        </PaginationButton>
      </div>
    </div>
    <div class="-ml-144 label-02-reg mt-2 text-[#4E596A]">
      Total {{ page?.totalElements }} Items
    </div>
  </div>
  <slot v-if="props.navigationPosition == 'top'"></slot>
</template>
