<script lang="ts" setup>
import { ref } from "vue"
import Pagination from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const items = ref<number[]>()

async function itemService(page: number, size: number) {
  const totalElements = 100
  const start = page * size
  const end = (page + 1) * size

  return {
    status: 200,
    data: {
      content: Array.from({ length: end - start }, (_, i) => start + i),
      size,
      totalElements,
      totalPages: totalElements / size,
      number: page,
      numberOfElements: 100,
      first: page == 1 ? true : false,
      last: totalElements >= end ? true : false,
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
