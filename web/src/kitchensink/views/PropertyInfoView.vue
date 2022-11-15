<script lang="ts" setup>
import PropertyInfo from "@/components/PropertyInfo.vue"
</script>

interface Props { label: string value?: string direction?: string }

<template>
  <h1 class="mb-10 mt-20">row:</h1>
  <PropertyInfo direction="row" label="myLabel" value="value" />
  <h1 class="mb-10 mt-40">column:</h1>
  <PropertyInfo direction="column" label="myLabel" value="value" />
</template>
