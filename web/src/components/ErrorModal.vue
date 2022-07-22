<script lang="ts" setup>
defineProps<{ title: string; description: string }>()
</script>

<template>
  <v-container class="error">
    <v-row>
      <v-col class="error_icon">
        <v-icon color="#B0243F" size="20px">error</v-icon>
      </v-col>
      <v-col align-self="stretch">
        <div class="error_title">
          {{ title }}
        </div>
        <div>
          {{ description }}
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.error {
  background-color: #f9e5ec;
  border-left: 8px solid #b0243f;
  margin-top: 23px;
}

.error_icon {
  max-width: 20px;
  margin-right: 10px;
}

.error_title {
  font-size: 16px;
  font-weight: 700;
}
</style>
