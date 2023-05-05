<script lang="ts" setup>
import { ref } from "vue"
import Pagination from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const items = ref<number[]>()

async function itemService(page: number) {
  const totalElements = 60
  const start = page * itemsPerPage
  const end =
    page > totalElements / itemsPerPage
      ? totalElements
      : (page + 1) * itemsPerPage

  return {
    status: 200,
    data: {
      content: Array.from({ length: end - start }, (_, i) => start + i),
      size: itemsPerPage,
      totalElements,
      totalPages: totalElements / itemsPerPage,
      number: page,
      numberOfElements: 100,
      first: page == 0 ? true : false,
      last: page + 1 >= totalElements / itemsPerPage ? true : false,
    },
  }
}

async function handleUpdateItems(newItems: number[]) {
  items.value = newItems
}
</script>

<template>
  <div>
    <Pagination
      get-inital-data
      :item-service="itemService"
      :items-per-page="itemsPerPage"
      @update-items="handleUpdateItems"
    >
      <ul v-for="item in items" :key="item">
        <span>{{ item }}</span>
      </ul>
    </Pagination>
  </div>
</template>
