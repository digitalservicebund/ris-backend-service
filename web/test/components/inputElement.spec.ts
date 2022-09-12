import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import InputElement from "@/components/InputElement.vue"
import { InputType } from "@/domain"
import type { ModelType } from "@/domain"

function renderComponent(options?: {
  type?: InputType
  modelValue?: ModelType
}) {
  const props = {
    type: options?.type,
    modelValue: options?.modelValue,
    attributes: {
      id: "test-id",
      ariaLabel: "test-label",
    },
  }
  const renderResult = render(InputElement, { props })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

describe("InputElement", () => {
  it("renders per default a textbox element", () => {
    const { queryByRole } = renderComponent({ type: undefined })

    const textbox = queryByRole("textbox")

    expect(textbox).toBeInTheDocument()
  })

  it("renders a textbox when type property defines so", () => {
    const { queryByRole } = renderComponent({ type: InputType.TEXT })

    const textbox = queryByRole("textbox")

    expect(textbox).toBeInTheDocument()
  })

  it("displays model value into input element", () => {
    const { queryByDisplayValue } = renderComponent({
      modelValue: "test value",
    })

    const input = queryByDisplayValue("test value")

    expect(input).toBeInTheDocument()
  })

  it("emits update model value event when input is used", async () => {
    const { emitted, user, getByRole } = renderComponent({
      type: InputType.TEXT,
    })
    const input = getByRole("textbox")

    await user.type(input, "a")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([["a"]])
  })
})
