<script setup lang="ts">
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import { computed, ref } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const data = [
  {
    id: "1000",
    code: "f230fh0g3",
    name: "Bamboo Watch",
    category: "Accessories",
    quantity: 24,
  },
  {
    id: "1001",
    code: "nvklal433",
    name: "Black Watch",
    category: "Accessories",
    quantity: 61,
  },
  {
    id: "1002",
    code: "zz21cz3c1",
    name: "Blue Band",
    category: "Fitness",
    quantity: 2,
  },
]

// Dynamically generate the columns based on the keys in the data
const columns = [
  { field: "code", header: "Code" },
  { field: "name", header: "Name" },
  { field: "category", header: "Category" },
  { field: "quantity", header: "Quantity" },
]
const selected = ref([])

const isLoading = ref(true)
const hasError = ref(true)
const emptyText = computed(() =>
  hasError.value ? "" : "Keine Daten vorhanden",
)
</script>

<template>
  <KitchensinkPage name="Table">
    <KitchensinkStory name="Default">
      <DataTable :value="data">
        <Column
          v-for="col in columns"
          :key="col.field"
          :field="col.field"
          :header="col.header"
        />
      </DataTable>
    </KitchensinkStory>

    <KitchensinkStory name="Empty">
      <DataTable selection-mode="multiple" :value="[]">
        <Column selection-mode="multiple"></Column>
        <Column field="code" header="Code"></Column>
        <Column field="name" header="Name"></Column>
        <Column field="category" header="Category"></Column>
        <Column field="quantity" header="Quantity"></Column>
        <template #empty> Keine Daten vorhanden. </template>
      </DataTable>
    </KitchensinkStory>

    <KitchensinkStory name="Loading">
      <DataTable :loading="isLoading" :value="[]">
        <Column field="code" header="Code"></Column>
        <Column field="name" header="Name"></Column>
        <Column field="category" header="Category"></Column>
        <Column field="quantity" header="Quantity"></Column>
        <template #empty> {{ emptyText }} </template>
      </DataTable>
    </KitchensinkStory>

    <KitchensinkStory name="Error">
      <!-- 
        ⚠️ PrimeVue’s theming system is static — it cannot access component state like `hasError`.

        Because of this, the DataTable will always render the empty slot and apply vertical padding 
        from `emptyMessageCell`, even in error states. This can lead to unwanted visual gaps.

        A workaround is to conditionally override the `emptyMessageCell` padding using pass-through options.
        While this solves the visual issue, it adds complexity and couples styling to logic.

        ✅ Preferred approach: Treat the error display (`InfoModal`) and the DataTable as separate components.

        You can either:
        - Render the `InfoModal` above the DataTable when an error is present, or
        - Conditionally render *only* the InfoModal *or* the DataTable.

        This keeps the layout clean, avoids styling workarounds, and makes the UI more maintainable.
      -->
      <InfoModal v-if="hasError" :status="InfoStatus.ERROR" title="Fehler" />
      <DataTable :value="[]">
        <Column field="code" header="Code" />
        <Column field="name" header="Name" />
        <Column field="category" header="Category" />
        <Column field="quantity" header="Quantity" />
        <template #empty>Keine Daten vorhanden.</template>
      </DataTable>
    </KitchensinkStory>

    <KitchensinkStory name="Selectable">
      <DataTable
        v-model:selection="selected"
        selection-mode="multiple"
        :value="data"
      >
        <Column selection-mode="multiple"></Column>
        <Column field="code" header="Code"></Column>
        <Column field="name" header="Name"></Column>
        <Column field="category" header="Category"></Column>
        <Column field="quantity" header="Quantity"></Column>
      </DataTable>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
