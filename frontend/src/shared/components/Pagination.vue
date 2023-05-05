<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { ref, withDefaults, onMounted } from "vue"
import { ServiceResponse } from "@/services/httpClient"

const props = withDefaults(
  defineProps<{
    itemsPerPage: number
    itemService: PageableService
    getInitalData?: boolean
  }>(),
  { getInitalData: false }
)

const emits = defineEmits<{
  (e: "updateItems", items: any[]): void // eslint-disable-line @typescript-eslint/no-explicit-any
}>()

type Page = {
  content: any[] // eslint-disable-line @typescript-eslint/no-explicit-any
  size: number
  totalElements: number
  totalPages: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
}

type PageableService = {
  (page: number, size: number, searchStr?: string): Promise<
    ServiceResponse<Page>
  >
}

const page = ref<Page>()

async function nextPage() {
  page.value && !page.value.last && (await updateItems(page.value.number + 1))
}

async function previousPage() {
  page.value && !page.value.first && (await updateItems(page.value.number - 1))
}

async function updateItems(newPage: number) {
  const response = await props.itemService(newPage, props.itemsPerPage, "")
  if (response.data) {
    emits("updateItems", response.data.content)
    page.value = response.data
  }
}

onMounted(() => props.getInitalData && updateItems(0))
</script>

<template>
  <div class="flex flex-col items-center">
    <div class="flex items-center">
      <div class="flex flex-grow items-center justify-center relative">
        <button
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
  <slot></slot>
</template>
