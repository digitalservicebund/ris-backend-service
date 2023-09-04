<script lang="ts" setup>
import { ref, onMounted } from "vue"
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const procedures = ref<Procedure[]>()
const currentPage = ref<Page<Procedure>>()

async function updateProcedures(page: number) {
  const response = await service.getAll(itemsPerPage, page)
  if (response.data) {
    procedures.value = response.data.content
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

onMounted(() => updateProcedures(0))
</script>

<template>
  <div aria-label="Vorgänge" class="bg-blue-200 p-16">
    <div class="flex flex-row">
      <div v-if="procedures" class="flex-1 px-12 py-56">
        <Pagination
          v-if="currentPage"
          navigation-position="bottom"
          :page="currentPage"
          @update-page="updateProcedures"
        >
          <ExpandableContent
            v-for="(procedure, index) in procedures"
            :key="`procedure-${index}`"
            class="mb-24 bg-white p-14"
            close-icon-name="expand_less"
            open-icon-name="expand_more"
            @click="loadDocumentUnits(procedure)"
            @keydown.enter="loadDocumentUnits(procedure)"
          >
            <template #header>
              <div class="relative w-auto">
                <span class="font-bold">{{ procedure.label }}</span>
                <span class="absolute left-208 whitespace-nowrap"
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
