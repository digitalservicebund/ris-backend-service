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
    :aria-label="ariaLabel"
    :class="buttonClasses"
    flat
    :ripple="false"
    :rounded="0"
  >
    <slot>
      <v-icon v-if="icon"> {{ icon }} </v-icon>
      {{ label }}
    </slot>
  </v-btn>
</template>

<style lang="scss" scoped>
.ris-btn {
  @apply bg-blue-800 font-bold;

  &.v-btn {
    font-size: var(--v-btn-size);
    text-transform: none;

    &--size-default {
      --v-btn-height: 48px;
      --v-btn-size: 1rem;

      padding: 1rem 1.5rem;
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
  @apply bg-blue-800 text-white;

  &.v-btn {
    &:hover {
      @apply bg-blue-800;
    }

    &:active {
      @apply bg-blue-500;
    }

    &:focus-visible {
      @apply border-2 border-solid border-blue-800;
    }

    &:disabled {
      @apply bg-gray-400 text-gray-900;
    }
  }
}

.btn-secondary {
  @apply border-solid border-2 border-blue-800 text-blue-800 bg-yellow-500;

  &.v-btn {
    &:hover {
      @apply bg-yellow-700;
    }

    &:active {
      @apply bg-yellow-400;
    }

    &:focus-visible {
      @apply border-solid border-2 border-blue-800;
    }
  }
}

.btn-ghost {
  @apply bg-blue-800 text-white;

  &.v-btn {
    &:hover {
      @apply bg-blue-800;
    }

    &:active {
      @apply border-none bg-white;
    }

    &:focus-visible {
      @apply border-2 border-solid border-gray-600;
    }
  }
}
</style>
