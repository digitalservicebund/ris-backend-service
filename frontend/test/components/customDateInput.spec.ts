import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { nextTick } from "vue"
import CustomDateInput from "@/components/CustomDateInput.vue"
import { ValidationError } from "@/domain"

function renderComponent(options?: {
  ariaLabel?: string
  isFutureDate?: boolean
  value?: string
  modelValue?: string
  placeholder?: string
  validationError?: ValidationError
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    isFutureDate: options?.isFutureDate ?? false,
    placeholder: options?.placeholder,
    validationError: options?.validationError,
  }
  const utils = render(CustomDateInput, { props })
  return { user, props, ...utils }
}

describe("CustomDateInput", () => {
  it("renders 3 input elements with aria label", () => {
    renderComponent({ ariaLabel: "Test" })
    const day = screen.queryByLabelText("Test Tag") as HTMLInputElement
    const month = screen.queryByLabelText("Test Monat") as HTMLInputElement
    const year = screen.queryByLabelText("Test Jahr") as HTMLInputElement

    expect(day).toBeInTheDocument()
    expect(month).toBeInTheDocument()
    expect(year).toBeInTheDocument()
  })

  it("renders an input with modelValue", async () => {
    renderComponent({ ariaLabel: "Test", modelValue: "2022-02-03" })
    const day = screen.queryByLabelText("Test Tag") as HTMLInputElement
    const month = screen.queryByLabelText("Test Monat") as HTMLInputElement
    const year = screen.queryByLabelText("Test Jahr") as HTMLInputElement

    expect(day).toHaveValue("03")
    expect(month).toHaveValue("02")
    expect(year).toHaveValue("2022")
  })

  it("allows to type date inside inputs", async () => {
    renderComponent({ ariaLabel: "Test", modelValue: "2022-02-03" })
    const day = screen.queryByLabelText("Test Tag") as HTMLInputElement
    const month = screen.queryByLabelText("Test Monat") as HTMLInputElement
    const year = screen.queryByLabelText("Test Jahr") as HTMLInputElement

    expect(day).toHaveValue("03")
    expect(month).toHaveValue("02")
    expect(year).toHaveValue("2022")

    await userEvent.clear(day)
    await userEvent.type(day, "04")
    await userEvent.tab()
    await userEvent.clear(month)
    await userEvent.type(month, "04")
    await userEvent.tab()
    await userEvent.clear(year)
    await userEvent.type(year, "2021")
    await userEvent.tab()

    expect(day).toHaveValue("04")
    expect(month).toHaveValue("04")
    expect(year).toHaveValue("2021")
  })

  it("emits model update event when input changes", async () => {
    const { emitted } = renderComponent({
      ariaLabel: "Test",
      modelValue: "2022-02-03",
    })
    const day = screen.queryByLabelText("Test Tag") as HTMLInputElement
    const month = screen.queryByLabelText("Test Monat") as HTMLInputElement
    const year = screen.queryByLabelText("Test Jahr") as HTMLInputElement

    await userEvent.clear(day)
    await userEvent.type(day, "04")
    await userEvent.clear(month)
    await userEvent.type(month, "04")
    await userEvent.clear(year)
    await userEvent.type(year, "2021")
    await userEvent.tab()

    await nextTick()

    expect(day).toHaveValue("04")
    expect(month).toHaveValue("04")
    expect(year).toHaveValue("2021")
    expect(emitted()["update:modelValue"]).toEqual([
      ["2021-04-04T00:00:00.000Z"],
    ])
  })

  it("emits undefined on backspace delete", async () => {
    const { emitted } = renderComponent({
      ariaLabel: "Test",
      modelValue: "2022-02-03",
    })
    const day = screen.queryByLabelText("Test Tag") as HTMLInputElement

    await userEvent.type(day, "{backspace}")
    await nextTick()

    expect(day).toHaveValue("")
    expect(emitted()["update:modelValue"]).toBeTruthy()
    expect(emitted().day).toEqual(undefined)
  })

  it("does not allow dates in the future", async () => {
    const { emitted } = renderComponent({
      ariaLabel: "Datum",
      modelValue: "2024-02-03",
    })

    expect(emitted()["update:modelValue"]).not.toBeTruthy()

    expect(emitted()["update:validationError"]).toBeTruthy()

    const array = emitted()["update:validationError"] as ValidationError[][]

    expect(
      array.filter((element) => element[0] !== undefined)[0][0].defaultMessage
    ).toBe("Das Datum darf nicht in der Zukunft liegen")
  })
})
