import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { ComboboxItem } from "@/components/input/types"
import NewEditionEvaluation from "@/components/legalperiodical/NewEditionEvaluation.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import comboboxItemService from "@/services/comboboxItemService"
import service from "@/services/legalPeriodicalEditionService"
import routes from "~/test-helper/routes"

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(NewEditionEvaluation, {
      global: { plugins: [router] },
    }),
  }
}

describe("Legal periodical edition list", () => {
  const legalPeriodical: LegalPeriodical = {
    abbreviation: "BDZ",
  }
  const dropdownLegalPeriodicalItems: ComboboxItem[] = [
    {
      label: legalPeriodical.abbreviation!,
      value: legalPeriodical,
    },
  ]
  vi.spyOn(comboboxItemService, "getLegalPeriodicals").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownLegalPeriodicalItems }),
  )
  test("renders correctly", async () => {
    renderComponent()

    expect(screen.getByText("Neue Periodikaauswertung")).toBeVisible()

    expect(screen.getByLabelText("Periodikum")).toBeVisible()
    expect(screen.getByLabelText("Präfix")).toBeVisible()
    expect(screen.getByLabelText("Suffix")).toBeVisible()
    expect(screen.getByLabelText("Name der Ausgabe")).toBeVisible()
    expect(screen.getByText("Auswertung starten")).toBeVisible()
  })

  test("selecting legal periodical from combobox value for legal periodical", async () => {
    const { user } = renderComponent()
    const periodicalField = screen.getByLabelText("Periodikum")

    await user.type(periodicalField, "BDZ")
    const dropdownItems = screen.getAllByLabelText(
      "dropdown-option",
    ) as HTMLElement[]
    expect(dropdownItems[0]).toHaveTextContent("BDZ")
    await user.click(dropdownItems[0])
    await expect(periodicalField).toHaveValue("BDZ")
  })

  test("clicking Auswertung starten button calls service with correct values", async () => {
    const legalPeriodical: LegalPeriodical = {
      abbreviation: "BDZ",
    }
    const fetchSpy = vi.spyOn(service, "save").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new LegalPeriodicalEdition({
          id: crypto.randomUUID(),
          legalPeriodical: legalPeriodical,
          name: "name",
          prefix: "präfix",
          suffix: "suffix",
          references: [],
        }),
      }),
    )
    const { user } = renderComponent()
    const periodicalField = screen.getByLabelText("Periodikum")

    await user.type(periodicalField, "BDZ")
    const dropdownItems = screen.getAllByLabelText(
      "dropdown-option",
    ) as HTMLElement[]
    expect(dropdownItems[0]).toHaveTextContent("BDZ")
    await user.click(dropdownItems[0])
    await expect(periodicalField).toHaveValue("BDZ")
    await user.type(screen.getByLabelText("Präfix"), "präfix")
    await user.type(screen.getByLabelText("Suffix"), "suffix")
    await user.type(screen.getByLabelText("Name der Ausgabe"), "name")

    await user.click(screen.getByText("Auswertung starten"))
    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith({
      uuid: undefined,
      legalPeriodical: {
        abbreviation: "BDZ",
      },
      name: "name",
      prefix: "präfix",
      references: undefined,
      suffix: "suffix",
    })
  })

  describe("Legal periodical validation", () => {
    test("don't call save if empty field", async () => {
      const fetchSpy = vi.spyOn(service, "save").mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: new LegalPeriodicalEdition({
            id: crypto.randomUUID(),
            legalPeriodical: legalPeriodical,
            name: "name",
            prefix: "präfix",
            suffix: "suffix",
            references: [],
          }),
        }),
      )
      const { user } = renderComponent()

      await user.click(screen.getByLabelText("Auswertung starten"))

      expect(
        screen.getAllByText("Name oder Präfix sind nicht befüllt").length,
        "validation message was not shown for both name and prefix",
      ).toBe(2)

      expect(
        screen.getByText("Pflichtfeld nicht befüllt"),
        "should be shown if legal periodical empty",
      ).toBeVisible()
      expect(fetchSpy).toHaveBeenCalledTimes(0)
    })

    test("save if legal periodical and (name / präfix) are not null", async () => {
      const fetchSpy = vi.spyOn(service, "save").mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: new LegalPeriodicalEdition({
            id: crypto.randomUUID(),
            legalPeriodical: legalPeriodical,
            name: "name",
            prefix: "präfix",
            suffix: "suffix",
            references: [],
          }),
        }),
      )

      const { user } = renderComponent()
      const periodicalField = screen.getByLabelText("Periodikum")

      await user.type(periodicalField, "BDZ")
      const dropdownItems = screen.getAllByLabelText(
        "dropdown-option",
      ) as HTMLElement[]
      expect(dropdownItems[0]).toHaveTextContent("BDZ")
      await user.click(dropdownItems[0])
      await expect(periodicalField).toHaveValue("BDZ")

      await user.type(screen.getByLabelText("Präfix"), "präfix")

      await user.click(screen.getByLabelText("Auswertung starten"))

      expect(fetchSpy).toHaveBeenCalledTimes(1)
    })
  })
})
