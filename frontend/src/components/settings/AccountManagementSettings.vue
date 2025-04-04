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
  <div class="flex-col gap-16">
    <span class="ris-label2-bold">Kontoverwaltung</span>
    <div>
      <Button label="Bare.ID öffnen" @click="openBareId"> </Button>
    </div>
  </div>
</template>
