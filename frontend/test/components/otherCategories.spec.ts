import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import OtherCategories from "@/components/OtherCategories.vue"
import AbuseFee, { Addressee } from "@/domain/abuseFee"
import { AppealWithdrawal } from "@/domain/appeal"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { Decision } from "@/domain/decision"
import Definition from "@/domain/definition"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import ObjectValue, { ProceedingType } from "@/domain/objectValue"
import OriginOfTranslation, {
  TranslationType,
} from "@/domain/originOfTranslation"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

function mockSessionStore(
  contentRelatedIndexing: ContentRelatedIndexing,
  courtType: string = "",
  jurisdictionType: string = "",
) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new Decision("q834", {
    contentRelatedIndexing: contentRelatedIndexing,
    coreData: {
      court: {
        label: courtType,
        type: courtType,
        jurisdictionType,
      },
    },
  })

  return mockedSessionStore
}
describe("other categories", () => {
  beforeEach(() => {
    vi.resetModules()
    vi.resetAllMocks()
    setActivePinia(createTestingPinia())
  })
  describe("LegislativeMandate", () => {
    test("should not display legislative mandate when it is false and courtType is non-constitutional", async () => {
      // Arrange
      mockSessionStore({ hasLegislativeMandate: false }, "BAG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByText("Berufsbild")).toBeInTheDocument()
      expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
      expect(
        screen.queryByText("Gesetzgebungsauftrag vorhanden"),
      ).not.toBeInTheDocument()
    })

    test("should display legislative mandate button when it is false and courtType is constitutional", async () => {
      // Arrange
      mockSessionStore({ hasLegislativeMandate: false }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByText("Berufsbild")).toBeInTheDocument()
      expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
      expect(
        screen.queryByText("Gesetzgebungsauftrag vorhanden"),
      ).not.toBeInTheDocument()
    })

    test("should display checked legislative mandate when it is true and has constitutional courtType", async () => {
      // Arrange
      mockSessionStore({ hasLegislativeMandate: true }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByText("Berufsbild")).toBeInTheDocument()
      expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
      expect(
        screen.getByText("Gesetzgebungsauftrag vorhanden"),
      ).toBeInTheDocument()
    })

    test("should display checked legislative mandate when it is true and has non-constitutional courtType", async () => {
      // Arrange
      mockSessionStore({ hasLegislativeMandate: true }, "BAG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByText("Berufsbild")).toBeInTheDocument()
      expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
      expect(
        screen.getByText("Gesetzgebungsauftrag vorhanden"),
      ).toBeInTheDocument()
    })
  })

  describe("Dismissal Inputs", () => {
    test("should not display dismissal inputs/button when inputs are empty and courtType is non-labor", async () => {
      // Arrange
      mockSessionStore({ dismissalGrounds: [], dismissalTypes: [] }, "BGH")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.queryByText("Kündigung")).not.toBeInTheDocument()
      expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
      expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    })

    test("should display dismissal button when inputs are empty and courtType is labor", async () => {
      // Arrange
      mockSessionStore({ dismissalGrounds: [], dismissalTypes: [] }, "BAG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByText("Kündigung")).toBeInTheDocument()
      expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
      expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    })

    test("should display dismissal inputs when ground is non-empty and courtType is non-labor", async () => {
      // Arrange
      mockSessionStore(
        { dismissalGrounds: ["ground"], dismissalTypes: [] },
        "BGH",
      )

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.queryByText("Kündigung")).not.toBeInTheDocument()
      expect(screen.getByText("Kündigungsarten")).toBeInTheDocument()
      expect(screen.getByText("Kündigungsgründe")).toBeInTheDocument()
    })

    test("should display dismissal inputs when ground is non-empty and courtType is non-labor", async () => {
      // Arrange
      mockSessionStore(
        { dismissalGrounds: [], dismissalTypes: ["type"] },
        "BGH",
      )

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.queryByText("Kündigung")).not.toBeInTheDocument()
      expect(screen.getByText("Kündigungsarten")).toBeInTheDocument()
      expect(screen.getByText("Kündigungsgründe")).toBeInTheDocument()
    })

    test("should display dismissal inputs when inputs are non-empty and courtType is labor", async () => {
      // Arrange
      mockSessionStore(
        {
          dismissalGrounds: ["ground"],
          dismissalTypes: ["type"],
        },
        "BAG",
      )

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.queryByText("Kündigung")).not.toBeInTheDocument()
      expect(screen.getByText("Kündigungsarten")).toBeInTheDocument()
      expect(screen.getByText("Kündigungsgründe")).toBeInTheDocument()
    })
  })

  describe("CollectiveAgreements", () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    test("should not display collective agreements button when it is empty and not a labor court", async () => {
      // Arrange
      mockSessionStore({ collectiveAgreements: [] }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.queryByRole("button", { name: "Tarifvertrag" }),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("textbox", { name: "Tarifvertrag Input" }),
      ).not.toBeInTheDocument()
    })

    test("should display collective agreements button when it is empty and labor court", async () => {
      // Arrange
      mockSessionStore({ collectiveAgreements: [] }, "LArbG")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.getByRole("button", { name: "Tarifvertrag" }),
      ).toBeInTheDocument()
      expect(
        screen.queryByRole("textbox", { name: "Tarifvertrag Input" }),
      ).not.toBeInTheDocument()
    })

    test("should display collective agreements when it is not empty without labor court", async () => {
      // Arrange
      mockSessionStore(
        {
          collectiveAgreements: [
            new CollectiveAgreement({ name: "Stehende Bühnen", norm: "§ 23" }),
          ],
        },
        "BVerfG",
      )

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(screen.getByText("Stehende Bühnen, § 23")).toBeInTheDocument()

      expect(
        screen.queryByRole("button", { name: "Tarifvertrag" }),
      ).not.toBeInTheDocument()
    })
  })

  describe("E-VSF", () => {
    test("should not display evsf button when it is empty and not a financial court", async () => {
      // Arrange
      mockSessionStore({ evsf: undefined }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.queryByRole("button", { name: "E-VSF" }),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("textbox", { name: "E-VSF" }),
      ).not.toBeInTheDocument()
    })

    test("should display E-VSF button when it is empty and financial court", async () => {
      // Arrange
      mockSessionStore({ evsf: undefined }, "BFH", "Finanzgerichtsbarkeit")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByRole("button", { name: "E-VSF" })).toBeInTheDocument()
      expect(
        screen.queryByRole("textbox", { name: "E-VSF" }),
      ).not.toBeInTheDocument()
    })

    test("should display E-VSF when it is not empty without financial court", async () => {
      // Arrange
      mockSessionStore({ evsf: "X 00 00-0-0" }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(screen.getByRole("textbox", { name: "E-VSF" })).toHaveValue(
        "X 00 00-0-0",
      )

      expect(
        screen.queryByRole("button", { name: "E-VSF" }),
      ).not.toBeInTheDocument()
    })
  })
  describe("Definition", () => {
    test("should display button without existing definitions", async () => {
      // Arrange
      mockSessionStore({ definitions: [] }, "BGH")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.getByRole("button", { name: "Definition" }),
      ).toBeInTheDocument()
    })

    test("should display existing definitions", async () => {
      // Arrange
      mockSessionStore(
        { definitions: [new Definition({ definedTerm: "abc" })] },
        "BGH",
      )

      // Act
      const router = createRouter({
        history: createWebHistory(),
        routes: routes,
      })
      render(OtherCategories, { global: { plugins: [router] } })

      // Assert
      expect(screen.getByLabelText("Definitionen")).toHaveTextContent("abc")
    })
  })

  describe("Foreign Language Versions", () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    it("should display foreign language version button when no data", async () => {
      // Arrange
      mockSessionStore({
        foreignLanguageVersions: [],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(
        screen.getByRole("button", { name: "Fremdsprachige Fassung" }),
      ).toBeInTheDocument()
    })

    it("should display foreign language versions", async () => {
      // Arrange
      mockSessionStore({
        foreignLanguageVersions: [
          new ForeignLanguageVersion({
            id: "1",
            languageCode: {
              id: "3",
              label: "Englisch",
            },
            link: "http://link-to-translation.en",
          }),
          new ForeignLanguageVersion({
            id: "2",
            languageCode: {
              id: "4",
              label: "Französisch",
            },
            link: "https://link-to-translation.fr",
          }),
          new ForeignLanguageVersion({
            id: "3",
            languageCode: {
              id: "5",
              label: "Spanisch",
            },
            link: "link-to-translation.es",
          }),
        ],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(await screen.findByText("Fremdsprachige Fassung")).toBeVisible()

      const links = screen.getAllByRole("link")
      expect(links).toHaveLength(3)
      expect(links[0]).toHaveAttribute("href", "http://link-to-translation.en")
      expect(links[1]).toHaveAttribute("href", "https://link-to-translation.fr")
      expect(links[2]).toHaveAttribute("href", "https://link-to-translation.es")
    })
  })

  describe("Origin of Translation", () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    it("should display origin of translation button when no data", async () => {
      // Arrange
      mockSessionStore({
        originOfTranslations: [],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(
        screen.getByRole("button", { name: "Herkunft der Übersetzung" }),
      ).toBeInTheDocument()
    })

    it("should display origin of translation", async () => {
      // Arrange
      mockSessionStore({
        originOfTranslations: [
          new OriginOfTranslation({
            id: "1",
            languageCode: {
              id: "3",
              label: "Englisch",
            },
            translationType: TranslationType.NICHT_AMTLICH,
            translators: ["translator a", "translator b"],
            borderNumbers: [23, 42],
            urls: ["http://link-to-translation.en"],
          }),
          new OriginOfTranslation({
            id: "2",
            languageCode: {
              id: "4",
              label: "Französisch",
            },
            translationType: TranslationType.AMTLICH,
            translators: ["translator c", "translator d"],
            borderNumbers: [13, 99],
            urls: ["https://link-to-translation.fr"],
          }),
        ],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(await screen.findByText("Herkunft der Übersetzung")).toBeVisible()

      const links = screen.getAllByRole("link")
      expect(links).toHaveLength(2)
      expect(links[0]).toHaveAttribute("href", "http://link-to-translation.en")
      expect(links[1]).toHaveAttribute("href", "https://link-to-translation.fr")

      const summaries = screen.getAllByTestId("origin-of-translation-summary")
      expect(summaries[0]).toHaveTextContent(
        "Englisch, translator a, translator b: 23, 42, http://link-to-translation.en (nicht-amtlich)",
      )
      expect(summaries[1]).toHaveTextContent(
        "Französisch, translator c, translator d: 13, 99, https://link-to-translation.fr (amtlich)",
      )
    })
  })

  describe("Appeal", () => {
    test("should not display appeal button when it is empty and not a financial court", async () => {
      // Arrange
      mockSessionStore({ appeal: undefined }, "BVerfG")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.queryByRole("button", { name: "Rechtsmittel" }),
      ).not.toBeInTheDocument()
      expect(screen.queryByTestId("appellants")).not.toBeInTheDocument()
    })

    test("should display appeal button when it is undefined and financial court", async () => {
      // Arrange
      mockSessionStore({ appeal: undefined }, "BFH", "Finanzgerichtsbarkeit")

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.getByRole("button", { name: "Rechtsmittel" }),
      ).toBeInTheDocument()
      expect(screen.queryByTestId("appellants")).not.toBeInTheDocument()
    })

    test("should display appeal when it is not undefined without financial court", async () => {
      // Arrange
      mockSessionStore(
        { appeal: { appealWithdrawal: AppealWithdrawal.JA } },
        "BVerfG",
      )

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.queryByRole("button", { name: "Rechtsmittel" }),
      ).not.toBeInTheDocument()
      expect(screen.getByTestId("appellants")).toBeInTheDocument()
    })
  })

  describe("JobProfiles", () => {
    test("should display job profile button when it is undefined", async () => {
      // Arrange
      mockSessionStore({ jobProfiles: undefined })

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.getByRole("button", { name: "Berufsbild" }),
      ).toBeInTheDocument()
      expect(screen.queryByTestId("job-profiles")).not.toBeInTheDocument()
    })

    test("should display job profiles when it is not undefined", async () => {
      // Arrange
      mockSessionStore({ jobProfiles: ["job profile 1", "job profile 2"] })

      // Act
      render(OtherCategories)

      // Assert
      expect(
        screen.queryByRole("button", { name: "Berufsbild" }),
      ).not.toBeInTheDocument()
      expect(screen.getByTestId("job-profiles")).toBeInTheDocument()
    })
  })

  describe("ObjectValues (Gegenstandswert)", () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    it("should display 'Gegenstandswert' button when no data", async () => {
      // Arrange
      mockSessionStore({
        objectValues: [],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(
        screen.getByRole("button", { name: "Gegenstandswert" }),
      ).toBeInTheDocument()
    })

    it("should display 'Gegenstandswert'", async () => {
      // Arrange
      mockSessionStore({
        objectValues: [
          new ObjectValue({
            id: "1",
            amount: 500,
            currencyCode: {
              id: "3",
              label: "Euro (EUR)",
            },
            proceedingType: ProceedingType.VERFASSUNGSBESCHWERDE,
          }),
        ],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(await screen.findByText("Gegenstandswert")).toBeVisible()
      const summary = screen.getByTestId("object-value-summary")
      expect(summary).toHaveTextContent("500 Euro (EUR), Verfassungsbeschwerde")
    })
  })

  describe("AbuseFees (Missbrauchsgebühr)", () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    it("should display 'Missbrauchsgebühr' button when no data", async () => {
      // Arrange
      mockSessionStore({
        abuseFees: [],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(
        screen.getByRole("button", { name: "Missbrauchsgebühr" }),
      ).toBeInTheDocument()
    })

    it("should display 'Missbrauchsgebühr'", async () => {
      // Arrange
      mockSessionStore({
        abuseFees: [
          new AbuseFee({
            id: "1",
            amount: 500,
            currencyCode: {
              id: "3",
              label: "Euro (EUR)",
            },
            addressee: Addressee.BEVOLLMAECHTIGTER,
          }),
        ],
      })

      // Act
      render(OtherCategories, {
        global: {
          plugins: [[router]],
        },
      })

      // Assert
      expect(await screen.findByText("Missbrauchsgebühr")).toBeVisible()
      const summary = screen.getByTestId("abuse-fee-summary")
      expect(summary).toHaveTextContent("500 Euro (EUR), Bevollmächtigter")
    })
  })
})
