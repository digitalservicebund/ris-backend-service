<script setup lang="ts">
import Button from "primevue/button"
import adminService from "@/services/adminService"

async function openBareId() {
  const response = await adminService.getAccountManagementUrl()
  if (response?.data) {
    window.open(response.data, "_blank")
  } else {
    console.error("Failed to retrieve the account management URL.")
  }
}
</script>

<template>
  <div class="flex flex-col gap-16">
    <span class="ds-label-02-bold">Kontoverwaltung</span>
    <Button label="Bare.ID öffnen" @click="openBareId"> </Button>
  </div>
</template>
