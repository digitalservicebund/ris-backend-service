import { userEvent } from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import HandoverView from "@/components/HandoverView.vue"
import DocumentUnit from "@/domain/documentUnit"
import { EventRecordType } from "@/domain/eventRecord"
import LegalForce from "@/domain/legalForce"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import handoverService from "@/services/handoverService"

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

const setupWithDocUnitThatHasBeenHandedOver = () =>
  render(HandoverView, {
    props: {
      documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
      eventLog: [
        {
          type: EventRecordType.HANDOVER,
          xml: '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
          statusMessages: ["success"],
          success: true,
          receiverAddress: "receiver address",
          mailSubject: "mail subject",
          date: "01.02.2000",
        },
      ],
    },
    global: {
      plugins: [router],
    },
  })

const setupWithAllRequiredFields = () =>
  render(HandoverView, {
    props: {
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
    },
    global: {
      plugins: [router],
    },
  })

describe("HandoverView:", () => {
  vi.spyOn(handoverService, "getPreview").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: {
        xml: "<xml>all good</xml>",
        success: true,
      },
    }),
  )

  describe("renders plausibility check", () => {
    it("with all required fields filled", async () => {
      setupWithAllRequiredFields()

      expect(
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt"),
      ).toBeInTheDocument()
      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()
      expect(await screen.findByText("XML Vorschau")).toBeInTheDocument()
    })

    it("render preview error", async () => {
      render(HandoverView, {
        props: {
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
          errorMessage: {
            title: "preview error",
            description: "error message description",
          },
        },
        global: {
          plugins: [router],
        },
      })
      expect(await screen.findByText("preview error")).toBeInTheDocument()
    })

    it("with required fields missing", async () => {
      render(HandoverView, {
        props: {
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
        },
        global: {
          plugins: [router],
        },
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

    it("'Rubriken bearbeiten' button links back to categories", async () => {
      render(HandoverView, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
        },
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

  describe("on press 'Dokumentationseinheit an jDV übergeben'", () => {
    it("hands over successfully", async () => {
      const { emitted } = setupWithAllRequiredFields()
      const handoverButton = screen.getByRole("button", {
        name: "Dokumentationseinheit an jDV übergeben",
      })
      await fireEvent.click(handoverButton)

      expect(emitted().handoverDocument).toBeTruthy()
    })

    it("renders error modal from backend", async () => {
      render(HandoverView, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
          handoverResult: {
            xml: "xml",
            statusMessages: ["error message 1", "error message 2"],
            success: false,
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: undefined,
          },
          errorMessage: {
            title: "error message title",
            description: "error message description",
          },
        },
        global: {
          plugins: [router],
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
      render(HandoverView, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
        },
        global: {
          plugins: [router],
        },
      })

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
      setupWithDocUnitThatHasBeenHandedOver()
      expect(screen.getByLabelText("Letzte Ereignisse")).toHaveTextContent(
        `Letzte EreignisseXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALSXML1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>`,
      )
    })

    it("without earlier handed over document unit", async () => {
      render(HandoverView, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
        },
        global: {
          plugins: [router],
        },
      })
      expect(screen.getByLabelText("Letzte Ereignisse")).toHaveTextContent(
        `Letzte Ereignisse Diese Dokumentationseinheit wurde bisher nicht an die jDV übergeben`,
      )
    })
  })

  it("with preview stubbing", async () => {
    render(HandoverView, {
      props: {
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
      },
      global: {
        plugins: [router],
        stubs: {
          CodeSnippet: {
            template: '<div data-testid="code-snippet"/>',
          },
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

  it("with stubbing", () => {
    const { container } = render(HandoverView, {
      props: {
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
        eventLog: [
          {
            type: EventRecordType.HANDOVER,
            xml: "xml content",
            statusMessages: ["success"],
            success: true,
            receiverAddress: "receiver address",
            mailSubject: "mail subject",
            date: "01.02.2000",
          },
        ],
      },
      global: {
        plugins: [router],
        stubs: {
          CodeSnippet: {
            template: '<div data-testid="code-snippet"/>',
          },
        },
      },
    })

    expect(container).toHaveTextContent(
      `Übergabe an jDVPlausibilitätsprüfungAlle Pflichtfelder sind korrekt ausgefülltDokumentationseinheit an jDV übergebenLetzte EreignisseXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALS`,
    )

    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("XML")
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})
