<script lang="ts" setup>
import { onMounted } from "vue"
import { useDocUnitsStore } from "../../store"

const docUnitsStore = useDocUnitsStore()

onMounted(() => {
  docUnitsStore.fetchAll()
})
</script>

<template>
  <v-table v-if="!docUnitsStore.isEmpty()">
    <thead>
      <tr>
        <th class="text-center">Dok.-Nummer</th>
        <th class="text-center">Angelegt am</th>
        <th class="text-center">Aktenzeichen</th>
        <th class="text-center">Dokumente</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="docUnit in docUnitsStore.getAll()" :key="docUnit.id">
        <td>
          <router-link :to="{ name: 'Rubriken', params: { id: docUnit.id } }">
            {{ docUnit.id }}
          </router-link>
        </td>
        <td>-</td>
        <td>{{ docUnit.aktenzeichen }}</td>
        <td>
          <router-link
            v-if="docUnit.filename"
            :to="{ name: 'Dokumente', params: { id: docUnit.id } }"
          >
            {{ docUnit.filename }}
          </router-link>
        </td>
      </tr>
    </tbody>
  </v-table>
  <span v-else>No doc units found</span>
</template>
