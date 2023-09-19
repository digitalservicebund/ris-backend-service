import { userEvent } from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { MetadatumType } from "@/domain/norm"

//  ensures that the dateOrYearInputGroupProps type will always reflect the actual props of the DateOrYearInputGroup component. If you change the props of the component, this type will automatically update to match. This can help catch type errors early on and make the code more maintainable.
type DateOrYearInputGroupProps = InstanceType<
  typeof DateOrYearInputGroup
>["$props"]

function renderComponent(props?: Partial<DateOrYearInputGroupProps>) {
  const effectiveProps: DateOrYearInputGroupProps = {
    dateValue: props?.dateValue ?? undefined,
    yearValue: props?.yearValue ?? undefined,
    "onUpdate:selectedInputType": props?.["onUpdate:selectedInputType"],
    "onUpdate:dateValue": props?.["onUpdate:dateValue"],
    "onUpdate:yearValue": props?.["onUpdate:yearValue"],
    idPrefix: props?.idPrefix ?? "test-id-prefix",
    label: props?.label ?? "test-label",
    selectedInputType: props?.selectedInputType ?? MetadatumType.DATE,
  }
  return render(DateOrYearInputGroup, { props: effectiveProps })
}

async function changeToYearInput() {
  const yearRadioButton = screen.getByLabelText("Jahresangabe")
  expect(yearRadioButton).toBeInTheDocument()
  expect(yearRadioButton).toBeVisible()
  expect(yearRadioButton).not.toBeChecked()

  await fireEvent.click(yearRadioButton)

  expect(yearRadioButton).toBeChecked()
}

async function changeToDateInput() {
  const dateRadioButton = screen.getByLabelText("Datum")
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

  describe("default date component", () => {
    it("shows 2 radio buttons with 1 selected by default and corresponding field displayed", () => {
      renderComponent({ label: "test-label" })
      const dateRadioButton = screen.getByLabelText("Datum")
      const yearRadioButton = screen.getByLabelText("Jahresangabe")
      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")

      expect(dateRadioButton).toBeInTheDocument()
      expect(dateRadioButton).toBeVisible()
      expect(dateRadioButton).toBeChecked()

      expect(yearRadioButton).toBeInTheDocument()
      expect(yearRadioButton).toBeVisible()
      expect(yearRadioButton).not.toBeChecked()

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()
    })

    it("user can enter a date input", async () => {
      const user = userEvent.setup()
      let dateValue: string | undefined = undefined
      const onUpdateDateValue = vi.fn().mockImplementation((value) => {
        dateValue = value
      })

      const { rerender } = renderComponent({
        dateValue,
        "onUpdate:dateValue": onUpdateDateValue,
      })

      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")

      expect(dateInputField).toBeInTheDocument()
      expect(dateInputField).toBeVisible()

      await user.type(dateInputField, "12.05.2020")
      await rerender({ dateValue })
      expect(dateInputField).toHaveValue("12.05.2020")
      expect(dateValue).toBe("2020-05-12T00:00:00.000Z")
    })

    it("user can delete the date input", async () => {
      const user = userEvent.setup()
      let dateValue: string | undefined = "2020-05-12"
      const onUpdateDateValue = vi.fn().mockImplementation((value) => {
        dateValue = value
      })

      const { rerender } = renderComponent({
        dateValue,
        "onUpdate:dateValue": onUpdateDateValue,
      })

      const dateInputField = screen.getByPlaceholderText("TT.MM.JJJJ")

      expect(dateInputField).toHaveValue("12.05.2020")
      await user.clear(dateInputField)
      await rerender({ dateValue })
      expect(dateInputField).toHaveValue("")
      expect(dateValue).toBeUndefined()
    })
  })

  describe("year input component", () => {
    it("user clicks year radio button and renders year input element", async () => {
      let selectedInputType = MetadatumType.DATE
      const onUpdateSelectedInputType = vi.fn().mockImplementation((value) => {
        selectedInputType = value
      })

      const { rerender } = renderComponent({
        selectedInputType,
        "onUpdate:selectedInputType": onUpdateSelectedInputType,
      })

      await changeToYearInput()
      expect(onUpdateSelectedInputType).toHaveBeenCalledWith(MetadatumType.YEAR)
      await rerender({ selectedInputType })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      expect(yearInputField).toBeInTheDocument()
      expect(yearInputField).toBeVisible()
    })

    it("user can enter only digits in the year input field", async () => {
      let yearValue = ""
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.type(yearInputField, "abcd")
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("")
    })

    it("user can enter only 4 digits in the year input field", async () => {
      let yearValue = ""
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.type(yearInputField, "12345")
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("1234")
    })

    it("user can clear the year input", async () => {
      let yearValue = "2003"
      const onUpdateYearValue = vi.fn().mockImplementation((value) => {
        yearValue = value
      })

      const { rerender } = renderComponent({
        selectedInputType: MetadatumType.YEAR,
        yearValue,
        "onUpdate:yearValue": onUpdateYearValue,
      })

      const yearInputField = screen.getByPlaceholderText("JJJJ")
      await userEvent.clear(yearInputField)
      await rerender({ yearValue })

      expect(yearInputField).toHaveValue("")
      expect(yearValue).toBeUndefined()
    })
  })

  describe("behaviour when switching between date and year components", () => {
    it("date value is deleted after year value is entered", async () => {
      renderComponent()

      const dateInputField = screen.getByLabelText("test-label")

      await userEvent.type(dateInputField, "05.12.2020")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("05.12.2020")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText("test-label")

      await userEvent.type(yearInputFieldNew, "1989")
      expect(yearInputFieldNew.value).toBe("1989")

      await changeToDateInput()

      const dateInputFieldNew = screen.getByLabelText("test-label")

      expect(dateInputFieldNew).not.toHaveValue()
    })

    it("year value is deleted after date value is entered", async () => {
      renderComponent()
      await changeToYearInput()
      const yearInputField = screen.getByLabelText("test-label")

      await userEvent.type(yearInputField, "1989")
      expect(yearInputField.value).toBe("1989")

      await changeToDateInput()

      const dateInputField = screen.getByLabelText("test-label")

      await userEvent.type(dateInputField, "05.12.2020")
      await userEvent.tab()

      expect(dateInputField).toHaveValue("05.12.2020")

      await changeToYearInput()

      const yearInputFieldNew = screen.getByLabelText("test-label")

      expect(yearInputFieldNew).not.toHaveValue()
    })

    it("doesn't automatically switch back to date when the year is cleared", async () => {
      renderComponent({
        yearValue: "2020",
      })
      const user = userEvent.setup()

      const yearInputField = screen.getByLabelText("test-label")

      await user.clear(yearInputField)

      expect(yearInputField).toBeVisible()
    })
  })
})
