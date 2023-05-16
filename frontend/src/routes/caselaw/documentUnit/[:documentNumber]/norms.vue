<script lang="ts" setup>
import { ref } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import { NormReference } from "@/domain/normReference"
import EditableList from "@/shared/components/EditableList.vue"

const selectedNorms = ref<NormReference[]>([])
selectedNorms.value = [
  { abbreviation: "juris1", singleNorm: "§1" },
  { abbreviation: "juris2", singleNorm: "§2" },
  { abbreviation: "juris3", singleNorm: "§3" },
  { abbreviation: "juris4", singleNorm: "§4" },
]
const defaultValue = {}
</script>

<template>
  <div class="m-24">
    <ExpandableDataSet as-column :data-set="selectedNorms" title="Normen">
      <EditableList
        v-model="selectedNorms"
        :default-value="defaultValue"
        :edit-component="NormReferenceInput"
      />
    </ExpandableDataSet>
  </div>
</template>
