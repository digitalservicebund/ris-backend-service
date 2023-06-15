# 11. Develop a custom date input component

Date: 2023-05-16

## Status

Accepted

## Context

We need a UI component for users to insert dates. Our current solution uses the browser's native date input with some custom styling. This leads to a few issues, e.g.:

- edge cases around validation
- automatically "correcting" wrong dates (which we don't want)
- usability and accessibility issues related to the browser's date picker

There's also a WIP implementation of a simplified text-based date input that doesn't show a date picker.

We need to support the following requirements:

- Keyboard-first
  - at this point our users are power users who don't want to pick dates manually. They prefer to type or paste them
  - For now we _do not_ want to show a date picker
- Require various specific formats such as `DD.MM.YYYY`, `YYYY`, `MM.YYYY`, `DD.MM`
- Validate that the value is a date
- Not interfere with user inputs automatically

This leads to the question of whether we want to:

- Continue investing effort into building a custom component or
- Picking a standard browser component or
- Adding a dependency to a 3rd party component

## Decision

The browser standard control doesn't allow enough flexibility for input formats and can't hide the date picker widget reliably across browsers.

There are not really any 3rd party components (outside of full blown component libraries) that fit our requirements, probably because it's pretty straightforward once the date picker widget is out of the picture.

As a consequence, **we'll build a custom date input based on the current WIP implementation of a text-based date input**. The component will be an `<input type="text">` at its core. Specific data formats will be enforced by an input masking library such as [maska](https://github.com/beholdr/maska).

## Consequences

- We're in control of the implementation and will be able to support "unusual" requirements such as the different date formats, keyboard-centric interaction, and not interfering with user input automatically

- We avoid adding dependencies for specific UI components—which are notoriously hard to customize—, without having to reinvent the wheel and build everything from scratch

- We will most likely need to invest some time into testing and continuously improving the component. It's important to note though that any standard or 3rd party components wouldn't support our requirements out of the box either

- It's a lightweight solution that is good enough for now, and we can easily replace it or scale it up should we need something more comprehensive in the future

- Relying on a simple text-input with masking should not introduce any accessibility issues or cross-browser incompatibilities
