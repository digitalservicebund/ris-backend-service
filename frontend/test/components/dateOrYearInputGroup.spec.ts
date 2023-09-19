import { userEvent } from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { Metadata , MetadatumType } from "@/domain/norm"

//  ensures that the dateOrYearInputGroupProps type will always reflect the actual props of the DateOrYearInputGroup component. If you change the props of the component, this type will automatically update to match. This can help catch type errors early on and make the code more maintainable.
type dateOrYearInputGroupProps = InstanceType<
    typeof DateOrYearInputGroup
>["$props"]

function renderComponent(props?: dateOrYearInputGroupProps) {
  const effectiveProps: dateOrYearInputGroupProps = {
    dateValue: props?.dateValue ?? undefined,
    yearValue: props?.yearValue ?? undefined,
    "onUpdate:selectedInputType": props
    "onUpdate:dateValue": props?.["onUpdate:dateValue"],
    "onUpdate:yearValue": props?.["onUpdate:yearValue"],
    idPrefix: props?.idPrefix ?? 'test-id-prefix',
    label: props?.label ?? 'test-label',
    selectedInputType: props?.selectedInputType ?? MetadatumType.DATE
  }
  return render(DateOrYearInputGroup, {props: effectiveProps})
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
      renderComponent({label: "test-label"})
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
      renderComponent({
        dateValue:"2020-05-12"
      })

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(dateInputField).toHaveValue("12.05.2020")
      await user.clear(dateInputField)
      expect(dateInputField).toHaveValue("")
    })
  })

  describe("Year input component", () => {
    it("user clicks Year radio button and renders year input element", async () => {
      const {emitted} = renderComponent()
      await changeToYearInput()
      expect(emitted("onUpdate:selectedInputType")).toHaveLength(1)
    })

    it("user can enter only digits in the year input field", async () => {
      let modelValue = {}

      const onUpdateModelValue = vi.fn().mockImplementation((value) => {
        modelValue = value
      })

      const { rerender } = renderComponent({
        modelValue,
        "onUpdate:modelValue": onUpdateModelValue,
      })

      await changeToYearInput()
      await rerender({modelValue})
      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "1923")
      await rerender({modelValue})

      expect(yearInputField.value).toBe("")
      expect(yearInputField.value.length).toBe(0)
    })

    it("user can enter only 4 digits in the year input field", async () => {
      let modelValue: Metadata = {YEAR:["2023"]}

      const onUpdateModelValue = vi.fn().mockImplementation((value) => {
        modelValue = value
      })

      const { rerender } = renderComponent({
        modelValue,
        "onUpdate:modelValue": onUpdateModelValue,
      })

      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()

      await userEvent.type(yearInputField, "20235")
      await rerender({modelValue})
      expect(yearInputField.value).toBe("2023")
      expect(yearInputField.value.length).toBe(4)
    })

    it("user can clear the year input", async () => {
      const user = userEvent.setup()
      renderComponent({
        yearValue:"2023"
      })

      const yearInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

      expect(yearInputField).toHaveValue("2023")
      await user.clear(yearInputField)
      expect(yearInputField).toHaveValue("")
      expect(props.yearValue).toEqual([""])
    })
  })

  describe("Behaviour when switching between date and year components", () => {
    it("Date value is deleted after year value is entered", async () => {
      renderComponent()

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

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

      await userEvent.type(yearInputField, "1989")
      expect(yearInputField.value).toBe("1989")

      await changeToDateInput()

      const dateInputField = screen.getByLabelText(
        "test-label",
      ) as HTMLInputElement

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
      renderComponent({
        yearValue:"2020"
      })
      const user = userEvent.setup()

      const yearInputField = screen.getByLabelText(
          "test-label",
      ) as HTMLInputElement

      await user.clear(yearInputField)

      expect(yearInputField).toBeVisible()
    })
  })
})
