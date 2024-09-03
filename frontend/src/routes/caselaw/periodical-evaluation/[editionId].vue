<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import { useRoute } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import ErrorPage from "@/components/PageError.vue"
import { usePeriodicalEvaluationMenuItems } from "@/composables/usePeriodicalEvaluationMenuItems"
import Reference from "@/domain/reference"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"

const store = useEditionStore()
const responseError = ref<ResponseError>()
const route = useRoute()

const references = computed({
  get: () => (store.edition ? (store.edition.references as Reference[]) : []),
  set: (newValues) => {
    store.edition!.references = newValues
  },
})

const title = computed(
  () =>
    `Periodikaauswertung | ${store.edition?.legalPeriodical?.abbreviation}, ${store.edition?.name ? store.edition.name : store.edition?.prefix}`,
)

watch(references, async () => {
  const response = await store.updateEdition()
  if (response.error) {
    responseError.value = response.error
  }
})

onMounted(async () => {
  const response = await store.loadEdition()
  if (response.error) {
    responseError.value = response.error
  }
})

const menuItems = usePeriodicalEvaluationMenuItems(
  store.edition?.id,
  route.query,
)
</script>

<template>
  <div class="flex w-screen grow">
    <div
      v-if="!route.path.includes('preview') && store.edition"
      class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
    >
      <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
    </div>
    <div v-if="store.edition" class="flex w-full min-w-0 flex-col bg-gray-100">
      <div class="flex grow flex-col items-start">
        <h2 class="ds-label-01-bold p-24 text-black">{{ title }}</h2>
        <router-view />
      </div>
    </div>
    <ErrorPage
      v-if="responseError"
      :error="responseError"
      :title="responseError?.title"
    />
  </div>
</template>
