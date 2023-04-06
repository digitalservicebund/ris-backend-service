import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import CitationDateInput from "@/components/CitationDateInput.vue"

function renderComponent(options?: { ariaLabel?: string }) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
  }
  const utils = render(CitationDateInput, { props })
  return { user, props, ...utils }
}

async function changeToYearInput() {
  const yearRadioButton = screen.getByLabelText(
    "Jahresangabe"
  ) as HTMLInputElement
  expect(yearRadioButton).toBeInTheDocument()
  expect(yearRadioButton).toBeVisible()
  expect(yearRadioButton).not.toBeChecked()

  await fireEvent.click(yearRadioButton)

  expect(yearRadioButton).toBeChecked()
}

describe("Citation date/year field", () => {
  describe("Default date component", () => {
    it("Shows 2 radio buttons with 1 selected by default and corresponding field displayed", () => {
      renderComponent()
      const dateRadioButton = screen.getByLabelText("Datum") as HTMLInputElement
      const yearRadioButton = screen.getByLabelText(
        "Jahresangabe"
      ) as HTMLInputElement
      const dateInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(dateRadioButton).toBeInTheDocument()
      expect(dateRadioButton).toBeVisible()
      expect(dateRadioButton).toBeChecked()

      expect(yearRadioButton).toBeInTheDocument()
      expect(yearRadioButton).toBeVisible()
      expect(yearRadioButton).not.toBeChecked()

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()
    })

    it("User can enter a date input", async () => {
      renderComponent()
      const dateInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()
      expect(dateInputField?.type).toBe("date")

      await userEvent.type(dateInputField, "2020-05-12")

      expect(dateInputField).toHaveValue("2020-05-12")
    })
  })

  describe("Year input component", () => {
    it("user clicks Year radio button and renders year input element", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()
    })

    it("user can enter only digits in the year input field", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "abcd")

      expect(yearInputField.value).toBe("")
      expect(yearInputField.value.length).toBe(0)
    })

    it("user can enter only 4 digits in the year input field", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "12345")
      expect(yearInputField.value).toBe("1234")
      expect(yearInputField.value.length).toBe(4)
    })
  })
})
