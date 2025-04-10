import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import { nextTick } from "vue"
import { ValidationError } from "@/components/input/types"
import YearInput from "@/components/input/YearInput.vue"

function renderComponent(options?: {
  id?: string
  ariaLabel?: string
  modelValue?: string
  hasError?: boolean
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    hasError: false,
  }
  const utils = render(YearInput, { props })
  return { user, props, ...utils }
}

describe("YearInput", () => {
  beforeEach(() => {
    // InputMask evaluates cursor position on every keystroke, however, our browser vitest setup does not
    // implement any layout-related functionality, meaning the required functions for cursor offset
    // calculation are missing. When we deal with typing in date/ year / time inputs, we can mock it with
    // TextInput, as we only need the string and do not need to test the actual mask behaviour.
    config.global.stubs = {
      InputMask: InputText,
    }
  })

  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
  })

  it("renders year input", () => {
    renderComponent({ modelValue: "2022" })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toHaveValue("2022")
  })

  it("emits model update event when input completed and valid", async () => {
    const { emitted } = renderComponent({
      modelValue: "2022",
    })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    expect(input).toHaveValue("2022")
    await userEvent.clear(input)
    await userEvent.type(input, "2023")
    await nextTick()

    expect(emitted()["update:modelValue"]).toEqual([[undefined], ["2023"]])
  })

  it("removes validation errors on backspace delete", async () => {
    const { emitted } = renderComponent({
      modelValue: "2022",
    })
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    await userEvent.type(input, "{backspace}")

    expect(emitted()["update:validationError"]).toBeTruthy()
    expect(emitted()["update:validationError"]).toEqual([
      [undefined],
      [undefined],
    ])
  })

  it("does not allow invalid year", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    await userEvent.type(input, "0000")
    await nextTick()

    expect(input).toHaveValue("0000")

    expect(emitted()["update:modelValue"]).not.toBeTruthy()

    expect(emitted()["update:validationError"]).toBeTruthy()

    const array = emitted()["update:validationError"] as ValidationError[][]

    expect(
      array.filter((element) => element[0] !== undefined)[0][0].message,
    ).toBe("Kein valides Jahr")
  })

  it("does not allow incomplete year", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement

    await userEvent.type(input, "03")
    await userEvent.type(input, "{tab}")
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
    const emittedEvents = emitted()["update:validationError"]
    expect(emittedEvents[emittedEvents.length - 1]).toEqual([
      {
        message: "Unvollständiges Jahr",
        instance: "identifier",
      },
    ])
  })

  it("tabbing through input does not trigger incomplete year validation error", async () => {
    const { emitted } = renderComponent()
    const input = screen.queryByLabelText("aria-label") as HTMLInputElement
    await userEvent.type(input, "{tab}")
    await nextTick()

    expect(emitted()["update:modelValue"]).not.toBeTruthy()
    expect(emitted()["update:validationError"]).toEqual([[undefined]])
  })
})
