import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { describe, test } from "vitest"
import NoteStatusIndicationGroup from "@/components/statusIndication/NoteStatusIndicationGroup.vue"
import { MetadataSectionName } from "@/domain/Norm"

type NoteStatusIndicationGroupProps = InstanceType<
  typeof NoteStatusIndicationGroup
>["$props"]

function renderComponent(props: Partial<NoteStatusIndicationGroupProps>) {
  const defaultProps: NoteStatusIndicationGroupProps = {
    modelValue: {},
    ...props,
  }

  return render(NoteStatusIndicationGroup, { props: defaultProps })
}

describe("NoteStatusIndicationGroup in repeal mode", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
  test("should render", () => {
    renderComponent({ type: MetadataSectionName.REPEAL })
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      type: MetadataSectionName.REPEAL,
      modelValue: { TEXT: ["foo"] },
    })

    const input = screen.getByRole("textbox", { name: "Aufhebung" })
    expect(input).toHaveValue("foo")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = { TEXT: ["foo"] }
    renderComponent({ type: MetadataSectionName.REPEAL, modelValue })

    const input = screen.getByRole("textbox", { name: "Aufhebung" })
    expect(input).toHaveValue("foo")

    await user.clear(input)
    await user.type(input, "bar")
    expect(modelValue.TEXT).toEqual(["bar"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = { TEXT: ["foo"] }
    renderComponent({ type: MetadataSectionName.REPEAL, modelValue })

    const input = screen.getByRole("textbox", { name: "Aufhebung" })
    expect(input).toHaveValue("foo")

    await user.clear(input)
    expect(modelValue.TEXT).toBeUndefined()
  })
})

describe("NoteStatusIndicationGroup in other status mode", () => {
  test("should render", () => {
    renderComponent({ type: MetadataSectionName.OTHER_STATUS })
  })

  test("should render all inputs and correct model value", () => {
    renderComponent({
      type: MetadataSectionName.OTHER_STATUS,
      modelValue: { NOTE: ["foo"] },
    })

    const input = screen.getByRole("textbox", { name: "Sonstiger Hinweis" })
    expect(input).toHaveValue("foo")
  })

  test("should change the model value when updating the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = { NOTE: ["foo"] }
    renderComponent({ type: MetadataSectionName.OTHER_STATUS, modelValue })

    const input = screen.getByRole("textbox", { name: "Sonstiger Hinweis" })
    expect(input).toHaveValue("foo")

    await user.type(input, "{backspace}{backspace}{backspace}bar")
    expect(modelValue.NOTE).toEqual(["bar"])
  })

  test("should change the model value when clearing the inputs", async () => {
    const user = userEvent.setup()
    const modelValue = { NOTE: ["foo"] }
    renderComponent({ type: MetadataSectionName.OTHER_STATUS, modelValue })

    const input = screen.getByRole("textbox", { name: "Sonstiger Hinweis" })
    expect(input).toHaveValue("foo")

    await user.clear(input)
    expect(modelValue.NOTE).toBeUndefined()
  })
})
