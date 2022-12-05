<script lang="ts" setup>
import TupleInput from "@/components/TupleInput.vue"
import { defineDateField, TupleInputAttributes } from "@/domain"

const tupleFields: TupleInputAttributes["fields"] = {
  parent: defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "Entscheidungsdatum",
    true,
    undefined
  ),
  child: defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "Entscheidungsdatum",
    true,
    undefined
  ),
}
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Text Input</h1>

    <div>
      <TupleInput aria-label="Tuple Input" :fields="tupleFields"></TupleInput>
    </div>
  </div>
</template>
