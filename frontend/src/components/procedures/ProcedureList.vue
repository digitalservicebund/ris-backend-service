<script lang="ts" setup>
import { ref, onMounted, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const procedures = ref<Procedure[]>()
const currentPage = ref<Page<Procedure>>()

const route = useRoute()
const router = useRouter()

const queries = ref<{ [key: string]: string }>({ q: "" })

async function updateProcedures(page: number, filter?: string) {
  const response = await service.getAll(itemsPerPage, page, filter)
  if (response.data) {
    procedures.value = copyDocumentUnits(
      response.data.content,
      procedures.value,
    )

    currentPage.value = response.data
  }
}

async function loadDocumentUnits(loadingProcedure: Procedure) {
  if (!procedures.value) return
  if (loadingProcedure.documentUnitCount == 0) return
  if (loadingProcedure.documentUnits) return

  const response = await service.getDocumentUnits(loadingProcedure.label)

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

watch(
  queries,
  () => {
    updateProcedures(currentPage.value?.number || 0, queries.value.q)
    router.push(queries.value.q ? { query: queries.value } : {})
  },
  { deep: true },
)

onMounted(() => {
  if (route.query.q) queries.value.q = route.query.q as string
  updateProcedures(0, queries.value.q)
})
</script>

<template>
  <div class="bg-white px-32 pb-16 pt-32">
    <InputField
      id="procedureFilter"
      label="Dokumentnummer oder Aktenzeichen"
      visually-hide-label
    >
      <TextInput
        id="procedureFilter"
        v-model="queries.q"
        aria-label="Dokumentnummer oder Aktenzeichen Suche"
        class="ds-input-medium"
        placeholder="Nach Vorgängen suchen"
      ></TextInput>
    </InputField>
  </div>
  <div class="bg-blue-200 px-32 pt-24">
    <div class="flex flex-row">
      <div v-if="procedures" class="flex-1 py-56">
        <Pagination
          v-if="currentPage"
          navigation-position="bottom"
          :page="currentPage"
          @update-page="updateProcedures"
        >
          <ExpandableContent
            v-for="procedure in procedures"
            :key="procedure.label"
            class="mb-24 bg-white p-16"
            close-icon-name="expand_less"
            open-icon-name="expand_more"
            @update:is-expanded="
              (isExpanded) => isExpanded && loadDocumentUnits(procedure)
            "
          >
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
