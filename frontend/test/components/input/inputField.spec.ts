import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { MANDATORY_FIELD_MISSING } from "@/i18n/errors.json"

type InputFieldProps = InstanceType<typeof InputField>["$props"]

function renderComponent(
  props?: Partial<InputFieldProps>,
  slots?: Record<string, unknown>,
) {
  const id = props?.id ?? "identifier"

  const effectiveSlots = { default: `<input id="${id}" />`, ...slots }

  const effectiveProps: InputFieldProps = {
    id,
    label: props?.label ?? "",
    ...props,
  }

  return render(InputField, { props: effectiveProps, slots: effectiveSlots })
}

describe("InputField", () => {
  it("renders the component with a label", () => {
    renderComponent({ label: "test label" })
    const element = screen.getByLabelText("test label")
    expect(element).toBeInTheDocument()
  })

  it("does not render an empty label", () => {
    renderComponent({})
    expect(screen.queryByTestId("label-wrapper")).not.toBeInTheDocument()
  })

  it("visually hides the label", () => {
    renderComponent({ label: "test", visuallyHideLabel: true })
    const element = screen.getByTestId("label-wrapper")
    expect(element).toBeInTheDocument()
    expect(element).toHaveClass("sr-only")
  })

  it("renders a label with multiple lines", () => {
    renderComponent({ label: ["test label 1", "test label 2"] })
    const element = screen.getByLabelText("test label 1 test label 2")
    expect(element).toBeInTheDocument()
  })

  it("renders a label with multiple lines and marks as required", () => {
    renderComponent({
      label: ["test label 1", "test label 2"],
      required: true,
    })

    const element = screen.getByLabelText("test label 1 test label 2 *")
    expect(element).toBeInTheDocument()
  })

  it("renders the label at the top by default", () => {
    renderComponent({ label: "test" })
    const element = screen.getByTestId("label-wrapper")
    expect(element).not.toHaveClass("order", { exact: false })
  })

  it("sets the correct order when rendering the label at the top", () => {
    renderComponent({ label: "test" })
    const element = screen.getByTestId("label-wrapper")
    expect(element).not.toHaveClass("order", { exact: false })
  })

  it("sets the correct order when rendering the label to the right", () => {
    renderComponent({ label: "test", labelPosition: LabelPosition.RIGHT })
    const element = screen.getByTestId("label-wrapper")
    expect(element).toHaveClass("order-1")
  })

  it("marks the component as requied", () => {
    renderComponent({ label: "test label", required: true })
    const element = screen.getByLabelText("test label *")
    expect(element).toBeInTheDocument()
  })

  it("renders the error message from a prop", () => {
    renderComponent({
      validationError: { message: "error message", instance: "identifier" },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("removes the error message when the prop is cleared", async () => {
    const { rerender } = renderComponent({
      id: "identifier",
      validationError: { message: "from props", instance: "identifier" },
    })

    expect(screen.getByText("from props")).toBeInTheDocument()

    await rerender({ validationError: undefined })
    expect(screen.queryByText("from props")).not.toBeInTheDocument()
  })

  it("shows the error's own message if no code exists", () => {
    renderComponent({
      validationError: { message: "error message", instance: "identifier" },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("shows the error's own message if the code is not found", () => {
    renderComponent({
      validationError: {
        code: "NOT_FOUND",
        instance: "identifier",
        message: "error message",
      },
    })

    expect(screen.getByText("error message")).toBeInTheDocument()
  })

  it("maps the error code to a message if possible", () => {
    renderComponent({
      validationError: {
        code: "MANDATORY_FIELD_MISSING",
        instance: "identifier",
        message: "This should not be visible",
      },
    })

    expect(screen.getByText(MANDATORY_FIELD_MISSING.title)).toBeInTheDocument()
  })

  it("renders the slot content", () => {
    renderComponent(
      { id: "test-identifier", label: "test label" },
      {
        default: `
          <template v-slot="slotProps">
            <input type='radio' :id="slotProps.id" />
          </template>
        `,
      },
    )

    const element = screen.getByRole<HTMLInputElement>("radio")
    expect(element).toBeInTheDocument()
  })

  it("forwards the ID to the slot", () => {
    renderComponent(
      { id: "test-identifier", label: "test label" },
      {
        default: `
          <template v-slot="slotProps">
            ID is {{ slotProps.id }}
          </template>
        `,
      },
    )

    const element = screen.getByText("ID is test-identifier")
    expect(element).toBeInTheDocument()
  })

  it("tells the slot content that there is an error", () => {
    renderComponent(
      { validationError: { instance: "foo", message: "bar" } },
      {
        default: `
          <template v-slot="slotProps">
            Has error is {{ slotProps.hasError }}
          </template>
        `,
      },
    )

    const element = screen.getByText("Has error is true")
    expect(element).toBeInTheDocument()
  })

  it("doesn't tell the slot content that there is an error if there is none", () => {
    renderComponent(undefined, {
      default: `
          <template v-slot="slotProps">
            Has error is {{ slotProps.hasError }}
          </template>
        `,
    })

    const element = screen.getByText("Has error is false")
    expect(element).toBeInTheDocument()
  })

  it("sets the validation error based on the slot events", async () => {
    const user = userEvent.setup()
    renderComponent(undefined, {
      default: `
          <template v-slot="slotProps">
            <button @click="slotProps.updateValidationError({ instance: 'foo', message: 'bar' })">
              Create error
            </button>
          </template>
        `,
    })

    await user.click(screen.getByRole("button"))
    expect(screen.getByText("bar")).toBeInTheDocument()
  })

  it("removes the validation error based on the slot events", async () => {
    const user = userEvent.setup()
    renderComponent(
      { validationError: { instance: "foo", message: "bar" } },
      {
        default: `
          <template v-slot="slotProps">
            <button @click="slotProps.updateValidationError(undefined)">
              Remove error
            </button>
          </template>
        `,
      },
    )

    const element = screen.getByText("bar")
    expect(element).toBeInTheDocument()
    await user.click(screen.getByRole("button"))
    expect(element).not.toBeInTheDocument()
  })

  it("renders custom label classes", async () => {
    renderComponent({ label: "foo", labelClass: "ris-label1-regular" })

    const element = screen.getByText("foo")
    // eslint-disable-next-line testing-library/no-node-access
    expect(element.parentNode).toHaveClass("ris-label1-regular")
  })
})
