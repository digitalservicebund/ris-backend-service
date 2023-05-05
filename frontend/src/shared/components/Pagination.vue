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

interface Page {
  content: any[] // eslint-disable-line @typescript-eslint/no-explicit-any
  size: number
  totalElements: number
  totalPages: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
}

interface PageableService {
  (page: number, size: number, searchStr?: string): Promise<
    ServiceResponse<Page>
  >
}

const currentPage = ref<number>()
const totalItems = ref<number>()
const totalPages = ref<number>()

const isFirstPage = ref<boolean>()
const isLastPage = ref<boolean>()

async function nextPage() {
  if (!isLastPage.value)
    await updateItems(currentPage.value ? currentPage.value + 1 : 1)
}

async function previousPage() {
  if (!isFirstPage.value && currentPage.value)
    await updateItems(currentPage.value - 1)
}

async function updateItems(newPage: number) {
  const response = await props.itemService(newPage, props.itemsPerPage, "")
  if (response.data) {
    emits("updateItems", response.data.content)

    currentPage.value = response.data.number
    totalItems.value = response.data.totalElements
    totalPages.value = response.data.totalPages
    isFirstPage.value = response.data.first
    isLastPage.value = response.data.last
  }
  // else
}

onMounted(() => props.getInitalData && updateItems(0))
</script>

<template>
  <div class="flex flex-col items-center">
    <div class="flex items-center">
      <div class="flex flex-grow items-center justify-center relative">
        <button
          class="disabled:opacity-25 flex items-center link-01-bold pr-20"
          :disabled="isFirstPage"
          @click="previousPage"
          @keydown.enter="previousPage"
        >
          <span class="material-icons no-">arrow_back</span
          ><span class="underline">zurück</span>
        </button>
        <span v-if="currentPage != undefined" class="pr-20">
          {{ currentPage + 1 }} von {{ totalPages }}
        </span>
        <button
          class="disabled:opacity-25 flex items-center link-01-bold pr-20"
          :disabled="isLastPage"
          @click="nextPage"
          @keydown.enter="nextPage"
        >
          <span class="underline">vor</span
          ><span class="material-icons">arrow_forward</span>
        </button>
      </div>
    </div>
    <div v-if="totalItems" class="-ml-144 label-02-reg mt-2 text-[#4E596A]">
      Total {{ totalItems }} Items
    </div>
  </div>
  <slot></slot>
</template>
