<script lang="ts" setup>
import ProcedureDetail from "./ProcedureDetail.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import service from "@/services/procedureService"

const { data: procedures, error } = await service.getAll()
</script>

<template>
  <div aria-label="Vorgänge" class="bg-blue-200 p-16">
    <div class="flex flex-row">
      <div v-if="procedures" class="flex-1 px-12 py-56">
        <ExpandableContent
          v-for="(procedure, index) in procedures"
          :key="`procedure-${index}`"
          class="mb-24 bg-white p-14"
        >
          <template #header>
            <div class="relative">
              <span class="font-bold">{{ procedure.label }}</span>
              <span class="absolute left-208 whitespace-nowrap"
                >{{ procedure.documentUnitCount }} Dokumentationseinheiten</span
              >
            </div>
          </template>

          <ProcedureDetail :procedure="procedure" />
        </ExpandableContent>
      </div>
      <div v-else>
        <RouteErrorDisplay :error="error" />
      </div>
    </div>
  </div>
</template>
