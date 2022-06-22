<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted } from "vue"
import { deleteDocUnit } from "../api/docUnitService"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()

onMounted(() => {
  store.fetchAll()
})

const onDelete = (docUnitId: string) => {
  deleteDocUnit(docUnitId).then(() => {
    store.removeById(docUnitId)
  })
}
</script>

<template>
  <v-table v-if="!store.isEmpty()" class="doc-unit-list-table">
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
      <tr v-for="docUnit in store.getAll()" :key="docUnit.id">
        <td>
          <router-link
            class="doc-unit-list-active-link"
            :to="{
              name: store.hasFileAttached(docUnit.id)
                ? 'Rubriken'
                : 'Dokumente',
              params: { id: docUnit.id },
            }"
          >
            {{ docUnit.id }}
          </router-link>
        </td>
        <td>{{ dayjs(docUnit.creationtimestamp).format("DD.MM.YYYY") }}</td>
        <td>{{ docUnit.aktenzeichen ? docUnit.aktenzeichen : "-" }}</td>
        <td>
          {{ docUnit.filename ? docUnit.filename : "-" }}
        </td>
        <td>
          <v-icon @click="onDelete(docUnit.id)"> delete </v-icon>
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
