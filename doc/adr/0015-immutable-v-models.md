# 15. Immutable v-models

Date: 2023-06-28

## Status

Proposed

## Context

Currently many of our components are using `v-model`s in a way that is likely to cause issues. Specifically, we frequently pass around objects and arrays as models (which in itself is fine), but then work on the **references** to the objects. This means the underlying data changes immediately, breaking [one-way data flow](https://vuejs.org/guide/components/props.html#one-way-data-flow). This is harder to reason about, hard to debug, and leads to issues with Vue's reactivity system.

> [!info] TL;DR
>
> - We're mutating prop values when we really shouldn't
> - This makes our data flow hard to understand and leads to errors that are hard to debug
>
> Proposed solution:
>
> - Make sure models are always treated as immutable, using [Immer](https://immerjs.github.io/immer/) for efficiently updating immutable data structures
> - Do this from now on for all new components
> - Refactor existing components as we go along (perhaps schedule a first batch in cases where it's already causing issues)

### Simplified example

Say we have a parent component and a child component, and the parent passes some data in the form of an object to the child via a `v-model`:

Parent component:

```vue
<script>
import { ref } from "vue";

const someData = ref({
  id: "4711",
  title: "some title",
  items: ["one", "two", "three"],
});
</script>

<template>
  <ChildComponent v-model="someData" />
</template>
```

Child component:

```vue
<script>
import { watch, ref } from "vue"

const props = defineProps<{
  modelValue: Data;
}>()

const emit = defineEmits<{
  ("update:modelValue", value: Data) => void;
}>()

const value = ref(props.modelValue);

watch(
  () => props.modelValue,
  () => { value = props.modelValue },
  { deep: true }
)

watch(value, () => {
  emit("update:modelValue", value)
})
</script>

<template>
  <input v-model="value.title" />
</template>
```

### Expectation

At first look you might think that this:

1. Copies the `modelValue` from the props to the local `value`
2. Wires that up with the input, changes the value when the user types
3. Then emits the updated value to let the parent know what changed
4. The parent stores the updated value
5. ...which is again passed as a prop to the child, thereby updating the value of the input

### Reality

The problem is that's not what happens. In reality:

1. The value is not a copy, but a **reference** to the data in the parent component
2. The input directly mutates data in the _parent_, bypassing the emit event and breaking one-way data flow
   - This is **not allowed** but Vue doesn't detect this for objects and arrays
   - If you tried this with primitive types, Vue would complain
3. The update event either is...
   - Never fired at all (but the data is still updated), or
   - It is fired but redundant, as the data has already been mutated at this point

### Issues

1. Data changes in unpredictable ways, bypassing all update events or checks, which...
   - leads to issues that are very hard to debug
   - means we don't have a central authority that can take care of cleaning up, ensuring consistency, or triggering behavior if certain states are reached
2. Vue's reactivity system often fails to detect these changes (a workaround is to use deep watchers, but they're [discouraged](https://vuejs.org/guide/essentials/watchers.html#deep-watchers) because of their performance overhead)
3. It breaks features such as the `current` and `previous` value parameters in watchers, since they will both point to the same thing. Because of that it's impossible to compare versions and track changes.

Some practical examples of where this causes problems in our app:

- We'll sometimes post garbage data to the API because our forms directly change that root app state, even if the user is still working on it and data is incomplete or wrong. Norms solves this by cleaning up the data before posting to the API, which seems like unnecessary and error-prone work. Caselaw ran into the same issue, where data was changed even though they removed **all** update events.

- The chips component should focus the input control when the last chip is deleted. However it currently can't because 1) the watcher doesn't trigger when the chips list is changed, 2) if you try to fix this using a deep watcher or one of the events provided by the chips list, you'll find that the data doesn't have the content you'd expect (e.g. outdated, "previous" and "current" are the same, ...)

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

Parent component: Can stay the same

Child component:

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
    // readonly is a helper from Vue that returns an immutable
    // proxy to the original object
    return readonly(props.modelValue);
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

## Consequences

- This will also have an effect on our unit tests, since they rely on mutating the model value in many places. [Learn more about testing `v-model`](https://test-utils.vuejs.org/guide/advanced/v-model.html).

- We will implement this pattern in new components from now on.

- We will refactor existing components as we go along. If there are any pressing issues already, we should align with product and see if we can refactor them right away.
