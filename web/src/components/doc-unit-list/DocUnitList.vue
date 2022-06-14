<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted } from "vue"
import { deleteDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()

onMounted(() => {
  store.fetchAll()
})

const onDelete = (docUnitId: number) => {
  deleteDocUnit(docUnitId).then(() => {
    store.removeById(docUnitId)
  })
}
</script>

<template>
  <v-table v-if="!store.isEmpty()">
    <thead>
      <tr>
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
        <td>{{ docUnit.aktenzeichen }}</td>
        <td>
          <router-link
            v-if="docUnit.filename"
            :to="{ name: 'Dokumente', params: { id: docUnit.id } }"
          >
            {{ docUnit.filename }}
          </router-link>
        </td>
        <td>
          <v-icon @click="onDelete(docUnit.id)"> delete </v-icon>
        </td>
      </tr>
    </tbody>
  </v-table>
  <span v-else>No doc units found</span>
</template>
