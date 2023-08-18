import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { Metadata } from "@/domain/norm"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: Metadata
  idPrefix?: string
  label?: string
}) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
    modelValue: options?.modelValue ?? {},
    idPrefix: options?.ariaLabel ?? "test-prefix",
    label: options?.ariaLabel ?? "test-label",
  }
  const utils = render(DateOrYearInputGroup, { props })
  return { user, props, ...utils }
}

async function changeToYearInput() {
  const yearRadioButton = screen.getByLabelText(
    "Jahresangabe",
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

describe("date/year field", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
  describe("Default date component", () => {
    it("Shows 2 radio buttons with 1 selected by default and corresponding field displayed", () => {
      renderComponent()
      const dateRadioButton = screen.getByLabelText("Datum") as HTMLInputElement
      const yearRadioButton = screen.getByLabelText(
        "Jahresangabe",
      ) as HTMLInputElement
      const dateInputField = screen.getByLabelText(
        "test-label",
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
        "test-label",
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "12.05.2020")

      expect(dateInputField).toHaveValue("12.05.2020")
    })

    it("User can delete the date input", async () => {
      const user = userEvent.setup()
      const modelValue: Metadata = { DATE: ["2020-05-12"] }
      renderComponent({ modelValue })

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(dateInputField).toHaveValue("12.05.2020")
      await user.type(dateInputField, "{backspace}")
      expect(dateInputField).toHaveValue("12.05.202")
    })
  })

  describe("Year input component", () => {
    it("user clicks Year radio button and renders year input element", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()
    })

    it("user can enter only digits in the year input field", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "test-label",
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
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "12345")
      expect(yearInputField.value).toBe("1234")
      expect(yearInputField.value.length).toBe(4)
    })

    it("user can clear the date input", async () => {
      const user = userEvent.setup()
      const modelValue: Metadata = { YEAR: ["2023"] }
      renderComponent({ modelValue })

      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toHaveValue("2023")
      await user.clear(yearInputField)
      expect(modelValue.YEAR).toEqual([])
    })
  })

  describe("Behaviour when switching between date and year components", () => {
    it("Date value is deleted after year value is entered", async () => {
      renderComponent()

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "05.12.2020")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("05.12.2020")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      await userEvent.type(yearInputFieldNew, "1989")
      expect(yearInputFieldNew.value).toBe("1989")

      await changeToDateInput()

      const dateInputFieldNew = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(dateInputFieldNew).not.toHaveValue()
    })

    it("Year value is deleted after date value is entered", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "1989")
      expect(yearInputField.value).toBe("1989")

      await changeToDateInput()

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await userEvent.type(dateInputField, "05.12.2020")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("05.12.2020")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputFieldNew).not.toHaveValue()
    })

    it("doesn't automatically switch back to date when the year is cleared", async () => {
      renderComponent({ modelValue: { YEAR: ["2020"] } })

      const user = userEvent.setup()

      const announcementYearInputField = screen.getByRole("textbox", {
        name: "test-label Jahresangabe",
      }) as HTMLInputElement

      await user.clear(announcementYearInputField)

      expect(announcementYearInputField).toBeVisible()
    })
  })
})
