<script lang="ts" setup>
import dayjs from "dayjs"
import { DocUnit } from "../types/DocUnit"

defineProps<{ docUnits: DocUnit[] }>()
const emit = defineEmits<{
  (e: "deleteDocUnit", docUnit: DocUnit): void
}>()

const onDelete = (docUnit: DocUnit) => {
  emit("deleteDocUnit", docUnit)
}
</script>

<template>
  <v-table v-if="docUnits.length" class="doc-unit-list-table">
    <thead>
      <tr class="table-header">
        <th class="text-center">Dok.-Nummer</th>
        <th class="text-center">Angelegt am</th>
        <th class="text-center">Aktenzeichen</th>
        <th class="text-center">Dokumente</th>
        <th class="text-center">Löschen</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="docUnit in docUnits" :key="docUnit.id">
        <td>
          <router-link
            class="doc-unit-list-active-link"
            :to="{
              name: docUnit.s3path ? 'Rubriken' : 'Dokumente',
              params: { id: docUnit.documentnumber },
            }"
          >
            {{ docUnit.documentnumber }}
          </router-link>
        </td>
        <td>{{ dayjs(docUnit.creationtimestamp).format("DD.MM.YYYY") }}</td>
        <td>{{ docUnit.aktenzeichen ? docUnit.aktenzeichen : "-" }}</td>
        <td>
          {{ docUnit.filename ? docUnit.filename : "-" }}
        </td>
        <td>
          <v-icon
            aria-label="Dokumentationseinheit löschen"
            @click="onDelete(docUnit)"
          >
            delete
          </v-icon>
        </td>
      </tr>
    </tbody>
  </v-table>
  <span v-else>Keine Dokumentationseinheiten gefunden</span>
</template>

<style lang="scss">
.table-header {
  background-color: $gray400;
}
.doc-unit-list-table td,
th {
  font-size: medium !important;
}
.doc-unit-list-active-link {
  text-decoration: underline;
}
</style>
