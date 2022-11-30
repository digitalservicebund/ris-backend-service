<script lang="ts" setup>
import TupleInput from "@/components/TupleInput.vue"
import type { InputField } from "@/domain"
import { defineDateField } from "@/domain"

const tupleFields: { parent: InputField; child: InputField } = {
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
