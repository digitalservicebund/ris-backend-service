<script lang="ts" setup>
import dayjs from "dayjs"
import { ref, onMounted, watch } from "vue"
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconExpandLess from "~icons/ic/baseline-expand-less"
import IconExpandMore from "~icons/ic/baseline-expand-more"
import IconFolderOpen from "~icons/material-symbols/folder-open"

const itemsPerPage = 10
const procedures = ref<Procedure[]>()
const currentPage = ref<Page<Procedure>>()
const currentlyExpanded = ref<number[]>([])
const { getQueryFromRoute, pushQueryToRoute, route } = useQuery<"q">()
const query = ref(getQueryFromRoute())
const responseError = ref()

/**
 * Loads all proceudres
 * @param {number} page - page to be updated
 * @param {Query<string>} queries - parameters from route to filter search results
 */
async function updateProcedures(page: number, queries?: Query<string>) {
  const response = await service.get(itemsPerPage, page, queries?.q)
  if (response.data) {
    procedures.value = response.data.content
    currentPage.value = response.data
  }
}

/**
 * Loads documentunits and adds to local value
 */
async function loadDocumentUnits(loadingProcedure: Procedure) {
  if (responseError.value) {
    responseError.value = undefined
  }
  if (!procedures.value) return
  if (loadingProcedure.documentUnitCount == 0) return
  if (loadingProcedure.documentUnits) return

  const response = await service.getDocumentUnits(loadingProcedure.id)

  if (response.error) {
    responseError.value = response.error
    return
  }

  procedures.value = procedures.value.map((procedure) =>
    procedure.label == loadingProcedure.label
      ? {
          ...procedure,
          documentUnits: response.data,
        }
      : procedure,
  )
}

/**
 * Lazily loads the documentunits, only when the procedure list is expanded.
 * When the isExpanded state changes, the procdedure is added/ removed from
 * the currentlyExpanded procedures list, because the need dynamic css classes accordingly
 */
async function handleIsExpanded(
  isExpanded: boolean,
  procedure: Procedure,
  index: number,
) {
  if (isExpanded) {
    currentlyExpanded.value.push(index)
    await loadDocumentUnits(procedure)
  } else {
    currentlyExpanded.value = currentlyExpanded.value.filter(
      (item) => item !== index,
    )
  }
}

/**
 * Sets a timeout before pushing the search query to the route,
 * in order to only change the url params when the user input pauses.
 */
const debouncedPushQueryToRoute = (() => {
  let timeoutId: number | null = null

  return (currentQuerry: Query<string>) => {
    if (timeoutId != null) window.clearTimeout(timeoutId)

    timeoutId = window.setTimeout(() => pushQueryToRoute(currentQuerry), 500)
  }
})()

/**
 * Get query from url and set local query value
 */
watch(route, () => {
  const currentQuery = getQueryFromRoute()
  if (JSON.stringify(query.value) != JSON.stringify(currentQuery))
    query.value = currentQuery
})

/**
 * Update procedures with local query value und update after timeout
 */
watch(
  query,
  async () => {
    await updateProcedures(0, query.value)
    debouncedPushQueryToRoute(query.value)
  },
  { deep: true },
)

/**
 * Switching pages closes all expanded items
 */
watch(currentPage, (newPage) => {
  if (newPage?.number !== currentPage.value) {
    currentlyExpanded.value = []
  }
})

onMounted(() => {
  updateProcedures(0, query.value)
})
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h1 class="ds-heading-02-reg">Vorgänge</h1>
    <div class="mt-24 w-480" role="search">
      <InputField id="procedureFilter" label="Vorgang" visually-hide-label>
        <TextInput
          id="procedureFilter"
          v-model="query.q"
          aria-label="Nach Vorgängen suchen"
          class="ds-input-medium"
          placeholder="Nach Vorgängen suchen"
        ></TextInput>
      </InputField>
    </div>

    <div v-if="procedures" class="flex-1">
      <Pagination
        v-if="currentPage"
        navigation-position="bottom"
        :page="currentPage"
        @update-page="(page) => updateProcedures(page, query)"
      >
        <ExpandableContent
          v-for="(procedure, index) in procedures"
          :key="index"
          class="border-b-1 border-blue-300 bg-white px-24 py-20"
          :class="{
            'my-24': currentlyExpanded.includes(index),
            'hover:bg-blue-100': !currentlyExpanded.includes(index),
          }"
          :is-expanded="currentlyExpanded.includes(index)"
          @update:is-expanded="
            (isExpanded) => handleIsExpanded(isExpanded, procedure, index)
          "
        >
          <template #open-icon>
            <IconExpandMore class="text-blue-800" />
          </template>

          <template #close-icon>
            <IconExpandLess class="text-blue-800" />
          </template>

          <template #header>
            <div class="flex w-full justify-between">
              <div class="flex flex-row items-center gap-16">
                <IconFolderOpen />
                <span class="ds-label-01-reg" :title="procedure.label">{{
                  procedure.label
                }}</span>
                <div
                  class="ds-label-02-reg flex flex-row items-center gap-4 rounded-full bg-blue-300 px-8 py-2 outline-none"
                >
                  <IconBaselineDescription class="w-16 text-blue-800" />
                  <span>
                    {{ procedure.documentUnitCount }}
                  </span>
                </div>
              </div>
              <span class="mr-24"
                >erstellt am
                {{ dayjs(procedure.createdAt).format("DD.MM.YYYY") }}</span
              >
            </div>
          </template>

          <ProcedureDetail
            :procedure="procedure"
            :response-error="responseError"
          />
        </ExpandableContent>
      </Pagination>
    </div>
    <div
      v-else
      class="flex h-full w-full flex-1 items-center justify-center bg-white"
    >
      <div class="flex flex-col items-center space-y-24">
        <span
          class="flex flex h-128 w-128 items-center justify-center justify-center rounded-full bg-blue-200"
        >
          <IconFolderOpen class="text-64 text-blue-700" />
        </span>
        <span>Es wurden noch keine Vorgänge angelegt.</span>
      </div>
    </div>
  </div>
</template>
