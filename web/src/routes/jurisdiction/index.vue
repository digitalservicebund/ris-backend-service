<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocUnitList from "@/components/DocUnitList.vue"
import TextButton from "@/components/TextButton.vue"
import DocUnit from "@/domain/docUnit"
import docUnitService from "@/services/docUnitService"

const router = useRouter()
const docUnits = ref(await docUnitService.getAll())

const handleDelete = async (docUnit: DocUnit) => {
  const status = await docUnitService.delete(docUnit.uuid)
  if (status === 200) {
    docUnits.value = docUnits.value.filter((item) => item != docUnit)
  }
}
</script>

<template>
  <v-main>
    <v-container fluid>
      <v-row>
        <v-col class="rechtsprechung-header"><h1>Rechtsprechung</h1></v-col>
      </v-row>
      <v-row class="text-right">
        <v-col>
          <TextButton
            label="Neue Dokumentationseinheit"
            @click="router.push({ name: 'jurisdiction-docUnit-new' })"
          />
        </v-col>
      </v-row>
      <v-row>
        <v-col> </v-col>
      </v-row>
      <v-row class="text-center">
        <v-col class="mb-4">
          <DocUnitList :doc-units="docUnits" @delete-doc-unit="handleDelete" />
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>
<style lang="scss"></style>
