<script lang="ts" setup>
import dayjs from "dayjs"
import { computed } from "vue"
import DocumentUnit from "../domain/documentUnit"
import PropertyInfo from "@/components/PropertyInfo.vue"

const props = defineProps<{ documentUnit: DocumentUnit }>()

const entries = computed(() => [
  {
    label: "Aktenzeichen",
    value: props.documentUnit.coreData.fileNumbers?.[0],
  },
  {
    label: "Entscheidungsdatum",
    value: props.documentUnit.coreData.decisionDate
      ? dayjs(props.documentUnit.coreData.decisionDate).format("DD.MM.YYYY")
      : undefined,
  },
  {
    label: "Gericht",
    value: props.documentUnit.coreData.court?.label,
  },
])
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid flex h-80 items-center px-[2rem]"
  >
    <div class="grow text-30">
      {{ documentUnit.documentNumber || " - " }}
    </div>

    <div v-for="entry in entries" :key="entry.label" class="grow">
      <PropertyInfo
        direction="row"
        :label="entry.label"
        :value="entry.value"
      ></PropertyInfo>
    </div>
  </div>
</template>
