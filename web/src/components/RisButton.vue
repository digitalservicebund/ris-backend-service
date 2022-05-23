<script lang="ts" setup>
defineProps<{
  label: string
}>()
</script>

<template>
  <v-btn class="ris-btn" :rounded="0" :ripple="false" :flat="true">
    {{ label }}
  </v-btn>
</template>

<style lang="scss">
.ris-btn {
  font-family: $font-bold;

  &.v-btn {
    text-transform: none;
    font-size: var(--v-btn-size);

    &--size-default {
      --v-btn-height: 48px;
      --v-btn-size: 1rem;
      padding: 13px 24px;
    }

    &--size-small {
      --v-btn-height: 40px;
      --v-btn-size: 1rem;
      padding: 9px 24px;
    }

    &--size-large,
    &--size-x-large {
      --v-btn-height: 64px;
      --v-btn-size: 1.125rem;
      padding: 19px 24px;
    }

    &:not(.v-btn--icon) {
      .v-icon--start {
        margin-inline-start: 0;
      }

      .v-icon--end {
        margin-inline-end: 0;
      }
    }

    &:hover {
      background-color: $blue700 !important;

      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:active {
      background-color: $blue500 !important;

      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:focus-visible {
      outline: 2px solid $blue800;
      outline-offset: 2px;

      .v-btn__overlay {
        opacity: 0;
      }
    }
  }
}
</style>
