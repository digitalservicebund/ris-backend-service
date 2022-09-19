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

<template>
  <label class="input-field" :for="id">
    <span class="input-field__label">
      <span v-if="!!iconName" class="material-icons" data-testid="icon">{{
        iconName
      }}</span>
      {{ label }}<span v-if="!!required">*</span>
    </span>

    <slot :id="id" />
    <span v-if="!!errorMessage" class="input-field__error">
      {{ errorMessage }}
    </span>
  </label>
</template>

<style lang="scss" scoped>
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
    @apply text-red-800;
  }
}
</style>
