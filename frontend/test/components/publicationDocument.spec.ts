import userEvent from "@testing-library/user-event"
import { render, fireEvent, screen } from "@testing-library/vue"
import PublicationDocument from "@/components/PublicationDocument.vue"

const setupWithPublishedDocument = () =>
  render(PublicationDocument, {
    props: {
      lastPublishedXmlMail: {
        xml: '<?xml version="1.0"?>\n<!DOCTYPE juris-r SYSTEM "juris-r.dtd">\n<xml>content</xml>',
        statusMessages: "success",
        statusCode: "200",
        receiverAddress: "receiver address",
        mailSubject: "mail subject",
        publishDate: "01.02.2000",
      },
    },
  })

describe("PublicationDocument:", () => {
  describe("with earlier published document unit", () => {
    it("render text", async () => {
      const { container } = setupWithPublishedDocument()
      expect(container).toHaveTextContent(
        `VeröffentlichenPlausibilitätsprüfung help Durch Klick auf Veröffentlichen wird die Plausibilitätsprüfung ausgelöst. Empfänger-E-Mail-Adresse: campaignDokumentationseinheit veröffentlichenLetzte Veröffentlichungen Letzte Veröffentlichung am 01.02.2000über E-Mail an: receiver address Betreff: mail subjectalsxml1<?xml version="1.0"?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>`
      )
    })

    describe("on press 'Dokumentationseinheit veröffentlichen'", () => {
      it("without email address", async () => {
        const { emitted } = setupWithPublishedDocument()
        const inputReceiverAddress = screen.getByLabelText(
          "Empfängeradresse E-Mail"
        )

        await userEvent.clear(inputReceiverAddress)
        await userEvent.tab()

        const publishButton = screen.getByRole("button", {
          name: "Dokumentationseinheit veröffentlichen",
        })
        await fireEvent.click(publishButton)

        expect(emitted().publishADocument).toBeFalsy()
      })

      it("with invalid email address", async () => {
        const { emitted } = setupWithPublishedDocument()
        const inputReceiverAddress = screen.getByLabelText(
          "Empfängeradresse E-Mail"
        )

        await userEvent.type(inputReceiverAddress, "test.email")
        await userEvent.tab()

        const publishButton = screen.getByRole("button", {
          name: "Dokumentationseinheit veröffentlichen",
        })
        await fireEvent.click(publishButton)

        expect(emitted().publishADocument).toBeFalsy()
      })

      it("with valid email address", async () => {
        const { emitted } = setupWithPublishedDocument()
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
        expect(emitted().publishADocument[0]).toEqual(["test.email@test.com"])
      })
    })
  })

  it("without earlier published document unit", async () => {
    const { container } = render(PublicationDocument)
    expect(container).toHaveTextContent(
      `VeröffentlichenPlausibilitätsprüfung help Durch Klick auf Veröffentlichen wird die Plausibilitätsprüfung ausgelöst. Empfänger-E-Mail-Adresse: campaignDokumentationseinheit veröffentlichenLetzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht`
    )
  })

  it("with error message", async () => {
    const { container } = render(PublicationDocument, {
      props: {
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
    })
    expect(container).toHaveTextContent(
      `VeröffentlichenPlausibilitätsprüfung keyboard_arrow_down 2 Pflichtfelder nicht befüllt error message 1error message 2Empfänger-E-Mail-Adresse: campaignDokumentationseinheit veröffentlichenerrorerror message titleerror message descriptionLetzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht`
    )
  })

  it("with stubbing", () => {
    const { container } = render(PublicationDocument, {
      global: {
        stubs: {
          CodeSnippet: {
            template: '<div data-testid="code-snippet"/>',
          },
        },
      },
      props: {
        lastPublishedXmlMail: {
          xml: "xml content",
          statusMessages: "success",
          statusCode: "200",
          receiverAddress: "receiver address",
          mailSubject: "mail subject",
          publishDate: "01.02.2000",
        },
      },
    })
    expect(container).toHaveTextContent(
      `VeröffentlichenPlausibilitätsprüfung help Durch Klick auf Veröffentlichen wird die Plausibilitätsprüfung ausgelöst. Empfänger-E-Mail-Adresse: campaignDokumentationseinheit veröffentlichenLetzte Veröffentlichungen Letzte Veröffentlichung am 01.02.2000über E-Mail an: receiver address Betreff: mail subjectals`
    )

    const codeSnippet = screen.queryByTestId("code-snippet")

    expect(codeSnippet).toBeInTheDocument()
    expect(codeSnippet?.title).toBe("xml")
    expect(codeSnippet).toHaveAttribute("xml")
    expect(codeSnippet?.getAttribute("xml")).toBe("xml content")
  })
})
