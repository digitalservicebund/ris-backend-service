import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import HandoverDecisionView from "@/components/HandoverDecisionView.vue"
import { Decision } from "@/domain/decision"
import { Env } from "@/domain/env"
import { EventRecordType, HandoverMail, Preview } from "@/domain/eventRecord"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import LegalForce from "@/domain/legalForce"
import { DuplicateRelationStatus } from "@/domain/managementData"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import featureToggleService from "@/services/featureToggleService"
import handoverDocumentationUnitService from "@/services/handoverDocumentationUnitService"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"

import { TextCheckAllResponse } from "@/types/textCheck"
import routes from "~/test-helper/routes"

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
})

function renderComponent(
  options: {
    props?: unknown
    documentUnit?: Decision
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    stubs?: any
    env?: Env
  } = {},
) {
  const user = userEvent.setup()

  return {
    user,
    ...render(HandoverDecisionView, {
      props: options.props ?? {},
      global: {
        plugins: [
          [router],
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit:
                    options.documentUnit ??
                    new Decision("123", {
                      documentNumber: "foo",
                    }),
                },
                session: { env: options.env ?? "staging" },
              },
            }),
          ],
        ],
        stubs: options.stubs ?? undefined,
      },
    }),
  }
}

describe("HandoverDocumentationUnitView:", () => {
  beforeEach(() => {
    vi.spyOn(handoverDocumentationUnitService, "getPreview").mockResolvedValue({
      status: 200,
      data: new Preview({
        xml: "<xml>all good</xml>",
        success: true,
      }),
    })

    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })

    vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
      status: 200,
      data: {
        suggestions: [],
        totalTextCheckErrors: 0,
        categoryTypes: [],
      },
    } as ServiceResponse<TextCheckAllResponse>)
  })
  describe("renders plausibility check", () => {
    it("with all required fields filled", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
        }),
      })

      expect(
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeInTheDocument()
      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()
      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
    })

    it("render preview error", async () => {
      renderComponent({
        props: {
          errorMessage: {
            title: "preview error",
            description: "error message description",
          },
        },
      })
      expect(await screen.findByText("preview error")).toBeInTheDocument()
    })

    it("with required fields missing", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          contentRelatedIndexing: {
            norms: [
              new NormReference({
                normAbbreviationRawValue: "ABC",
                singleNorms: [
                  new SingleNorm({
                    singleNorm: "§ 1",
                    legalForce: new LegalForce(),
                  }),
                ],
              }),
            ],
          },
        }),
      })
      expect(
        await screen.findByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).toBeInTheDocument()

      expect(screen.getByText("Aktenzeichen")).toBeInTheDocument()
      expect(screen.getByText("Gericht")).toBeInTheDocument()
      expect(screen.getByText("Entscheidungsdatum")).toBeInTheDocument()
      expect(screen.getByText("Rechtskraft")).toBeInTheDocument()
      expect(screen.getByText("Dokumenttyp")).toBeInTheDocument()
      expect(screen.getByText("Normen")).toBeInTheDocument()
      expect(screen.getByText("ABC")).toBeInTheDocument()
      expect(screen.getByText("Gesetzeskraft")).toBeInTheDocument()

      expect(screen.queryByText("XML Vorschau")).not.toBeInTheDocument()
    })

    it("should show error message with invalid outline", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          shortTexts: { otherHeadnote: "Other Headnote" },
          longTexts: { outline: "Outline" },
        }),
      })
      expect(
        await screen.findByText(
          'Die Rubriken "Gliederung" und "Sonstiger Orientierungssatz" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein.',
        ),
      ).toBeInTheDocument()

      expect(
        await screen.findByLabelText("Rubriken bearbeiten"),
      ).toBeInTheDocument()
      expect(screen.queryByText("XML Vorschau")).not.toBeInTheDocument()
    })

    it("should show validation error message when casefacts are invalid", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          longTexts: { reasons: "Reasons", caseFacts: "CaseFacts" },
        }),
      })

      expect(
        await screen.findByText(
          'Die Rubriken "Gründe" und "Tatbestand" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein.',
        ),
      ).toBeInTheDocument()
      expect(screen.getByText("Rubriken bearbeiten")).toBeInTheDocument()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).not.toBeInTheDocument()
      expect(screen.queryByText("XML Vorschau")).not.toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeDisabled()
    })

    it("should show no validation error message when casefacts are valid", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          longTexts: { caseFacts: "CaseFacts" },
        }),
      })

      expect(
        screen.queryByText(
          'Die Rubriken "Gründe" und "Tatbestand" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein.',
        ),
      ).not.toBeInTheDocument()
      expect(screen.queryByText("Rubriken bearbeiten")).not.toBeInTheDocument()

      expect(
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeInTheDocument()
      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeEnabled()
    })

    it("should show validation error message when decisionReasons are invalid", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          longTexts: { reasons: "Reasons", decisionReasons: "decisionReasons" },
        }),
      })

      expect(
        await screen.findByText(
          'Die Rubriken "Gründe" und "Entscheidungsgründe" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein.',
        ),
      ).toBeInTheDocument()
      expect(screen.getByText("Rubriken bearbeiten")).toBeInTheDocument()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).not.toBeInTheDocument()
      expect(screen.queryByText("XML Vorschau")).not.toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeDisabled()
    })

    it("should show no validation error message when decisionReasons are valid", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          longTexts: { decisionReasons: "decisionReasons" },
        }),
      })

      expect(
        screen.queryByText(
          'Die Rubriken "Gründe" und "Entscheidungsgründe" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein.',
        ),
      ).not.toBeInTheDocument()
      expect(screen.queryByText("Rubriken bearbeiten")).not.toBeInTheDocument()

      expect(
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeInTheDocument()
      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeEnabled()
    })

    it("'Rubriken bearbeiten' button links back to categories", async () => {
      const pinia = createTestingPinia({
        initialState: {
          docunitStore: {
            documentUnit: new Decision("123", {
              documentNumber: "foo",
              longTexts: {
                reasons: "Reasons",
                decisionReasons: "decisionReasons",
              },
            }),
          },
        },
      })
      render(HandoverDecisionView, {
        global: {
          plugins: [[router], [pinia]],
        },
      })

      expect(
        await screen.findByLabelText("Rubriken bearbeiten"),
      ).toBeInTheDocument()

      await userEvent.click(screen.getByLabelText("Rubriken bearbeiten"))

      expect(router.currentRoute.value.name).toBe(
        "caselaw-documentUnit-documentNumber-categories",
      )
    })

    it("should not allow to publish with pending duplicate", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          managementData: {
            duplicateRelations: [
              {
                documentNumber: "documentNumber",
                status: DuplicateRelationStatus.PENDING,
                isJdvDuplicateCheckActive: true,
              },
            ],
            borderNumbers: [],
          },
          coreData: {
            fileNumbers: ["foo"],
            court: { type: "type", location: "location", label: "label" },
            decisionDate: "2022-02-01",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
        }),
      })

      expect(
        screen.getByText("Es besteht Dublettenverdacht."),
      ).toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeDisabled()
    })

    it("should allow to publish with ignored duplicate", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          longTexts: { decisionReasons: "decisionReasons" },
          managementData: {
            duplicateRelations: [
              {
                documentNumber: "documentNumber",
                isJdvDuplicateCheckActive: true,
                status: DuplicateRelationStatus.IGNORED,
              },
            ],
            borderNumbers: [],
          },
        }),
      })

      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
      expect(
        screen.getByText("Es besteht kein Dublettenverdacht."),
      ).toBeInTheDocument()
      expect(
        screen.getByRole("button", {
          name: "Dokumentationseinheit an jDV übergeben",
        }),
      ).toBeEnabled()
    })

    it("should show a warning for filled unexportable fields", async () => {
      renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            oralHearingDates: ["2022-02-01"],
            hasDeliveryDate: true,
            celexNumber: "celexNumber",
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
          contentRelatedIndexing: {
            evsf: "X 00 00",
            foreignLanguageVersions: [new ForeignLanguageVersion()],
            appealAdmission: {
              admitted: false,
            },
          },
          longTexts: { decisionReasons: "decisionReasons" },
          managementData: {
            duplicateRelations: [],
            borderNumbers: [],
          },
        }),
      })

      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
      expect(
        screen.getByText(
          "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden:",
        ),
      ).toBeInTheDocument()
      expect(screen.getByText("E-VSF")).toBeInTheDocument()
      expect(
        screen.getByText("Zustellung an Verkündungs statt"),
      ).toBeInTheDocument()
      expect(
        screen.getByText("Datum der mündlichen Verhandlung"),
      ).toBeInTheDocument()
      expect(screen.getByText("Rechtsmittelzulassung")).toBeInTheDocument()
      const handoverButton = screen.getByRole("button", {
        name: "Dokumentationseinheit an jDV übergeben",
      })
      expect(handoverButton).toBeEnabled()
      await fireEvent.click(handoverButton)

      expect(
        screen.getByText("Prüfung hat Warnungen ergeben"),
      ).toBeInTheDocument()
      expect(
        screen.getByRole("button", { name: "Trotzdem übergeben" }),
      ).toBeInTheDocument()
    })
  })

  it("should show error message with invalid border numbers", async () => {
    renderComponent({
      documentUnit: new Decision("123", {
        documentNumber: "foo",
        coreData: {
          fileNumbers: ["foo"],
          court: {
            type: "type",
            location: "location",
            label: "label",
          },
          decisionDate: "2022-02-01",
          legalEffect: "legalEffect",
          documentType: {
            jurisShortcut: "ca",
            label: "category",
          },
        },
        longTexts: {
          otherLongText: "<border-number><number>4</number></border-number>",
        },
      }),
      stubs: {
        CodeSnippet: {
          template: '<div data-testid="code-snippet"/>',
        },
      },
    })
    expect(
      await screen.findByLabelText("Randnummernprüfung"),
    ).toHaveTextContent(
      "Randnummernprüfung " +
        "Die Reihenfolge der Randnummern ist nicht korrekt. " +
        "Rubrik" +
        "Sonstiger Langtext " +
        "Erwartete Randnummer 1 " +
        "Tatsächliche Randnummer 4" +
        "Randnummern neu berechnen",
    )
  })

  describe("on press 'Dokumentationseinheit an jDV übergeben'", () => {
    it("hands over successfully", async () => {
      const { emitted } = renderComponent({
        documentUnit: new Decision("123", {
          documentNumber: "foo",
          coreData: {
            fileNumbers: ["foo"],
            court: {
              type: "type",
              location: "location",
              label: "label",
            },
            decisionDate: "2022-02-01",
            legalEffect: "legalEffect",
            documentType: {
              jurisShortcut: "ca",
              label: "category",
            },
          },
        }),
      })
      const handoverButton = screen.getByRole("button", {
        name: "Dokumentationseinheit an jDV übergeben",
      })
      await fireEvent.click(handoverButton)

      expect(emitted().handoverDocument).toBeTruthy()
    })

    it("renders error modal from backend", async () => {
      renderComponent({
        props: {
          handoverResult: new HandoverMail({
            type: EventRecordType.HANDOVER,
            attachments: [{ fileContent: "xml" }],
            statusMessages: ["error message 1", "error message 2"],
            success: false,
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: undefined,
          }),
          errorMessage: {
            title: "error message title",
            description: "error message description",
          },
        },
      })

      expect(
        screen.queryByLabelText("Erfolg der jDV Übergabe"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei jDV Übergabe"),
      ).toHaveTextContent(`error message titleerror message description`)
    })

    it("renders error modal from frontend", async () => {
      renderComponent()

      const handoverButton = screen.getByRole("button", {
        name: "Dokumentationseinheit an jDV übergeben",
      })
      await fireEvent.click(handoverButton)

      expect(
        screen.queryByLabelText("Erfolg der jDV Übergabe"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei jDV Übergabe"),
      ).toHaveTextContent(
        `Es sind noch nicht alle Pflichtfelder befüllt.Die Dokumentationseinheit kann nicht übergeben werden.`,
      )
    })
  })

  describe("last handed over xml", () => {
    it("with earlier handed over document unit", async () => {
      renderComponent({
        props: {
          eventLog: [
            new HandoverMail({
              type: EventRecordType.HANDOVER,
              attachments: [
                {
                  fileContent:
                    '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
                  fileName: "file.xml",
                },
              ],
              statusMessages: ["success"],
              success: true,
              receiverAddress: "receiver address",
              mailSubject: "mail subject",
              date: "01.02.2000",
            }),
          ],
        },
      })
      expect(screen.getByLabelText("Letzte Ereignisse")).toHaveTextContent(
        `Letzte EreignisseXml Email Abgabe - 02.01.2000 um 00:00 UhrE-Mail an: receiver address Betreff: mail subjectXML1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>`,
      )
    })

    it("without earlier handed over document unit", async () => {
      renderComponent()
      expect(screen.getByLabelText("Letzte Ereignisse")).toHaveTextContent(
        `Letzte Ereignisse Diese Dokumentationseinheit wurde bisher nicht an die jDV übergeben`,
      )
    })
  })

  it("with preview stubbing", async () => {
    renderComponent({
      documentUnit: new Decision("123", {
        coreData: {
          fileNumbers: ["foo"],
          court: { type: "type", location: "location", label: "label" },
          decisionDate: "2022-02-01",
          legalEffect: "legalEffect",
          documentType: {
            jurisShortcut: "ca",
            label: "category",
          },
        },
      }),
      stubs: {
        CodeSnippet: {
          template: '<div data-testid="code-snippet"/>',
        },
      },
    })

    expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()

    await fireEvent.click(screen.getByText("XML Vorschau"))
    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("<xml>all good</xml>")
  })

  it("should not allow to publish when publication is scheduled", async () => {
    renderComponent({
      documentUnit: new Decision("123", {
        managementData: {
          duplicateRelations: [],
          borderNumbers: [],
          scheduledPublicationDateTime: "2050-01-01T04:00:00.000Z",
        },
        coreData: {
          fileNumbers: ["foo"],
          court: { type: "type", location: "location", label: "label" },
          decisionDate: "2022-02-01",
          legalEffect: "legalEffect",
          documentType: {
            jurisShortcut: "ca",
            label: "category",
          },
        },
      }),
    })

    expect(
      screen.getByRole("button", {
        name: "Dokumentationseinheit an jDV übergeben",
      }),
    ).toBeDisabled()
  })

  it("should show the scheduling component", async () => {
    renderComponent()

    expect(screen.getByLabelText("Terminiertes Datum")).toBeVisible()
  })

  it("with stubbing", async () => {
    const { container } = renderComponent({
      props: {
        eventLog: [
          new HandoverMail({
            type: EventRecordType.HANDOVER,
            attachments: [{ fileContent: "xml content", fileName: "file.xml" }],
            statusMessages: ["success"],
            success: true,
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: "01.02.2000",
          }),
        ],
      },
      documentUnit: new Decision("123", {
        coreData: {
          fileNumbers: ["foo"],
          court: { type: "type", location: "location", label: "label" },
          decisionDate: "2022-02-01",
          legalEffect: "legalEffect",
          documentType: {
            jurisShortcut: "ca",
            label: "category",
          },
        },
        longTexts: {
          reasons: "<border-number><number>1</number></border-number>",
        },
      }),
      stubs: {
        CodeSnippet: {
          template: '<div data-testid="code-snippet"/>',
        },
      },
    })

    // Wait for XML Vorschau
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(container).toHaveTextContent(
      `Übergabe an jDVPlausibilitätsprüfungAlle Pflichtfelder sind korrekt ausgefüllt.RandnummernprüfungDie Reihenfolge der Randnummern ist korrekt.DublettenprüfungEs besteht kein Dublettenverdacht.RechtschreibprüfungEs wurden keine Rechtschreibfehler identifiziert.XML VorschauDokumentationseinheit an jDV übergebenOder für später terminieren:Datum * Uhrzeit * Termin setzenLetzte EreignisseXml Email Abgabe - 02.01.2000 um 00:00 UhrE-Mail an: receiver address Betreff: mail subject`,
    )

    const codeSnippet = screen.queryByTestId(
      "xml-handover-code-snippet-preview",
    )

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("XML")
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})

describe("renders uat test mode hint", () => {
  it("only in uat", async () => {
    renderComponent({
      env: { environment: "uat" },
    })
    expect(
      screen.getByText("UAT Testmodus für die Übergabe an die jDV"),
    ).toBeInTheDocument()
  })

  it("not in prod", async () => {
    renderComponent({
      env: { environment: "production" },
    })
    expect(
      screen.queryByText("UAT Testmodus für die Übergabe an die jDV"),
    ).not.toBeInTheDocument()
  })

  it("not in staging", async () => {
    renderComponent()
    expect(
      screen.queryByText("UAT Testmodus für die Übergabe an die jDV"),
    ).not.toBeInTheDocument()
  })
})
