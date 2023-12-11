import { userEvent } from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import PublicationDocument from "@/components/PublicationDocument.vue"
import DocumentUnit from "@/domain/documentUnit"

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

const setupWithPublishedDocument = () =>
  render(PublicationDocument, {
    props: {
      documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
      publicationLog: [
        {
          type: "PUBLICATION",
          xml: '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
          statusMessages: "success",
          statusCode: "200",
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
  render(PublicationDocument, {
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
      preview: { xml: "<xml>all good</xml>", statusCode: "200" },
    },
    global: {
      plugins: [router],
    },
  })

describe("PublicationDocument:", () => {
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
      expect(
        await screen.findByText("XML Vorschau der Veröffentlichung"),
      ).toBeInTheDocument()
    })

    it("render preview error", async () => {
      render(PublicationDocument, {
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
      render(PublicationDocument, {
        props: {
          documentUnit: new DocumentUnit("123", {
            documentNumber: "foo",
          }),
          preview: undefined,
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

      expect(
        screen.queryByText("XML Vorschau der Veröffentlichung"),
      ).not.toBeInTheDocument()
    })

    it("'Rubriken bearbeiten' button links back to categories", async () => {
      render(PublicationDocument, {
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

  describe("on press 'Dokumentationseinheit veröffentlichen'", () => {
    it("publishes successfully", async () => {
      const { emitted } = setupWithAllRequiredFields()
      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(emitted().publishADocument).toBeTruthy()
    })

    it("renders error modal from backend", async () => {
      render(PublicationDocument, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
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
        global: {
          plugins: [router],
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
      render(PublicationDocument, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
        },
        global: {
          plugins: [router],
        },
      })

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
      setupWithPublishedDocument()
      expect(
        screen.getByLabelText("Letzte Veröffentlichungen"),
      ).toHaveTextContent(
        `Letzte VeröffentlichungenXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALSXML1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>`,
      )
    })

    it("without earlier published document unit", async () => {
      render(PublicationDocument, {
        props: {
          documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
        },
        global: {
          plugins: [router],
        },
      })
      expect(
        screen.getByLabelText("Letzte Veröffentlichungen"),
      ).toHaveTextContent(
        `Letzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht`,
      )
    })
  })

  it("with preview stubbing", async () => {
    render(PublicationDocument, {
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
        preview: { xml: "<xml>all good</xml>", statusCode: "200" },
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

    await fireEvent.click(screen.getByText("XML Vorschau der Veröffentlichung"))
    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("<xml>all good</xml>")
  })

  it("with stubbing", () => {
    const { container } = render(PublicationDocument, {
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
        publicationLog: [
          {
            type: "PUBLICATION",
            xml: "xml content",
            statusMessages: "success",
            statusCode: "200",
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
      `VeröffentlichenPlausibilitätsprüfungAlle Pflichtfelder sind korrekt ausgefülltDokumentationseinheit veröffentlichenLetzte VeröffentlichungenXml Email Abgabe - 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALS`,
    )

    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("XML")
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})
