import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import ActiveCitation from "@/domain/activeCitation"
import { AppealWithdrawal, PkhPlaintiff } from "@/domain/appeal"
import { AppealAdmitter } from "@/domain/appealAdmitter"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { Decision } from "@/domain/decision"
import Definition from "@/domain/definition"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import NormReference from "@/domain/normReference"
import ObjectValue, { CurrencyCode, ProceedingType } from "@/domain/objectValue"
import OriginOfTranslation, {
  TranslationType,
} from "@/domain/originOfTranslation"
import SingleNorm from "@/domain/singleNorm"

function renderComponent(contentRelatedIndexing?: ContentRelatedIndexing) {
  return {
    ...render(PreviewContentRelatedIndexing, {
      props: {
        contentRelatedIndexing: contentRelatedIndexing ?? {},
      },
      global: {
        provide: {
          [previewLayoutInjectionKey as symbol]: "wide",
        },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("foo", {
                    managementData: {
                      borderNumbers: ["1", "2"],
                      duplicateRelations: [],
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("preview content related indexing", () => {
  test("renders all content related indexing", async () => {
    renderComponent({
      keywords: ["keyword"],
      norms: [
        new NormReference({
          normAbbreviation: { abbreviation: "ABC" },
        }),
      ],
      activeCitations: [
        new ActiveCitation({
          court: {
            type: "type1",
            location: "location1",
            label: "label1",
          },
          documentType: {
            jurisShortcut: "documentTypeShortcut1",
            label: "documentType1",
          },
          fileNumber: "test fileNumber1",
        }),
      ],
      fieldsOfLaw: [
        {
          identifier: "AB-01",
          text: "Text for AB",
          children: [],
          hasChildren: false,
          norms: [],
        },
      ],
      jobProfiles: ["Handwerker", "Elektriker"],
      dismissalGrounds: ["Betriebsbedingte Kündigung"],
      dismissalTypes: ["Einführung neuer Technologien"],
      collectiveAgreements: [
        new CollectiveAgreement({ name: "Normalvertrag Chor" }),
        new CollectiveAgreement({ name: "Stehende Bühnen" }),
      ],
      hasLegislativeMandate: true,
      foreignLanguageVersions: [
        new ForeignLanguageVersion({
          languageCode: { id: "1", label: "Englisch" },
          link: "http://link-to-translation.en",
        }),
        new ForeignLanguageVersion({
          languageCode: { id: "2", label: "Französisch" },
          link: "https://link-to-translation.fr",
        }),
        new ForeignLanguageVersion({
          languageCode: { id: "3", label: "Spanisch" },
          link: "www.link-to-translation.es",
        }),
      ],
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
      evsf: "X 00 00-0-0",
      definitions: [
        new Definition({ definedTerm: "Finanzkraft" }),
        new Definition({
          definedTerm: "Kündigungsfrist",
          definingBorderNumber: 3,
        }),
      ],
      appealAdmission: {
        admitted: true,
        by: AppealAdmitter.BFH,
      },
      objectValues: [
        new ObjectValue({
          id: "1",
          amount: 1000,
          currencyCode: {
            id: "23",
            label: "Euro (EUR)",
          } as CurrencyCode,
          proceedingType: ProceedingType.VERFASSUNGSBESCHWERDE,
        }),
      ],
    })

    expect(await screen.findByText("Schlagwörter")).toBeInTheDocument()
    expect(await screen.findByText("Normen")).toBeInTheDocument()
    expect(await screen.findByText("Aktivzitierung")).toBeInTheDocument()
    expect(await screen.findByText("Rechtsmittelzulassung")).toBeInTheDocument()
    expect(await screen.findByText("Sachgebiete")).toBeInTheDocument()
    expect(await screen.findByText("Kündigungsgründe")).toBeInTheDocument()
    expect(await screen.findByText("Kündigungsarten")).toBeInTheDocument()
    expect(await screen.findByText("Berufsbild")).toBeInTheDocument()
    expect(await screen.findByText("Gesetzgebungsauftrag")).toBeInTheDocument()
    expect(await screen.findByText("Tarifvertrag")).toBeInTheDocument()
    expect(
      await screen.findByText("Fremdsprachige Fassung"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Herkunft der Übersetzung"),
    ).toBeInTheDocument()
    expect(await screen.findByText("E-VSF")).toBeInTheDocument()
    expect(await screen.findByText("Definition")).toBeInTheDocument()
    expect(await screen.findByText("Gegenstandswert")).toBeInTheDocument()
  })

  test("renders multiple keywords and nothing else", async () => {
    renderComponent({
      keywords: ["foo", "bar"],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      jobProfiles: [],
      hasLegislativeMandate: false,
      collectiveAgreements: [],
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Schlagwörter")).toBeInTheDocument()
    expect(await screen.findByText("foo")).toBeInTheDocument()
    expect(await screen.findByText("bar")).toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders multiple norms and single norms and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [
        new NormReference({
          normAbbreviation: { abbreviation: "ABC" },
        }),
        new NormReference({
          normAbbreviation: { abbreviation: "DEF" },
          singleNorms: [
            new SingleNorm({
              singleNorm: "§1",
            }),
            new SingleNorm({
              singleNorm: "§2",
            }),
          ],
        }),
      ],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      hasLegislativeMandate: false,
      collectiveAgreements: [],
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Normen")).toBeInTheDocument()
    expect(await screen.findByText("ABC")).toBeInTheDocument()
    expect(await screen.findByText("DEF - §1")).toBeInTheDocument()
    expect(await screen.findByText("DEF - §2")).toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders multiple active citations and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [
        new ActiveCitation({
          court: {
            type: "AG",
            location: "Aachen",
            label: "AG Aachen",
          },
          documentType: {
            jurisShortcut: "Bes",
            label: "Beschluss",
          },
          fileNumber: "ABC.123",
        }),
        new ActiveCitation({
          court: {
            type: "BVerfG",
            label: "BVerfG Karlsruhe",
          },
          documentType: {
            jurisShortcut: "Urt",
            label: "Urteil",
          },
          fileNumber: "DEF.456",
        }),
      ],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      hasLegislativeMandate: false,
      collectiveAgreements: [],
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Aktivzitierung")).toBeInTheDocument()
    expect(
      await screen.findByText("AG Aachen, ABC.123, Beschluss"),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("BVerfG Karlsruhe, DEF.456, Urteil"),
    ).toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders multiple fields of law and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [
        {
          identifier: "AB-01-01",
          text: "Text for AB-01-01",
          children: [],
          hasChildren: false,
          norms: [],
          parent: {
            identifier: "AB-01",
            text: "Text for AB-01",
            children: [],
            hasChildren: false,
            norms: [],
            parent: {
              identifier: "AB",
              text: "Text for AB",
              children: [],
              hasChildren: false,
              norms: [],
            },
          },
        },
        {
          identifier: "CD-01-05",
          text: "Text for CD-01-05",
          children: [],
          hasChildren: false,
          norms: [],
          parent: {
            identifier: "CD-01",
            text: "Text for CD-01",
            children: [],
            hasChildren: false,
            norms: [],
            parent: {
              identifier: "CD",
              text: "Text for CD",
              children: [],
              hasChildren: false,
              norms: [],
            },
          },
        },
      ],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      hasLegislativeMandate: false,
      collectiveAgreements: [],
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Sachgebiete")).toBeInTheDocument()

    expect(await screen.findByText("Text for AB-01-01")).toBeInTheDocument()
    expect(await screen.findByText("Text for AB-01")).toBeInTheDocument()
    expect(await screen.findByText("Text for AB")).toBeInTheDocument()

    expect(await screen.findByText("Text for CD-01-05")).toBeInTheDocument()
    expect(await screen.findByText("Text for CD-01")).toBeInTheDocument()
    expect(await screen.findByText("Text for CD")).toBeInTheDocument()

    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders multiple job profiles and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: ["foo", "bar"],
      dismissalGrounds: [],
      collectiveAgreements: [],
      dismissalTypes: [],
      hasLegislativeMandate: false,
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Berufsbild")).toBeInTheDocument()
    expect(await screen.findByText("foo")).toBeInTheDocument()
    expect(await screen.findByText("bar")).toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders multiple collective agreements and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      collectiveAgreements: [
        new CollectiveAgreement({
          name: "Normalvertrag Chor",
          date: "2000",
          norm: "§ 23",
          industry: {
            id: "290b39dc-9368-4d1c-9076-7f96e05cb575",
            label: "Bühne, Theater, Orchester",
          },
        }),
        new CollectiveAgreement({
          name: "Stehende Bühnen",
          industry: {
            id: "290b39dc-9368-4d1c-9076-7f96e05cb575",
            label: "Bühne, Theater, Orchester",
          },
        }),
      ],
      dismissalGrounds: [],
      dismissalTypes: [],
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Tarifvertrag")).toBeInTheDocument()
    expect(
      await screen.findByText(
        "Normalvertrag Chor, 2000, § 23 (Bühne, Theater, Orchester)",
      ),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Stehende Bühnen (Bühne, Theater, Orchester)"),
    ).toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders legislative mandate and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: true,
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(await screen.findByText("Gesetzgebungsauftrag")).toBeInTheDocument()
    expect(await screen.findByText("Ja")).toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders dismissal inputs and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: ["ground"],
      dismissalTypes: ["type"],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      definitions: [],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(await screen.findByText("Kündigungsgründe")).toBeInTheDocument()
    expect(await screen.findByText("Kündigungsarten")).toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders foreign language versions and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      foreignLanguageVersions: [
        new ForeignLanguageVersion({
          languageCode: { id: "1", label: "Englisch" },
          link: "http://link-to-translation.en",
        }),
        new ForeignLanguageVersion({
          languageCode: { id: "2", label: "Französisch" },
          link: "https://link-to-translation.fr",
        }),
        new ForeignLanguageVersion({
          languageCode: { id: "3", label: "Spanisch" },
          link: "www.link-to-translation.es",
        }),
      ],
      originOfTranslations: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(screen.getByText("Fremdsprachige Fassung")).toBeInTheDocument()
    expect(screen.getByText("Englisch:")).toBeInTheDocument()
    expect(screen.getByText("Französisch:")).toBeInTheDocument()
    expect(screen.getByText("Spanisch:")).toBeInTheDocument()
    const links = screen.getAllByRole("link")
    expect(links).toHaveLength(3)
    expect(links[0]).toHaveAttribute("href", "http://link-to-translation.en")
    expect(links[1]).toHaveAttribute("href", "https://link-to-translation.fr")
    expect(links[2]).toHaveAttribute(
      "href",
      "https://www.link-to-translation.es",
    )
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders origin of translation and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      foreignLanguageVersions: [],
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
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(screen.getByText("Herkunft der Übersetzung")).toBeInTheDocument()
    expect(screen.getByTestId("Herkunft der Übersetzung")).toHaveTextContent(
      "Englisch, translator a, translator b: 23, 42, http://link-to-translation.en (nicht-amtlich)" +
        "Französisch, translator c, translator d: 13, 99, https://link-to-translation.fr (amtlich)",
    )
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders no legislative mandate when it is false", async () => {
    renderComponent({
      hasLegislativeMandate: false,
    })

    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
  })

  test("renders E-VSF and nothing else", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      originOfTranslations: [],
      evsf: "X 00 00-0-0",
      definitions: [],
      appealAdmission: undefined,
      objectValues: [],
    })

    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(await screen.findByText("E-VSF")).toBeInTheDocument()
    expect(await screen.findByText("X 00 00-0-0")).toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders definitions and nothing else", async () => {
    const { container } = renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      originOfTranslations: [],
      definitions: [
        new Definition({ definedTerm: "Finanzkraft" }),
        new Definition({
          definedTerm: "Richtige Randnummer",
          definingBorderNumber: 1,
        }),
        new Definition({
          definedTerm: "Kündigungsfrist (falsche Randnummer)",
          definingBorderNumber: 3,
        }),
      ],
      objectValues: [],
    })

    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(container).toHaveTextContent(
      "DefinitionFinanzkraftRichtige Randnummer | 1Kündigungsfrist (falsche Randnummer) | 3",
    )
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders 'Gegenstandswert' and nothing else", async () => {
    const { container } = renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: false,
      originOfTranslations: [],
      definitions: [],
      objectValues: [
        new ObjectValue({
          id: "1",
          amount: 1000,
          currencyCode: {
            id: "23",
            label: "Euro (EUR)",
          } as CurrencyCode,
          proceedingType: ProceedingType.VERFASSUNGSBESCHWERDE,
        }),
      ],
    })

    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(container).toHaveTextContent(
      "Gegenstandswert1.000 Euro (EUR), Verfassungsbeschwerde",
    )
  })

  describe("renders appeal admission", () => {
    test("no", async () => {
      const { container } = renderComponent({
        appealAdmission: {
          admitted: false,
        },
      })

      expect(container).toHaveTextContent("RechtsmittelzulassungNein")
    })

    test("yes, without appeal admitter", async () => {
      const { container } = renderComponent({
        appealAdmission: {
          admitted: true,
          by: undefined,
        },
      })

      expect(container).toHaveTextContent("RechtsmittelzulassungJa")
    })

    test("yes, with appeal admitter", async () => {
      const { container } = renderComponent({
        appealAdmission: {
          admitted: true,
          by: AppealAdmitter.FG,
        },
      })

      expect(container).toHaveTextContent("RechtsmittelzulassungJa, durch FG")
    })
  })

  test("renders appeal", () => {
    const { container } = renderComponent({
      appeal: {
        appellants: [{ id: "1", value: "Kläger" }],
        revisionDefendantStatuses: [
          { id: "1", value: "unbegründet" },
          { id: "1", value: "unzulässig" },
        ],
        revisionPlaintiffStatuses: [{ id: "1", value: "unbegründet" }],
        jointRevisionDefendantStatuses: [{ id: "1", value: "unbegründet" }],
        jointRevisionPlaintiffStatuses: [{ id: "1", value: "unbegründet" }],
        nzbDefendantStatuses: [{ id: "1", value: "unbegründet" }],
        nzbPlaintiffStatuses: [{ id: "1", value: "unbegründet" }],
        appealWithdrawal: AppealWithdrawal.JA,
        pkhPlaintiff: PkhPlaintiff.NEIN,
      },
    })

    expect(container).toHaveTextContent("RechtsmittelführerKläger")
    expect(container).toHaveTextContent(
      "Revision (Beklagter)unbegründet, unzulässig",
    )
    expect(container).toHaveTextContent("Revision (Kläger)unbegründet")
    expect(container).toHaveTextContent(
      "Anschlussrevision (Beklagter)unbegründet",
    )
    expect(container).toHaveTextContent("Anschlussrevision (Kläger)unbegründet")
    expect(container).toHaveTextContent("NZB (Beklagter)unbegründet")
    expect(container).toHaveTextContent("NZB (Kläger)unbegründet")
    expect(container).toHaveTextContent("Zurücknahme der RevisionJa")
    expect(container).toHaveTextContent("PKH-Antrag (Kläger)Nein")
  })

  test("renders nothing when elements are empty", async () => {
    renderComponent({
      keywords: [],
      norms: [],
      activeCitations: [],
      fieldsOfLaw: [],
      jobProfiles: [],
      dismissalGrounds: [],
      dismissalTypes: [],
      collectiveAgreements: [],
      hasLegislativeMandate: undefined,
      foreignLanguageVersions: [],
      originOfTranslations: [],
      evsf: "",
      definitions: [],
      appealAdmission: undefined,
      appeal: {
        appellants: [],
        revisionDefendantStatuses: [],
        revisionPlaintiffStatuses: [],
        jointRevisionDefendantStatuses: [],
        jointRevisionPlaintiffStatuses: [],
        nzbDefendantStatuses: [],
        nzbPlaintiffStatuses: [],
        appealWithdrawal: undefined,
        pkhPlaintiff: undefined,
      },
      objectValues: [],
    })
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittel")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })

  test("renders nothing when elements are undefined", async () => {
    renderComponent({
      keywords: undefined,
      norms: undefined,
      activeCitations: undefined,
      fieldsOfLaw: undefined,
      jobProfiles: undefined,
      hasLegislativeMandate: undefined,
      dismissalGrounds: undefined,
      dismissalTypes: undefined,
      collectiveAgreements: undefined,
      foreignLanguageVersions: undefined,
      originOfTranslations: undefined,
      evsf: undefined,
      definitions: undefined,
      appealAdmission: undefined,
      appeal: undefined,
      objectValues: undefined,
    })
    expect(screen.queryByText("Schlagwörter")).not.toBeInTheDocument()
    expect(screen.queryByText("Normen")).not.toBeInTheDocument()
    expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
    expect(screen.queryByText("Sachgebiete")).not.toBeInTheDocument()
    expect(screen.queryByText("Berufsbild")).not.toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsgründe")).not.toBeInTheDocument()
    expect(screen.queryByText("Kündigungsarten")).not.toBeInTheDocument()
    expect(screen.queryByText("Tarifvertrag")).not.toBeInTheDocument()
    expect(screen.queryByText("E-VSF")).not.toBeInTheDocument()
    expect(screen.queryByText("Definition")).not.toBeInTheDocument()
    expect(screen.queryByText("Fremdsprachige Fassung")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Herkunft der Übersetzung"),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittelzulassung")).not.toBeInTheDocument()
    expect(screen.queryByText("Rechtsmittel")).not.toBeInTheDocument()
    expect(screen.queryByText("Gegenstandswert")).not.toBeInTheDocument()
  })
})
