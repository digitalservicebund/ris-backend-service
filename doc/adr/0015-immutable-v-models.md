# 15. Immutable v-models

Date: 2023-06-28

## Status

Accepted

## Context

Currently many of our components are using `v-model`s in a way that is likely to cause issues. Specifically, we frequently pass around objects and arrays as models (which in itself is fine), but then work on the **references** to the objects. This means the underlying data changes immediately, breaking [one-way data flow](https://vuejs.org/guide/components/props.html#one-way-data-flow). This is harder to reason about, hard to debug, and leads to issues with Vue's reactivity system.

This causes a number of issues:

1. Data changes in unpredictable ways, bypassing all update events or checks, which...
   - leads to issues that are very hard to debug
   - means we don't have a central authority that can take care of cleaning up, ensuring consistency, or triggering behavior if certain states are reached
2. Vue's reactivity system often fails to detect these changes (a workaround is to use deep watchers, but they're [discouraged](https://vuejs.org/guide/essentials/watchers.html#deep-watchers) because of their performance overhead)
3. It breaks features such as the `current` and `previous` value parameters in watchers, since they will both point to the same thing. Because of that it's impossible to compare versions and track changes.

## Decision

1. Never mutate data that has been passed as props, neither directly nor indirectly through references.
2. There should only ever be one "owner" of a piece of data (e.g. a higher level component or a store)
   - Only the owner is allowed to change data
   - Everyone but the owner **has** to treat the data as readonly = immutable
3. If someone other than the owner wants to change data, they need to:
   - Create a copy of the data, make the desired changes (in very simple cases such as an array of strings, this can be done by spreading, otherwise a library optimized for immutable data structures like [Immer](https://immerjs.github.io/immer/) will result in better performance and better developer ergonomics)
   - Emit an event containing the copy which the parent then manages as it sees fit (usually will just replace the current value)
4. Prefer, where possible, props with primitive types over reference types, even if that results in more props. This is more straightforward to implement and results in better performance, because we don't need to worry about immutability in the first place.

### Example

```vue
<script>
import { computed, ref } from "vue"
import { produce } from "immer"

const props = defineProps<{
  modelValue: Data;
}>()

const emit = defineEmits<{
  ("update:modelValue", value: Data) => void;
}>()

const localModelValue = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emit("update:modelValue", value);
  }
})

// We no longer need the watchers + maintaining a copy of the prop

updateTitle(newTitle: string) {
  localModelValue.value = produce((draft) => {
    draft.title = newTitle;
  })
}
</script>

<template>
  <input
    :value="localModelValue.title"
    @input="updateTitle($event.target.value)"
  />
</template>
```

In the future, we will be able to further simplify this thanks to the (currently experimental) [`defineModel` compiler macro](https://github.com/vuejs/rfcs/discussions/503).

## Consequences

- This will also have an effect on our unit tests, since they rely on mutating the model value in many places. [Learn more about testing `v-model`](https://test-utils.vuejs.org/guide/advanced/v-model.html).

- We will implement this pattern in new components from now on.

- We will refactor existing components as we go along. If there are any pressing issues already, we should align with product and see if we can refactor them right away.
