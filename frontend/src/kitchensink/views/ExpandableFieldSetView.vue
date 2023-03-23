<script lang="ts" setup>
import ExpandableFieldset from "@/components/ExpandableFieldset.vue"
import InputGroup from "@/components/InputGroup.vue"
import { participatingInstitutions } from "@/fields/norms/participatingInstitutions"

const frameData = {
  participationInstitution:
    "Verordnung zur Durchführung der Erstattung von Mitteln",
  participationType: "Finanzdisziplin",
}
</script>

<template>
  <ExpandableFieldset header-id="generalDataFields">
    <template #header>
      <h2 id="generalDataFields" class="link-01-bold">Mitwirkende Organe</h2>
    </template>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="participatingInstitutions"
    />
  </ExpandableFieldset>
</template>
