<script lang="ts" setup>
import { ref, onMounted, watch } from "vue"
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"
import IconExpandLess from "~icons/ic/baseline-expand-less"
import IconExpandMore from "~icons/ic/baseline-expand-more"

const itemsPerPage = 10
const procedures = ref<Procedure[]>()
const currentPage = ref<Page<Procedure>>()

async function updateProcedures(page: number, queries?: Query<string>) {
  const response = await service.get(itemsPerPage, page, queries?.q)
  if (response.data) {
    procedures.value = copyDocumentUnits(
      response.data.content,
      procedures.value,
    )

    currentPage.value = response.data
  }
}

const { getQueryFromRoute, pushQueryToRoute, route } = useQuery<"q">()
const query = ref(getQueryFromRoute())

async function loadDocumentUnits(loadingProcedure: Procedure) {
  if (!procedures.value) return
  if (loadingProcedure.documentUnitCount == 0) return
  if (loadingProcedure.documentUnits) return

  const response = await service.getDocumentUnits(loadingProcedure.id)

  procedures.value = procedures.value.map((procedure) =>
    procedure.label == loadingProcedure.label
      ? { ...procedure, documentUnits: response.data }
      : procedure,
  )
}

function copyDocumentUnits(
  newProcedures: Procedure[],
  oldProcedures?: Procedure[],
): Procedure[] {
  return newProcedures.map((newProcedure) => {
    const oldProcedure = oldProcedures?.find(
      (oldProcedure) => oldProcedure.label == newProcedure.label,
    )
    return oldProcedure?.documentUnits ? oldProcedure : newProcedure
  })
}

const debouncedPushQueryToRoute = (() => {
  let timeoutId: number | null = null

  return (currentQuerry: Query<string>) => {
    if (timeoutId != null) window.clearTimeout(timeoutId)

    timeoutId = window.setTimeout(() => pushQueryToRoute(currentQuerry), 500)
  }
})()

onMounted(() => {
  updateProcedures(0, query.value)
})

watch(route, () => {
  const currentQuery = getQueryFromRoute()
  if (JSON.stringify(query.value) != JSON.stringify(currentQuery))
    query.value = currentQuery
})

watch(
  query,
  async () => {
    await updateProcedures(0, query.value)
    debouncedPushQueryToRoute(query.value)
  },
  { deep: true },
)
</script>

<template>
  <header class="bg-white px-16 py-16">
    <h1 class="ds-heading-02-reg">Vorgänge</h1>
    <div class="mt-32" role="search">
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
  </header>
  <div class="min-h-screen bg-blue-200 px-16 pt-24" role="main">
    <div class="flex flex-row">
      <div v-if="procedures" class="flex-1 py-56">
        <Pagination
          v-if="currentPage"
          navigation-position="bottom"
          :page="currentPage"
          @update-page="(page) => updateProcedures(page, query)"
        >
          <ExpandableContent
            v-for="procedure in procedures"
            :key="procedure.label"
            class="mb-24 bg-white p-16"
            @update:is-expanded="
              (isExpanded) => isExpanded && loadDocumentUnits(procedure)
            "
          >
            <template #open-icon>
              <IconExpandMore />
            </template>

            <template #close-icon>
              <IconExpandLess />
            </template>

            <template #header>
              <div class="grid grid-cols-[14em,max-content] gap-x-24">
                <span class="truncate font-bold" :title="procedure.label">{{
                  procedure.label
                }}</span>
                <span class="mr-16"
                  >{{
                    procedure.documentUnitCount
                  }}
                  Dokumentationseinheiten</span
                >
              </div>
            </template>

            <ProcedureDetail :procedure="procedure" />
          </ExpandableContent>
        </Pagination>
      </div>
    </div>
  </div>
</template>
