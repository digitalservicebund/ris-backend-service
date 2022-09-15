<script lang="ts" setup>
import { computed } from "vue"

interface Props {
  label?: string
  icon?: string
  ariaLabel?: string
  buttonType?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: "Speichern",
  icon: undefined,
  ariaLabel: undefined,
  buttonType: "primary",
})

const buttonClasses = computed(() => ({
  "ris-btn": true,
  "btn-primary": props.buttonType == "primary",
  "btn-secondary": props.buttonType == "secondary",
  "btn-ghost": props.buttonType == "ghost",
}))
</script>

<template>
  <v-btn
    :class="buttonClasses"
    :rounded="0"
    :ripple="false"
    :flat="true"
    :aria-label="ariaLabel"
  >
    <slot>
      <v-icon v-if="props.icon"> {{ props.icon }} </v-icon>
      {{ props.label }}
    </slot>
  </v-btn>
</template>

<style lang="scss" scoped>
.ris-btn {
  font-family: $font-bold;

  &.v-btn {
    text-transform: none;
    font-size: var(--v-btn-size);

    &--size-default {
      --v-btn-height: 48px;
      --v-btn-size: 1rem;

      padding: rem(13px) rem(24px);
    }

    &--size-small {
      --v-btn-height: 40px;
      --v-btn-size: 1rem;

      padding: rem(9px) rem(24px);
    }

    &--size-large,
    &--size-x-large {
      --v-btn-height: 64px;
      --v-btn-size: 1.125rem;

      padding: rem(19px) rem(24px);
    }

    /*
     * TODO:
     * Remove the disable once we established BEM linting.
     */
    /* stylelint-disable selector-class-pattern */
    &:not(.v-btn--icon) {
      .v-icon--start {
        margin-inline-start: 0;
      }

      .v-icon--end {
        margin-inline-end: 0;
      }
    }

    &:hover {
      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:active {
      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:focus-visible {
      outline-offset: 2px;

      .v-btn__overlay {
        opacity: 0;
      }
    }
    /* stylelint-enable selector-class-pattern */
  }
}

.btn-primary {
  background-color: $blue800 !important;
  color: $white !important;

  &.v-btn {
    &:hover {
      background-color: $blue700 !important;
    }

    &:active {
      background-color: $blue500 !important;
    }

    &:focus-visible {
      outline: 2px solid $blue800;
    }

    &:disabled {
      background-color: #dcdee1 !important;
      color: #4e596a !important;
    }
  }
}

.btn-secondary {
  background-color: #f5e05d !important;
  color: $blue800 !important;
  border: solid 2px $blue800;

  &.v-btn {
    &:hover {
      background-color: #e5ce5c !important;
    }

    &:active {
      background-color: #f7e67d !important;
    }

    &:focus-visible {
      outline: 2px solid $blue800;
      border: none;
    }
  }
}

.btn-ghost {
  background: none !important;
  color: $blue800 !important;
  border: 2px solid $white !important;
  text-decoration: underline;

  &.v-btn {
    &:hover {
      background-color: $white !important;
      border: 2px solid #b8bdc3 !important;
    }

    &:active {
      background-color: $white !important;
      border: none;
    }

    &:focus-visible {
      outline: 2px solid $blue800;
      border: 2px solid #b8bdc3 !important;
    }
  }
}
</style>
