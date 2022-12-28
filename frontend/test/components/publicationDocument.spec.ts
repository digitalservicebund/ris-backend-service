import userEvent from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import PublicationDocument from "@/components/PublicationDocument.vue"
import DocumentUnit from "@/domain/documentUnit"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "",
      name: "caselaw-documentUnit-:documentNumber-categories",
      component: {},
    },
  ],
})

const setupWithPublishedDocument = () =>
  render(PublicationDocument, {
    props: {
      documentUnit: new DocumentUnit("123", { documentNumber: "foo" }),
      lastPublishedXmlMail: {
        xml: '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
        statusMessages: "success",
        statusCode: "200",
        receiverAddress: "receiver address",
        mailSubject: "mail subject",
        publishDate: "01.02.2000",
      },
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
          category: "category",
        },
      }),
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
        screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt")
      ).toBeInTheDocument()
      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:"
        )
      ).not.toBeInTheDocument()
    })

    it("with required fields missing", async () => {
      render(PublicationDocument, {
        props: {
          documentUnit: new DocumentUnit("123", {
            documentNumber: "foo",
          }),
        },
        global: {
          plugins: [router],
        },
      })
      expect(
        await screen.findByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:"
        )
      ).toBeInTheDocument()

      expect(screen.getByText("Aktenzeichen")).toBeInTheDocument()
      expect(screen.getByText("Gericht")).toBeInTheDocument()
      expect(screen.getByText("Entscheidungsdatum")).toBeInTheDocument()
      expect(screen.getByText("Rechtskraft")).toBeInTheDocument()
      expect(screen.getByText("Dokumenttyp")).toBeInTheDocument()
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
        await screen.findByLabelText("Rubriken bearbeiten")
      ).toBeInTheDocument()

      await userEvent.click(screen.getByLabelText("Rubriken bearbeiten"))
      expect(router.currentRoute.value.name).toBe(
        "caselaw-documentUnit-:documentNumber-categories"
      )
    })
  })

  describe("on press 'Dokumentationseinheit veröffentlichen'", () => {
    it("with default email address", async () => {
      const { emitted } = setupWithAllRequiredFields()
      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(emitted().publishADocument).toBeTruthy()
      expect(emitted().publishADocument[0]).toEqual(["dokmbx@juris.de"])
    })

    it("with invalid email address", async () => {
      const { emitted } = setupWithAllRequiredFields()
      const inputReceiverAddress = screen.getByLabelText(
        "Empfängeradresse E-Mail"
      )

      await userEvent.clear(inputReceiverAddress)
      await userEvent.type(inputReceiverAddress, "test.email")
      await userEvent.tab()

      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(emitted().publishADocument).toBeFalsy()
    })

    it("with valid email address", async () => {
      const { emitted } = setupWithAllRequiredFields()
      const inputReceiverAddress = screen.getByLabelText(
        "Empfängeradresse E-Mail"
      )

      await userEvent.type(inputReceiverAddress, "test.email@test.com")
      await userEvent.tab()

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
            publishDate: undefined,
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
        screen.queryByLabelText("Erfolg der Veröffentlichung")
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei Veröffentlichung")
      ).toHaveTextContent(`errorerror message titleerror message description`)
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

      const inputReceiverAddress = screen.getByLabelText(
        "Empfängeradresse E-Mail"
      )

      await userEvent.type(inputReceiverAddress, "test.email@test.com")
      await userEvent.tab()

      const publishButton = screen.getByRole("button", {
        name: "Dokumentationseinheit veröffentlichen",
      })
      await fireEvent.click(publishButton)

      expect(
        screen.queryByLabelText("Erfolg der Veröffentlichung")
      ).not.toBeInTheDocument()
      expect(
        screen.getByLabelText("Fehler bei Veröffentlichung")
      ).toHaveTextContent(
        `errorEs sind noch nicht alle Pflichtfelder befüllt.Die Dokumentationseinheit kann nicht veröffentlicht werden.`
      )
    })
  })

  describe("last published xml", () => {
    it("with earlier published document unit", async () => {
      setupWithPublishedDocument()
      expect(
        screen.getByLabelText("Letzte Veröffentlichungen")
      ).toHaveTextContent(
        `Letzte Veröffentlichungen Letzte Veröffentlichung am 01\.02\.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALSXML1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content<\/xml>`
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
        screen.getByLabelText("Letzte Veröffentlichungen")
      ).toHaveTextContent(
        `Letzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht`
      )
    })
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
            category: "category",
          },
        }),
        lastPublishedXmlMail: {
          xml: "xml content",
          statusMessages: "success",
          statusCode: "200",
          receiverAddress: "receiver address",
          mailSubject: "mail subject",
          publishDate: "01.02.2000",
        },
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
      `Veröffentlichen1. Plausibilitätsprüfung check Alle Pflichtfelder sind korrekt ausgefüllt2. Empfänger der Export-EmailEmpfänger-E-Mail-Adresse: campaignDokumentationseinheit veröffentlichenLetzte Veröffentlichungen Letzte Veröffentlichung am 01.02.2000ÜBERE-Mail an: receiver address Betreff: mail subjectALS`
    )

    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("XML")
    expect(codeSnippet).toHaveAttribute("XML")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})
