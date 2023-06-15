# 12. Develop a custom chips component

Date: 2023-05-16

## Status

Accepted

## Context

We need a UI component for users to insert tags - basically a list of plain text values. We need to support the following requirements:

- the chips can be displayed inside the input field our below it
- they should be reachable with arrow keys and tab
- focused chips should be deleted when pressing enter
- keyboard accessible in general
- should be ordered by when they were added, but other sort orders are likely to be required in the future (e.g. alphabetical)
- paste a list of comma-separated values and have them be converted to chips automatically
- no selection from existing values, just simple text
- existing items can be deleted and edited
- additional styling for the input to make it easier to discover / differentiate from a regular input

This leads to the question of whether we want to:

- Continue investing effort into building a custom component or
- Picking a standard browser component or
- Adding a dependency to a 3rd party component

## Decision

There is no browser standard that we could use.

3rd party libraries that offer similar functionality typically do so by way of a multiselect, which falls short of our requirements in several ways (e.g. editing is usually not possible, displaying chips below the control is not supported, discoverability of the input is not great, comma-separated pasting is not supported).

We already have a fairly advanced implementation of a custom chips component.

As a consequence, we'll continue building on our existing implementation and further iterate on it.

## Consequences

- We're in control of the implementation and will be able to support "unusual" requirements such as editing chips, different layouts, and pasting comma-separated values

- We avoid adding dependencies for specific UI components—which are notoriously hard to customize—, without having to reinvent the wheel and build everything from scratch

- We will most likely need to invest some time into testing and continuously improving the component. It's important to note though that any standard or 3rd party components wouldn't support our requirements out of the box either
