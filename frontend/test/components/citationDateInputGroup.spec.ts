import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { Metadata } from "../../src/domain/Norm"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: Metadata
}) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(CitationDateInputGroup, { props })
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

async function changeToDateInput() {
  const dateRadioButton = screen.getByLabelText("Datum") as HTMLInputElement
  expect(dateRadioButton).toBeInTheDocument()
  expect(dateRadioButton).toBeVisible()
  expect(dateRadioButton).not.toBeChecked()

  await fireEvent.click(dateRadioButton)

  expect(dateRadioButton).toBeChecked()
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
        "Zitierdatum Datum"
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
        "Zitierdatum Datum"
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

  describe("Behaviour when switching between date and year components", () => {
    it("Date value is deleted after year value is entered", async () => {
      renderComponent()

      const dateInputField = screen.getByLabelText(
        "Zitierdatum Datum"
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "2020-05-12")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("2020-05-12")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      await userEvent.type(yearInputFieldNew, "1989")
      expect(yearInputFieldNew.value).toBe("1989")

      await changeToDateInput()

      const dateInputFieldNew = screen.getByLabelText(
        "Zitierdatum Datum"
      ) as HTMLInputElement

      expect(dateInputFieldNew).not.toHaveValue()
    })

    it("Year value is deleted after date value is entered", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "1989")
      expect(yearInputField.value).toBe("1989")

      await changeToDateInput()

      const dateInputField = screen.getByLabelText(
        "Zitierdatum Datum"
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "2020-05-12")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("2020-05-12")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText(
        "Zitierdatum"
      ) as HTMLInputElement

      expect(yearInputFieldNew).not.toHaveValue()
    })
  })
})
