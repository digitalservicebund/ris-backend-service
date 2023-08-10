# 19. Constructing locators in the frontend

Date: 2023-08-09

## Status

Accepted

## Context

> **Note**
>
> This is currently specific to the Norms part of the application, but intended to be reused by Caselaw later on.

The backend [validates user inputs](./0017-addressing-of-validation-errors.md). Validation messages are returned by a validation endpoint and [stored in a Pinia store](./0018-frontend-validation-error-storage.md) in the frontend. Once we retrieved and stored the result, we need a way to associate the messages with the relevant components in the UI. This information can then be used to provide feedback to the user.

Each entry in the validation result contains an `instance` (see the ADRs linked above for details). The instance is a URI pointing to the data that contains the error. We'll be calling this the "locator" later in the implementation. It consists of a list of segments, joined by a separator.

The instance _should_ start with a shared prefix that can be used for grouping related messages, followed by an arbitrary number of segments uniquely identifying each individual message within the scope:

```
prefix/path/to/section/idWithinSection
```

This ADR specifies:

1. What locators should look like on the example of a Norm frame
2. A flexible mechanism for constructing the locator in the frontend

## Decision

### Locator format

For the norm frame, the locator looks like this. `norms` is a shared prefix between all components developed by the norms team:

```
norms/{normUID}/{...SECTION_NAME(s)}/METADATUM_NAME
```

If the locator points at a property inside a repeated section, the 0-based index is be included as a separate segment:

```
norms/{normUID}/{...SECTION_NAME(s)}/0/METADATUM_NAME
```

(Note that the last segment of the locator, i.e. the one that makes it unique, will be called the “leaf”. Similarly, the components at the end of the component tree will be called “leaf components”).

### Constructing a locator

We assume that 1) omponents will be specialized and reused in various places, and 2) that they can't automatically infer the locator themselves based on the data they receive. They then need methods for:

- Communicating to all their children where they are in the component/data hierarchy
- Retrieving their current location
- Constructing a leaf locator based on their current location
- Doing all of the above without having to do manual string concatenation or knowing about all conventions for how locators are constructed

We will accomplish this by combining 2 things:

- A `useLocator` composable exposing two methods:
  - `addSegment(segments: MaybeRefOrGetter<string[]>)`: Allows the component to append segments to the current locator. These will then be available in the component itself and all its children.
  - `getLocator(leaf?: MaybeRefOrGetter<string[]>): ComputedRef<string>`: Allows the component to retrieve the current path. If one ore multiple leaves are provided, they are appended to the locator just like `addSegment` would, but with the difference that they won't affect any locators in children.
  - Note the `MaybeRefOrGetter` type annotation, which comes from Vue. This opens the possibility for the segments to be reactive, so components can update their part of the locator dynamically.
- Repurposing the `id` prop:
  - When components need information from their parent about which segments to append (e.g. varying metadatum names for components used in many places), they will receive that information via the `id` prop.
  - Many components already use an ID as an identifier e.g. for connecting input fields to labels, or for limiting DOM queries in tests to specific parts of the application. Instead of adding a new prop, we will repurpose the ID wherever possible.
  - Note that this is neither strictly necessary nor enforced, but a convention.

### Example

```ts
// App.vue - Here we define the shared root for all children. We accomplish
// this just like if we would append a segment elsewhere.
const { addSegment } = useLocator();
addSegment(() => ["norm", normId.value]);

// ChildComponent.vue - Children can now also append segments. Static segments
// can be a simple string value instead of a function.
const { addSegment } = useLocator();
addSegment(["METADATUM_NAME"]);

// LeafComponent.vue - Components that need to attach the locator to some input
// can get it from the composable.
const { getLocator } = useLocator();
const nameInputLocator = getLocator(() => [props.modelValue.id, "NAME"]);
// will result in: "norm/4711/METADATUM_NAME/4712/NAME"
```

The input locator can then be bound to a supporting component, e.g. the `InputField`. This will then use the locator to retrieve messages from the store:

```vue
<template>
  <InputField :id="nameInputLocator" /><!-- ... -->
</template>
```

### Implementation notes

While the implementation details of the `useLocator` composable outside the scope of this ADR, Vue's [provide/inject pattern](https://vuejs.org/guide/components/provide-inject.html#provide-inject) is a good candidate.

## Consequences

- Components will have a way of knowing where they are
- We will need to update a bunch of IDs (mostly in tests)
