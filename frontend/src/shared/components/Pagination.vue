<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { withDefaults } from "vue"
import { ServiceResponse } from "@/services/httpClient"

const props = withDefaults(
  defineProps<{
    page: Page<any> // eslint-disable-line @typescript-eslint/no-explicit-any
    navigationPosition?: "top" | "bottom"
  }>(),
  { navigationPosition: "top" }
)

const emits = defineEmits<{
  (e: "updatePage", page: number): void // eslint-disable-line @typescript-eslint/no-explicit-any
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

export type PageableService<T> = {
  (page: number, size: number, searchStr?: string): Promise<
    ServiceResponse<Page<T>>
  >
}
</script>

<template>
  <slot v-if="props.navigationPosition == 'bottom'"></slot>
  <div v-if="page?.content" class="flex flex-col items-center my-32">
    <div class="flex items-center">
      <div class="flex flex-grow items-center justify-center relative">
        <button
          aria-label="vorherige Ergebnisse"
          class="disabled:opacity-25 flex items-center link-01-bold pr-20"
          :disabled="page?.first"
          @click="previousPage"
          @keydown.enter="previousPage"
        >
          <span class="material-icons no-">arrow_back</span
          ><span class="underline">zurück</span>
        </button>
        <span v-if="page" class="pr-20">
          {{ page.number + 1 }} von {{ page.totalPages }}
        </span>
        <button
          aria-label="nächste Ergebnisse"
          class="disabled:opacity-25 flex items-center link-01-bold pr-20"
          :disabled="page?.last"
          @click="nextPage"
          @keydown.enter="nextPage"
        >
          <span class="underline">vor</span
          ><span class="material-icons">arrow_forward</span>
        </button>
      </div>
    </div>
    <div class="-ml-144 label-02-reg mt-2 text-[#4E596A]">
      Total {{ page?.totalElements }} Items
    </div>
  </div>
  <slot v-if="props.navigationPosition == 'top'"></slot>
</template>
