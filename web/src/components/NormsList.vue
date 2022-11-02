<script lang="ts" setup>
import Norms from "../domain/Norm"

defineProps<{ norms: Norms[] }>()
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
  <span v-else>Keine Dokumentationseinheiten gefunden</span>
</template>
