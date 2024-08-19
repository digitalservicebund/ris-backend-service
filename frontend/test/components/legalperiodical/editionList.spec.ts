import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { ComboboxItem } from "@/components/input/types"
import EditionList from "@/components/legalperiodical/EditionList.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import comboboxItemService from "@/services/comboboxItemService"
import legalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

function renderComponent() {
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw/documentUnit/new",
        name: "new",
        component: {},
      },
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/preview",
        name: "caselaw-documentUnit-documentNumber-preview",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {},
      },
      {
        path: "/caselaw/legal-periodical-editions/:uuid",
        name: "caselaw-legal-periodical-editions-uuid",
        component: {},
      },
    ],
  })
  return {
    user,
    ...render(EditionList, {
      global: { plugins: [router] },
    }),
  }
}

describe("Legal periodical edition list", () => {
  beforeEach(() => {
    const legalPeriodical: LegalPeriodical = {
      abbreviation: "BDZ",
    }
    const dropdownLegalPeriodicalItems: ComboboxItem[] = [
      {
        label: legalPeriodical.abbreviation!,
        value: legalPeriodical,
      },
    ]
    vi.spyOn(comboboxItemService, "getLegalPeriodicals").mockImplementation(
      () =>
        Promise.resolve({ status: 200, data: dropdownLegalPeriodicalItems }),
    )
    const dropdownLegalPeriodicalEditions: LegalPeriodicalEdition[] = [
      new LegalPeriodicalEdition({
        uuid: "1",
        legalPeriodical: dropdownLegalPeriodicalItems[0]
          .value as LegalPeriodical,
        name: "2024, Heft 1",
        references: [],
        prefix: "2024",
        suffix: "Heft 1",
      }),
    ]
    vi.spyOn(
      legalPeriodicalEditionService,
      "getAllByLegalPeriodicalId",
    ).mockImplementation(() =>
      Promise.resolve({ status: 200, data: dropdownLegalPeriodicalEditions }),
    )
  })

  test("renders correctly", async () => {
    renderComponent()

    expect(screen.getByText("Periodika")).toBeVisible()
    expect(screen.getByText("Neue Periodikaauswertung")).toBeVisible()
  })

  test.skip("selecting legal periodical from combobox updates filter", async () => {
    const { user } = renderComponent()
    const periodicalField = screen.getByLabelText("Periodikum")

    await user.type(periodicalField, "BDZ")
    const dropdownItems = screen.getAllByLabelText(
      "dropdown-option",
    ) as HTMLElement[]
    expect(dropdownItems[0]).toHaveTextContent("BDZ")
    await user.click(dropdownItems[0])
    expect(periodicalField).toHaveValue("BDZ")
    expect(
      screen.getByText("Ausgabe 2024, Heft 1 (0 Fundstellen)"),
    ).toBeVisible()
    expect(screen.getByText("bearbeiten")).toBeVisible()
  })
})
