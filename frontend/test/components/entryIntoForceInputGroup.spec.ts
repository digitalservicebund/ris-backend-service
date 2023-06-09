import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import EntryIntoForceInputGroup from "@/components/EntryIntoForceInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: Metadata
}) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(EntryIntoForceInputGroup, { props })
  return { user, props, ...utils }
}

async function changeToDateStateInput() {
  const undefinedRadioButton = screen.getByLabelText(
    "unbestimmt"
  ) as HTMLInputElement
  expect(undefinedRadioButton).toBeInTheDocument()
  expect(undefinedRadioButton).toBeVisible()
  expect(undefinedRadioButton).not.toBeChecked()

  await fireEvent.click(undefinedRadioButton)

  expect(undefinedRadioButton).toBeChecked()
}

describe("EntryIntoForceInputGroup", () => {
  describe("Default entry into force date component", () => {
    it("Shows 2 radio buttons with 1 selected by default and corresponding field displayed", () => {
      renderComponent()
      const definedRadioButton = screen.getByLabelText(
        "bestimmt"
      ) as HTMLInputElement
      const undefinedRadioButton = screen.getByLabelText(
        "unbestimmt"
      ) as HTMLInputElement
      const dateInputField = screen.getByLabelText(
        "Bestimmtes Inkrafttretedatum Date Input"
      ) as HTMLInputElement

      expect(definedRadioButton).toBeInTheDocument()
      expect(definedRadioButton).toBeVisible()
      expect(definedRadioButton).toBeChecked()

      expect(undefinedRadioButton).toBeInTheDocument()
      expect(undefinedRadioButton).toBeVisible()
      expect(undefinedRadioButton).not.toBeChecked()

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()
    })

    it("User can enter a date input", async () => {
      renderComponent()
      const dateInputField = screen.getByLabelText(
        "Bestimmtes Inkrafttretedatum Date Input"
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "12.05.2020")

      expect(dateInputField).toHaveValue("12.05.2020")
    })

    it("User can clear the date input", async () => {
      const user = userEvent.setup()
      const modelValue: Metadata = { DATE: ["2020-05-12"] }
      renderComponent({ modelValue })

      const dateInputField = screen.getByLabelText(
        "Bestimmtes Inkrafttretedatum Date Input"
      ) as HTMLInputElement

      expect(dateInputField).toHaveValue("12.05.2020")
      await user.type(dateInputField, "{backspace}")
      expect(modelValue.DATE).toBeUndefined()
    })
  })

  describe("Entry Into Force DateState component", () => {
    it("user clicks unbestimmt radio button and renders Entry Into Force DateState element", async () => {
      renderComponent()
      await changeToDateStateInput()
      const dropdown = screen.getByLabelText(
        "Unbestimmtes abweichendes Inkrafttretedatum Dropdown"
      ) as HTMLInputElement

      expect(dropdown).toBeInTheDocument()
      expect(dropdown).toBeVisible()
    })
  })
})
