<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { ref, computed, withDefaults, onMounted } from "vue"
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

const page = ref<number>()
const totalItems = ref<number>()
const totalPages = ref<number>()

const isFirstPage = computed(() => page.value == 0)
const isLastPage = computed(() =>
  totalPages.value ? page.value == totalPages.value : false
)

function nextPage() {
  if (!isLastPage.value) updateItems(page.value ? page.value + 1 : 1)
}

function previousPage() {
  if (!isFirstPage.value && page.value) updateItems(page.value - 1)
}

async function updateItems(newPage: number) {
  const response = await props.itemService(newPage, props.itemsPerPage, "")
  if (response.data) {
    emits("updateItems", response.data.content)
    page.value = response.data.number
    totalItems.value = response.data.totalElements
    totalPages.value = response.data.totalPages
  }
}

onMounted(() => updateItems(0))
</script>

<template>
  <span v-if="totalItems">Total Items: {{ totalItems }}</span>
  <span v-if="!isFirstPage" @click="previousPage" @keydown.enter="previousPage"
    >Previous</span
  >
  <span v-if="page != undefined">{{ page + 1 }} von {{ totalPages }}</span>
  <span v-if="!isLastPage" @click="nextPage" @keydown.enter="nextPage"
    >Next</span
  >

  <slot></slot>
</template>
