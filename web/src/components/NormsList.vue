<script lang="ts" setup>
import { Norm } from "../domain/Norm"

defineProps<{ norms: Norm[] }>()
</script>
<template>
  <div v-if="norms.length" class="pl-96 pt-[3.5rem]">
    <div v-for="norm in norms" :key="norm.guid">
      <div class="mb-24">
        <router-link
          class="heading-02-regular"
          :to="{
            name: 'norms-norm-:normGuid',
            params: { normGuid: norm.guid },
          }"
        >
          {{ norm.longTitle }}
        </router-link>
      </div>
    </div>
  </div>
</template>
