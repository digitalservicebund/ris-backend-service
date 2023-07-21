<script lang="ts" setup>
import { onMounted, ref } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const items = ref<number[]>()
const currentPage = ref<Page<number>>()

async function mockedItemService(page: number, size: number) {
  const totalElements = 60
  const start = page * size
  const end = page > totalElements / size ? totalElements : (page + 1) * size

  return {
    status: 200,
    data: {
      content: Array.from({ length: end - start }, (_, i) => start + i),
      size: size,
      totalElements,
      totalPages: totalElements / size,
      number: page,
      numberOfElements: 100,
      first: page === 0,
      last: page + 1 >= totalElements / size,
    },
  }
}

async function updateItems(page: number) {
  const response = await mockedItemService(page, itemsPerPage)
  if (response.data) {
    items.value = response.data.content
    currentPage.value = response.data
  }
}

onMounted(() => updateItems(0))
</script>

<template>
  <KitchensinkPage name="Pagination">
    <div>
      <Pagination
        v-if="currentPage"
        :page="currentPage"
        @update-page="updateItems"
      >
        <ul v-for="item in items" :key="item">
          <span>{{ item }}</span>
        </ul>
      </Pagination>
    </div>
  </KitchensinkPage>
</template>
