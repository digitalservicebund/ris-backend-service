<script lang="ts" setup>
import { Norm } from "../domain/Norm"

defineProps<{ norms: Norm[] }>()
</script>
<template>
  <div v-if="norms.length" class="p-40 w-[50rem]">
    <div v-for="norm in norms" :key="norm.guid">
      <div>
        <router-link
          class="heading-02-regular mb-[1rem]"
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
