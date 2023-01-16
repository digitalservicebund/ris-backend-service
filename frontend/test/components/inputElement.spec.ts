import { ValidationError } from "./../../src/domain/types"
import userEvent from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import InputElement from "@/components/InputElement.vue"
import { InputType } from "@/domain"
import type { ModelType } from "@/domain"
import { nextTick } from "vue"

function renderComponent(options?: {
  id?: string
  type?: InputType
  modelValue?: ModelType
  validationError?: ValidationError
}) {
  const props = {
    id: "test",
    type: options?.type,
    modelValue: options?.modelValue,
    validationError: options?.validationError,
    attributes: {
      id: "test-id",
      ariaLabel: "test-label",
    },
  }
  const utils = render(InputElement, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("InputElement", () => {
  it("renders per default a textbox element", () => {
    renderComponent({ type: undefined })

    const textbox = screen.queryByRole("textbox")

    expect(textbox).toBeInTheDocument()
  })

  it("renders a textbox when type property defines so", () => {
    renderComponent({ type: InputType.TEXT })

    const textbox = screen.queryByRole("textbox")

    expect(textbox).toBeInTheDocument()
  })

  it("displays model value into input element", () => {
    renderComponent({
      modelValue: "test value",
    })

    const input = screen.queryByDisplayValue("test value")

    expect(input).toBeInTheDocument()
  })

  it("emits update model value event when input is used", async () => {
    const { emitted, user } = renderComponent({
      type: InputType.TEXT,
    })
    const input = screen.getByRole("textbox")
    await user.type(input, "a")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([["a"]])
  })

  it("renders an error message if input is not valid", async () => {
    renderComponent({
      id: "Testfeld",
      type: InputType.DATE,
      modelValue: "2024-02-10",
      validationError: {
        defaultMessage:
          "Das Entscheidungsdatum darf nicht in der Zukunft liegen",
        field: "Testfeld",
      },
    })

    expect(
      screen.getByText(
        "Das Entscheidungsdatum darf nicht in der Zukunft liegen"
      )
    ).toBeInTheDocument()
  })
})
