<script lang="ts" setup>
import { ref, onMounted } from "vue"
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import service from "@/services/procedureService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const itemsPerPage = 10
const procedures = ref<Procedure[]>()
const currentPage = ref<Page<Procedure>>()

async function updateProcedures(page: number) {
  const response = await service.getAllWithDocumentUnits(itemsPerPage, page)
  if (response.data) {
    procedures.value = response.data.content
    currentPage.value = response.data
  }
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
          >
            <template #header>
              <div class="relative">
                <span class="font-bold">{{ procedure.label }}</span>
                <span class="absolute left-208 whitespace-nowrap"
                  >{{
                    (procedure.documentUnits as DocumentUnitListEntry[]).length
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
