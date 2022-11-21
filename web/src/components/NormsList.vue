<script lang="ts" setup>
interface Norm {
  longTitle: string
  guid: string
}

defineProps<{ norms: Norm[] }>()
</script>
<template>
  <div v-if="norms.length" class="pl-64 pt-[3.5rem]">
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
