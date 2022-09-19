<script lang="ts" setup>
defineProps<{ title: string; description: string }>()
</script>

<template>
  <v-container class="error">
    <v-row>
      <v-col class="error-icon">
        <span class="material-icons">error</span>
      </v-col>
      <v-col align-self="stretch">
        <div class="error-title">
          {{ title }}
        </div>
        <div>
          {{ description }}
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss" scoped>
.error {
  border-left: 8px solid #b0243f;
  margin-top: 23px;
  background-color: #f9e5ec;
}

.error-icon {
  max-width: 20px;
  margin-right: 10px;

  span {
    height: 5px;
    color: #b0243f;
    size: 20px;
  }
}

.error-title {
  font-size: 16px;
  font-weight: 700;
}
</style>
