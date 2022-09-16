<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import TextButton from "@/components/TextButton.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

const documentUnits = ref((await documentUnitService.getAll()).data)

const handleDelete = async (documentUnit: DocumentUnit) => {
  const status = (await documentUnitService.delete(documentUnit.uuid)).status
  if (status === 200) {
    documentUnits.value = documentUnits.value.filter(
      (item) => item != documentUnit
    )
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
            @click="router.push({ name: 'jurisdiction-documentUnit-new' })"
          />
        </v-col>
      </v-row>
      <v-row>
        <v-col> </v-col>
      </v-row>
      <v-row class="text-center">
        <v-col class="mb-4">
          <DocumentUnitList
            :document-units="documentUnits"
            @delete-document-unit="handleDelete"
          />
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>
