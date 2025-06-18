import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { beforeEach } from "vitest"
import { ref } from "vue"
import DocumentUnitCoreData from "@/components/DocumentUnitCoreData.vue"
import {
  CoreData,
  DocumentUnit,
  Kind,
  SourceValue,
} from "@/domain/documentUnit"

interface RenderProps {
  initialModelValue?: CoreData
  kind?: Kind
}

function renderComponent(props: RenderProps = {}) {
  const model = ref<CoreData>(props.initialModelValue || {})

  const user = userEvent.setup()

  const effectiveProps = {
    modelValue: model.value,
    kind: props.kind || Kind.DOCUMENTION_UNIT,
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
    },
  })

  return { user, model, ...view }
}

describe("Core Data", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })

  test("renders correctly with given documentUnitId", async () => {
    const documentUnit = new DocumentUnit("1", {
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
    const documentUnit = new DocumentUnit("1", {
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

    const input = await screen.findByLabelText(
      "Abweichendes Entscheidungsdatum",
    )
    await user.type(input, "02.02.2022{enter}")

    expect(model.value.deviatingDecisionDates).toEqual(["2022-02-02"])
  })

  test("renders year of dispute", async () => {
    const documentUnit = new DocumentUnit("1", {
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
})
