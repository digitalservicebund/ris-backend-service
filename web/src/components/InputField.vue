<template>
  <label :for="id" class="input-field">
    <span class="input-field__label">
      <v-icon data-testid="icon">{{ iconName }}</v-icon>
      {{ label
      }}<span v-if="requiredText !== undefined">{{ requiredText }}</span>
    </span>

    <slot :id="id" />
  </label>
</template>

<script lang="ts" setup>
interface Props {
  id: string
  label: string
  iconName: string
  requiredText?: string
}

defineProps<Props>()
</script>

<style lang="scss" setup>
.input-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;

  &__label {
    display: flex;
    gap: 0.3rem;
  }
}
</style>
