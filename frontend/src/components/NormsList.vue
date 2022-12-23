<script lang="ts" setup>
interface Norm {
  officialLongTitle: string
  guid: string
}

defineProps<{ norms: Norm[] }>()
</script>
<template>
  <div v-if="norms.length" class="pl-64">
    <div v-for="norm in norms" :key="norm.guid">
      <div class="mb-24">
        <router-link
          class="heading-03-regular"
          :to="{
            name: 'norms-norm-:normGuid',
            params: { normGuid: norm.guid },
          }"
        >
          {{ norm.officialLongTitle }}
        </router-link>
      </div>
    </div>
  </div>
</template>
