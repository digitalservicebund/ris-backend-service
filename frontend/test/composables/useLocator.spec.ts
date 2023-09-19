import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { defineComponent, h, ref } from "vue"

// Need a bunch of test components here so disabling that rule
/* eslint-disable vue/one-component-per-file */

import { useLocator } from "@/composables/useLocator"

describe("useLocator", () => {
  it("sets root segments in an empty locator", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => h(Child)
      },
    })

    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root")
  })

  it("appends segments to a locator", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child"])
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => h(Child)
      },
    })

    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/child")
  })

  it("branches a locator when segments are added in different children", () => {
    const Child1 = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child1"])
        return () =>
          h("output", { "data-testid": "child1" }, getLocator().value)
      },
    })

    const Child2 = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child2"])
        return () =>
          h("output", { "data-testid": "child2" }, getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => [h(Child1), h(Child2)]
      },
    })

    render(Parent)
    expect(screen.getByTestId("child1")).toHaveTextContent("root/child1")
    expect(screen.getByTestId("child2")).toHaveTextContent("root/child2")
  })

  it("returns a locator with a single leaf", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator(["foo"]).value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => h(Child)
      },
    })

    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/foo")
  })

  it("returns a locator with multiple leaves", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator(["foo", "bar"]).value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => h(Child)
      },
    })

    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/foo/bar")
  })

  it("returns the correct leaf when the locator has branches", () => {
    const Child1 = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child1"])
        return () =>
          h("output", { "data-testid": "child1" }, getLocator(["foo"]).value)
      },
    })

    const Child2 = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child2"])
        return () =>
          h("output", { "data-testid": "child2" }, getLocator(["bar"]).value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root"])
        return () => [h(Child1), h(Child2)]
      },
    })

    render(Parent)
    expect(screen.getByTestId("child1")).toHaveTextContent("root/child1/foo")
    expect(screen.getByTestId("child2")).toHaveTextContent("root/child2/bar")
  })

  it("does not change segments when getting a locator", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment, getLocator } = useLocator()
        addSegment(["root"])
        const locator = getLocator(["foo"])
        return () => h("div", [h("output", locator.value), h(Child)])
      },
    })

    render(Parent)
    const output = screen.getAllByRole("status")
    expect(output[0]).toHaveTextContent("root/foo")
    expect(output[1]).toHaveTextContent("root")
  })

  it("updates the locator in children when reactive segments change", async () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const count = ref(0)
        function increment() {
          count.value++
        }

        const { addSegment } = useLocator()
        addSegment(() => ["root", count.value.toString()])
        return () => h("div", [h("button", { onClick: increment }), h(Child)])
      },
    })

    const user = userEvent.setup()
    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/0")
    await user.click(screen.getByRole("button"))
    expect(screen.getByRole("status")).toHaveTextContent("root/1")
  })

  it("updates the locator in the same component when reactive segments change", async () => {
    const Parent = defineComponent({
      setup() {
        const count = ref(0)
        function increment() {
          count.value++
        }

        const { addSegment, getLocator } = useLocator()
        addSegment(() => ["root", count.value.toString()])

        return () =>
          h("div", [
            h("button", { onClick: increment }),
            h("output", getLocator().value),
          ])
      },
    })

    const user = userEvent.setup()
    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/0")
    await user.click(screen.getByRole("button"))
    expect(screen.getByRole("status")).toHaveTextContent("root/1")
  })

  it("updates the leaf when reactive leaves change", async () => {
    const Parent = defineComponent({
      setup() {
        const count = ref(0)
        function increment() {
          count.value++
        }

        const { addSegment, getLocator } = useLocator()
        addSegment(["root"])

        const dynamicLeaf = getLocator(() => [count.value.toString(), "foo"])

        return () =>
          h("div", [
            h("button", { onClick: increment }),
            h("output", dynamicLeaf.value),
          ])
      },
    })

    const user = userEvent.setup()
    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/0/foo")
    await user.click(screen.getByRole("button"))
    expect(screen.getByRole("status")).toHaveTextContent("root/1/foo")
  })

  it("supports appending multiple segments individually", () => {
    const Child = defineComponent({
      setup() {
        const { getLocator, addSegment } = useLocator()
        addSegment(["child"])
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const { addSegment } = useLocator()
        addSegment(["root1"])
        addSegment(["root2"])
        return () => h(Child)
      },
    })

    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root1/root2/child")
  })

  it("supports appending multiple reactive segments individually", async () => {
    const Child = defineComponent({
      setup() {
        const { getLocator } = useLocator()
        return () => h("output", getLocator().value)
      },
    })

    const Parent = defineComponent({
      setup() {
        const count = ref(0)
        function increment() {
          count.value++
        }

        const { addSegment } = useLocator()
        addSegment(["root"])
        addSegment(() => [count.value.toString(), "foo"])
        return () => h("div", [h("button", { onClick: increment }), h(Child)])
      },
    })

    const user = userEvent.setup()
    render(Parent)
    expect(screen.getByRole("status")).toHaveTextContent("root/0/foo")
    await user.click(screen.getByRole("button"))
    expect(screen.getByRole("status")).toHaveTextContent("root/1/foo")
  })
})
