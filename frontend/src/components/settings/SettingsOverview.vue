<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import AccountManagementSettings from "@/components/settings/AccountManagementSettings.vue"
import ApiKeySettings from "@/components/settings/ApiKeySettings.vue"
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <h3 class="ris-heading3-bold mt-48 ml-64">Einstellungen</h3>
    <div class="mt-56 min-h-screen bg-gray-100 pt-56 pl-64" v-bind="$attrs">
      <AccountManagementSettings />
      <ApiKeySettings class="mt-32" />
    </div>
  </FlexContainer>
</template>
