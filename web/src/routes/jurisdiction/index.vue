<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocUnitList from "@/components/DocumentUnitList.vue"
import TextButton from "@/components/TextButton.vue"
import DocumentUnit from "@/domain/documentUnit"
import docUnitService from "@/services/documentUnitService"

const router = useRouter()

const docUnits = ref((await docUnitService.getAll()).data)

const handleDelete = async (docUnit: DocumentUnit) => {
  const status = (await docUnitService.delete(docUnit.uuid)).status
  if (status === 200) {
    docUnits.value = docUnits.value.filter((item) => item != docUnit)
  }
}
</script>

<template>
  <v-main role="main">
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
