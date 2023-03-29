<script lang="ts" setup>
import PropertyInfo from "@/shared/components/PropertyInfo.vue"
</script>

<template>
  <h2 class="heading-02-regular mb-10 mt-20">row:</h2>
  <PropertyInfo direction="row" label="myLabel" value="value" />
  <h2 class="heading-02-regular mb-10 mt-40">column:</h2>
  <PropertyInfo direction="column" label="myLabel" value="value" />
</template>
