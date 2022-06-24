<script setup lang="ts">
import DocUnitInfoPanel from "@/components/DocUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import DocUnit from "@/domain/docUnit"

defineProps<{ docUnit: DocUnit }>()
</script>

<template>
  <v-main>
    <v-container fluid>
      <v-row>
        <v-col cols="2">
          <NavbarSide :id="String(docUnit.id)" />
        </v-col>
        <v-col cols="10" class="panel_and_main_area">
          <DocUnitInfoPanel :doc-unit="docUnit" />
          <slot></slot>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>
