<template>
  <label :for="id" class="input-field">
    <span class="input-field__label">
      <v-icon v-if="!!iconName" data-testid="icon">{{ iconName }}</v-icon>
      {{ label }}<span v-if="!!required">*</span>
    </span>

    <slot :id="id" />
    <span v-if="!!errorMessage" class="input-field__error">
      {{ errorMessage }}
    </span>
  </label>
</template>

<script lang="ts" setup>
interface Props {
  id: string
  label: string
  iconName?: string
  errorMessage?: string
  required?: boolean
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
@import "@/styles/variables";

.input-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;

  &__label {
    display: flex;
    gap: 0.3rem;
  }

  &__error {
    display: flex;
    gap: 0.3rem;
    color: $error;
  }
}
</style>
