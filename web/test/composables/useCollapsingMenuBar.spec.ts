import { ref } from "vue"
import {
  MenuButton,
  useCollapsingMenuBar,
} from "@/composables/useCollapsingMenuBar"
import { generateString } from "~/test-helper/dataGenerators"

function generateButton(partialButton?: Partial<MenuButton>) {
  return {
    type: generateString({ prefix: "group-1" }),
    icon: generateString({ prefix: "icon-1" }),
    ariaLabel: generateString({ prefix: "aria-1" }),
    isCollapsable: true,
    ...partialButton,
  }
}

describe("useCollapsingMenuBar", () => {
  it("reduces buttons list length to max bar entries", () => {
    const buttons = [
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
    ]

    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(1))
    expect(collapsedButtons.value).toHaveLength(1)
  })

  it("reduces buttons list length according to max bar entries changes", () => {
    const maxBarEntries = ref(3)
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton({ group: "foo" }),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
    ]

    const { collapsedButtons } = useCollapsingMenuBar(
      ref(buttons),
      maxBarEntries
    )

    expect(collapsedButtons.value).toHaveLength(3)

    maxBarEntries.value = 4
    expect(collapsedButtons.value).toHaveLength(4)

    maxBarEntries.value = 2
    expect(collapsedButtons.value).toHaveLength(2)
  })

  it("groups button list entries of the same group to child button entries", () => {
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
    ]
    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(2))

    expect(collapsedButtons.value).toHaveLength(2)
    expect(collapsedButtons.value[0]).toStrictEqual(buttons[0])
    expect(collapsedButtons.value[1].childButtons).toStrictEqual([
      buttons[1],
      buttons[2],
    ])
  })

  it("renders single buttons without groups at correct position", () => {
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton(),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
      generateButton(),
    ]
    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(4))

    expect(collapsedButtons.value).toHaveLength(4)
    expect(collapsedButtons.value[0]).toStrictEqual(buttons[0])
    expect(collapsedButtons.value[1]).toStrictEqual(buttons[1])
    expect(collapsedButtons.value[2].childButtons).toStrictEqual([
      buttons[2],
      buttons[3],
    ])
    expect(collapsedButtons.value[3]).toStrictEqual(buttons[4])
  })

  it("renders correct button positions", () => {
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton(),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
      generateButton(),
      generateButton(),
    ]
    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(5))

    expect(collapsedButtons.value).toHaveLength(5)
    expect(collapsedButtons.value[0]).toStrictEqual(buttons[0])
    expect(collapsedButtons.value[1]).toStrictEqual(buttons[1])
    expect(collapsedButtons.value[2].childButtons).toStrictEqual([
      buttons[2],
      buttons[3],
      buttons[4],
      buttons[5],
    ])
    expect(collapsedButtons.value[3]).toStrictEqual(buttons[6])
    expect(collapsedButtons.value[4]).toStrictEqual(buttons[7])
  })

  it("only creates new menu item if there are more than one buttons of same group", () => {
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton(),
      generateButton({ group: "bar" }),
    ]
    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(3))

    expect(collapsedButtons.value).toHaveLength(3)
    expect(collapsedButtons.value[0]).toStrictEqual(buttons[0])
    expect(collapsedButtons.value[1]).toStrictEqual(buttons[1])
    expect(collapsedButtons.value[2]).toStrictEqual(buttons[2])
  })

  it("only reduces collapsable buttons", () => {
    const buttons = [
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
      generateButton({ group: "foo", isCollapsable: false }),
      generateButton({ group: "foo", isCollapsable: false }),
      generateButton({ group: "foo", isCollapsable: false }),
    ]

    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(4))
    expect(collapsedButtons.value).toHaveLength(4)
    expect(collapsedButtons.value[0].childButtons).toStrictEqual([
      buttons[0],
      buttons[1],
    ])
    expect(collapsedButtons.value[1]).toStrictEqual(buttons[2])
    expect(collapsedButtons.value[2]).toStrictEqual(buttons[3])
  })

  it("sets the isLast property correctly to render group divider", () => {
    const buttons = [
      generateButton({ group: "foo" }),
      generateButton(),
      generateButton({ group: "bar" }),
      generateButton({ group: "bar" }),
    ]

    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(4))
    expect(collapsedButtons.value).toHaveLength(4)
    expect(collapsedButtons.value[0].isLast).toBe(true)
    expect(collapsedButtons.value[1].isLast).toBe(true)
    expect(collapsedButtons.value[2].isLast).toBe(undefined)
    expect(collapsedButtons.value[3].isLast).toBe(undefined)
  })

  it("renders more button when there is no more buttons to collapse", () => {
    const buttons = [
      generateButton({ isCollapsable: false }),
      generateButton({ isCollapsable: false, isSecondRow: true }),
      generateButton({ isCollapsable: false, isSecondRow: true }),
    ]
    const { collapsedButtons } = useCollapsingMenuBar(ref(buttons), ref(2))
    expect(collapsedButtons.value).toHaveLength(2)
    expect(collapsedButtons.value[0]).toStrictEqual(buttons[0])
    expect(collapsedButtons.value[1].type).toStrictEqual("more")
    expect(collapsedButtons.value[1].childButtons).toStrictEqual([
      buttons[1],
      buttons[2],
    ])
  })
})
