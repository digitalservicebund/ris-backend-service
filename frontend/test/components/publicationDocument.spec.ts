import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import { Stubs } from "@vue/test-utils/dist/types"
import { createRouter, createWebHistory } from "vue-router"
import PublicationDocument from "@/components/PublicationDocument.vue"
import DocumentUnit from "@/domain/documentUnit"
import LegalForce from "@/domain/legalForce"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import { PublicationHistoryRecordType } from "@/domain/xmlMail"
import publishService from "@/services/publishService"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/caselaw/documentUnit/:documentNumber/categories",
      name: "caselaw-documentUnit-documentNumber-categories",
      component: {},
    },
    {
      path: "/",
      name: "caselaw",
      component: {},
    },
  ],
})

function renderComponent(
  options: {
    props?: unknown
    documentUnit?: DocumentUnit
    stubs?: Stubs
  } = {},
) {
  const user = userEvent.setup()

  return {
    user,
    ...render(PublicationDocument, {
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
                    new DocumentUnit("123", {
                      documentNumber: "foo",
                    }),
                },
              },
            }),
          ],
        ],
        stubs: options.stubs ?? undefined,
      },
    }),
  }
}

describe("PublicationDocument:", () => {
  vi.spyOn(publishService, "getPreview").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: {
        xml: "<xml>all good</xml>",
        statusCode: "200",
      },
    }),
  )

  describe("renders plausibility check", () => {
    it("with all required fields filled", async () => {
      renderComponent({
        documentUnit: new DocumentUnit("123", {
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
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt"),
      ).toBeInTheDocument()
      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()
      expect(
        await screen.findByText("XML Vorschau der Veröffentlichung"),
      ).toBeInTheDocument()
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
        documentUnit: new DocumentUnit("123", {
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

      expect(
        screen.queryByText("XML Vorschau der Veröffentlichung"),
      ).not.toBeInTheDocument()
    })

    it("'Rubriken bearbeiten' button links back to categories", async () => {
      render(PublicationDocument, {
        global: {
          plugins: [router],
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
  })

  describe("on press 'Dokumentationseinheit veröffentlichen'", () => {
    it("publishes successfully", async () => {
      const { emitted } = renderComponent({
        documentUnit: new DocumentUnit("123", {
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
      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(emitted().publishDocument).toBeTruthy()
    })

    it("renders error modal from backend", async () => {
      renderComponent({
        props: {
          publishResult: {
            xml: "xml",
            statusMessages: ["error message 1", "error message 2"],
            statusCode: "400",
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: undefined,
          },
          errorMessage: {
            title: "error message title",
            description: "error message description",
          },
        },
      })

      expect(
        screen.queryByLabelText("Erfolg der Veröffentlichung"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei Veröffentlichung"),
      ).toHaveTextContent(`error message titleerror message description`)
    })

    it("renders error modal from frontend", async () => {
      renderComponent()

      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(
        screen.queryByLabelText("Erfolg der Veröffentlichung"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei Veröffentlichung"),
      ).toHaveTextContent(
        `Es sind noch nicht alle Pflichtfelder befüllt.Die Dokumentationseinheit kann nicht veröffentlicht werden.`,
      )
    })
  })

  describe("last published xml", () => {
    it("with earlier published document unit", async () => {
      renderComponent({
        props: {
          publicationLog: [
            {
              type: PublicationHistoryRecordType.PUBLICATION,
              xml: '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
              statusMessages: ["success"],
              statusCode: "200",
              receiverAddress: "receiver address",
              mailSubject: "mail subject",
              date: "01.02.2000",
            },
          ],
        },
      })
      expect(
        screen.getByLabelText("Letzte Veröffentlichungen"),
      ).toHaveTextContent(
        `Letzte VeröffentlichungenXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALSXML1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>`,
      )
    })

    it("without earlier published document unit", async () => {
      renderComponent()
      expect(
        screen.getByLabelText("Letzte Veröffentlichungen"),
      ).toHaveTextContent(
        `Letzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht`,
      )
    })
  })

  it("with preview stubbing", async () => {
    renderComponent({
      documentUnit: new DocumentUnit("123", {
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

    expect(
      await screen.findByText("XML Vorschau der Veröffentlichung"),
    ).toBeInTheDocument()

    await fireEvent.click(screen.getByText("XML Vorschau der Veröffentlichung"))
    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("<xml>all good</xml>")
  })

  it("with stubbing", () => {
    const { container } = renderComponent({
      props: {
        publicationLog: [
          {
            type: PublicationHistoryRecordType.PUBLICATION,
            xml: "xml content",
            statusMessages: ["success"],
            statusCode: "200",
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: "01.02.2000",
          },
        ],
      },
      documentUnit: new DocumentUnit("123", {
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

    expect(container).toHaveTextContent(
      `VeröffentlichenPlausibilitätsprüfungAlle Pflichtfelder sind korrekt ausgefülltDokumentationseinheit veröffentlichenLetzte VeröffentlichungenXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALS`,
    )

    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("XML")
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})
