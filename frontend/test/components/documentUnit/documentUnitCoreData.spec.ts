import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { InputText } from "primevue"
import { beforeEach, expect } from "vitest"
import { ref } from "vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import { CoreData } from "@/domain/coreData"
import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import { SourceValue } from "@/domain/source"

interface RenderProps {
  initialModelValue?: CoreData
  kind?: Kind
}

function renderComponent(props: RenderProps = {}) {
  const model = ref<CoreData>(props.initialModelValue || {})

  const user = userEvent.setup()

  const effectiveProps = {
    modelValue: model.value,
    kind: props.kind || Kind.DECISION,
  }

  const view = render(DocumentUnitCoreData, {
    props: effectiveProps,
    global: {
      provide: {
        modelValue: model.value,
        "onUpdate:modelValue": (val: CoreData) => {
          model.value = val
        },
      },
      stubs: {
        InputMask: InputText,
      },
    },
  })

  return { user, model, ...view }
}

describe("Core Data", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })

  test("renders correctly with given documentUnitId", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        fileNumbers: ["one", "two"],
        ecli: "abc123",
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    expect(screen.getByLabelText("ECLI")).toHaveValue("abc123")
  })

  test("renders deviating decision date", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        deviatingDecisionDates: ["2021-02-01", "2022-02-01"],
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })
    expect(await screen.findByTestId("deviating-decision-dates")).toBeVisible()

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("01.02.2021")
    expect(chipList[1]).toHaveTextContent("01.02.2022")
  })

  test("updates deviating decision date", async () => {
    const { user, model } = renderComponent({
      initialModelValue: { deviatingDecisionDates: [] },
    })

    await screen.findByLabelText("Abweichendes Entscheidungsdatum anzeigen")
    await user.click(
      screen.getByLabelText("Abweichendes Entscheidungsdatum anzeigen"),
    )

    const input = await screen.findByTestId("deviating-decision-dates")
    await user.type(input, "02.02.2022{enter}")

    expect(model.value.deviatingDecisionDates).toEqual(["2022-02-02"])
  })

  test("makes oral hearing date required when hasDeliveryDate is checked", async () => {
    const documentUnit = new Decision("1", {
      documentNumber: "ABCD2022000001",
    })

    const { user } = renderComponent({
      initialModelValue: documentUnit.coreData,
    })
    expect(
      await screen.findByText("Datum der mündlichen Verhandlung", {
        exact: true,
      }),
    ).toBeVisible()

    await user.click(
      await screen.findByLabelText("Zustellung an Verkündungs statt"),
    )

    expect(
      await screen.findByText("Datum der mündlichen Verhandlung *", {
        exact: true,
      }),
    ).toBeVisible()
  })

  test("renders oral hearing date", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        oralHearingDates: ["2022-02-01", "2023-03-01"],
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })
    expect(await screen.findByTestId("oral-hearing-dates")).toBeVisible()

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("01.02.2022")
    expect(chipList[1]).toHaveTextContent("01.03.2023")
  })

  test("updates oral hearing date", async () => {
    const { user, model } = renderComponent({
      initialModelValue: { oralHearingDates: [] },
    })

    const input = await screen.findByTestId("oral-hearing-dates")
    await user.type(input, "02.02.2022{enter}")

    expect(model.value.oralHearingDates).toEqual(["2022-02-02"])
  })

  test("renders year of dispute", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        yearsOfDispute: ["2021", "2022"],
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })
    await screen.findByText("Streitjahr")
    expect(screen.getByTestId("year-of-dispute")).toBeVisible()

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("2021")
    expect(chipList[1]).toHaveTextContent("2022")
  })

  test("updates year of dispute", async () => {
    const { user, model } = renderComponent({
      initialModelValue: { yearsOfDispute: [] },
    })

    await screen.findByText("Streitjahr")
    const input = screen.getByLabelText("Streitjahr")
    await user.type(input, "2023{enter}")

    expect(model.value.yearsOfDispute).toEqual(["2023"])
  })

  test("renders jurisdiction type", async () => {
    const coreData = {
      court: {
        label: "BGH",
        jurisdictionType: "Ordentliche Gerichtsbarkeit",
      },
    }

    renderComponent({ initialModelValue: coreData })
    expect(screen.getByTestId("jurisdiction-type")).toBeVisible()
    expect(screen.getByLabelText("Gerichtsbarkeit")).toHaveValue(
      "Ordentliche Gerichtsbarkeit",
    )
  })

  test("renders source", async () => {
    const coreData = {
      source: {
        value: SourceValue.AngefordertesOriginal,
      },
    }

    renderComponent({ initialModelValue: coreData })

    const sourceSelect = screen.getByLabelText("Quelle Input")
    expect(sourceSelect).toHaveTextContent(SourceValue.AngefordertesOriginal)
  })

  test("updates source", async () => {
    const { user, model } = renderComponent({
      initialModelValue: { source: undefined },
    })

    const dropdown = await screen.findByLabelText("Quelle Input")
    await user.click(dropdown)

    const options = await screen.findAllByRole("option")
    expect(options.length).toBe(6)
    expect(options[0]).toHaveTextContent(
      "unaufgefordert eingesandtes Original (O)",
    )

    await user.click(options[0])

    expect(model.value.source).toEqual({
      value: SourceValue.UnaufgefordertesOriginal,
    })
  })

  test("renders inputTypes", async () => {
    // Arrange
    const coreData = {
      inputTypes: ["Papier", "E-Mail", "EUR-LEX-Schnittstelle"],
    }

    // Act
    renderComponent({ initialModelValue: coreData })

    // Assert
    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(3)
    expect(chipList[0]).toHaveTextContent("Papier")
    expect(chipList[1]).toHaveTextContent("E-Mail")
    expect(chipList[2]).toHaveTextContent("EUR-LEX-Schnittstelle")
  })

  test("updates inputTypes", async () => {
    // Arrange
    const { user, model } = renderComponent({
      initialModelValue: { inputTypes: [] },
    })
    expect(model.value.inputTypes?.length).toBe(0)
    const input = screen.getByTestId("input-types")
    await user.click(input)

    // Act
    await user.type(input, "Papier{enter}E-Mail{enter}")

    // Assert
    const resultList = screen.getAllByRole("listitem")
    expect(resultList.length).toBe(2)
    expect(resultList[0]).toHaveTextContent("Papier")
    expect(resultList[1]).toHaveTextContent("E-Mail")
  })

  test("renders deviating document number", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        deviatingDocumentNumbers: ["XXRE123456789", "XXRE111111111"],
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })
    expect(
      await screen.findByTestId("deviating-document-numbers"),
    ).toBeVisible()

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("XXRE123456789")
    expect(chipList[1]).toHaveTextContent("XXRE111111111")
  })

  test("updates deviating document number", async () => {
    const { user, model } = renderComponent({
      initialModelValue: { deviatingDocumentNumbers: [] },
    })
    const input = await screen.findByTestId("deviating-document-numbers")
    await user.type(input, "XXRE123456789{enter}")

    expect(model.value.deviatingDocumentNumbers).toEqual(["XXRE123456789"])
  })

  test.each(["EuG", "EuGH"] as const)(
    "renders celex number editable with court %s",
    async (courtLabel) => {
      const documentUnit = new Decision("1", {
        coreData: {
          court: {
            label: courtLabel,
          },
          celexNumber: "62023CJ0538",
        },
        documentNumber: "ABCD2022000001",
      })

      renderComponent({ initialModelValue: documentUnit.coreData })
      const inputField = await screen.findByLabelText("CELEX-Nummer")
      expect(inputField).toHaveValue("62023CJ0538")
      expect(inputField).not.toHaveAttribute("readonly")
    },
  )

  test("renders celex number readonly", async () => {
    const documentUnit = new Decision("1", {
      coreData: {
        court: {
          label: "AG Aachen",
        },
        celexNumber: "62023CJ0538",
      },
      documentNumber: "ABCD2022000001",
    })

    renderComponent({ initialModelValue: documentUnit.coreData })
    const inputField = await screen.findByLabelText("CELEX-Nummer")
    expect(inputField).toHaveValue("62023CJ0538")
    expect(inputField).toHaveAttribute("readonly")
  })

  test("should not show decision-specific date attributes for pending proceeding", async () => {
    renderComponent({ kind: Kind.PENDING_PROCEEDING })
    expect(
      screen.queryByLabelText("Datum der mündlichen Verhandlung"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Zustellung an Verkündungs statt"),
    ).not.toBeInTheDocument()
  })
})
