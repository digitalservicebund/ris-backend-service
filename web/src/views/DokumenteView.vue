<script lang="ts" setup>
import RouteHelper from "../components/RouteHelper.vue"
import UploadFile from "../components/upload-file/UploadFile.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()
</script>

<template>
  <RouteHelper />
  <UploadFile v-if="store.hasSelected()" />
</template>
