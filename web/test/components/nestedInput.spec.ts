import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import NestedInput from "@/components/NestedInput.vue"
import { NestedInputAttributes, NestedInputModelType } from "@/domain"
import { defineTextField, defineDateField } from "@/domain/coreDataFields"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: NestedInputModelType
  fields?: NestedInputAttributes["fields"]
}) {
  const props = {
    ariaLabel: options?.ariaLabel ?? "Toggle label",
    modelValue: options?.modelValue,
    fields: options?.fields ?? {
      parent: defineTextField(
        "text input 1",
        "text input 1",
        "text input 1 label",
        false
      ),
      child: defineTextField(
        "text input 2",
        "text input 2",
        "text input 2 label",
        false
      ),
    },
  }
  const renderResult = render(NestedInput, { props })
  const user = userEvent.setup()
  return { user, props, ...renderResult }
}

describe("NestedInput", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  it("renders nested input with two text input fields", async () => {
    const { queryByLabelText } = renderComponent()

    const input1 = queryByLabelText("text input 1 label") as HTMLInputElement
    const input2 = queryByLabelText("text input 2 label") as HTMLInputElement

    expect(input1).toBeInTheDocument()
    expect(input2).toBeInTheDocument()

    expect(input1).toBeVisible()
    expect(input2).not.toBeVisible()
  })

  it("updates value when user types in input fields", async () => {
    const { queryByLabelText, getByDisplayValue, user } = renderComponent({
      modelValue: { fields: { parent: "foo", child: "bar" } },
    })

    const input1 = queryByLabelText("text input 1 label") as HTMLInputElement
    const input2 = queryByLabelText("text input 2 label") as HTMLInputElement

    await user.type(input1, " bar")
    expect(input1).toHaveValue("foo bar")

    await user.type(input2, " foo")
    expect(input2).toHaveValue("bar foo")

    expect(getByDisplayValue("foo bar")).toBeInTheDocument()
    expect(getByDisplayValue("bar foo")).toBeInTheDocument()
  })

  it("renders input with dynamic types", async () => {
    const { queryByLabelText } = renderComponent({
      fields: {
        parent: defineTextField(
          "text input 1",
          "text input 1",
          "text input label",
          false
        ),
        child: defineDateField(
          "decisionDate",
          "Entscheidungsdatum",
          "date input label",
          true,
          undefined
        ),
      },
    })

    const input1 = queryByLabelText("text input label") as HTMLInputElement
    const input2 = queryByLabelText("date input label") as HTMLInputElement

    expect(input1).toHaveAttribute("type", "text")
    expect(input2).toHaveAttribute("type", "date")
  })
})
