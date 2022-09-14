import { render, RenderResult, fireEvent } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import PublicationDocument from "@/components/PublicationDocument.vue"

describe("PublicationDocument:", () => {
  const vuetify = createVuetify({ components, directives })
  let renderResult: RenderResult

  describe("with earlier published document unit", () => {
    beforeEach(() => {
      renderResult = render(PublicationDocument, {
        global: { plugins: [vuetify] },
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
    })

    it("render text", async () => {
      expect(renderResult.container.textContent).match(
        new RegExp(
          "VeröffentlichenPlausibilitätsprüfung help\\s{2}Durch Klick auf Veröffentlichen wird " +
            "die Plausibilitätsprüfung ausgelöst.\\s{2}Empfänger-E-Mail-Adresse: " +
            "Dokumentationseinheit veröffentlichenLetzte Veröffentlichungen Letzte Veröffentlichung am " +
            "01.02.2000über E-Mail an: receiver address Betreff: mail subjectalsxml1" +
            '<\\?xml version="1.0"\\?>2<!DOCTYPE juris-r SYSTEM "juris-r.dtd">3<xml>content</xml>'
        )
      )
    })

    describe("on press 'Dokumentationseinheit veröffentlichen'", () => {
      it("without email address", async () => {
        const publishButton = renderResult.getByRole("button", {
          name: "Dokumentationseinheit veröffentlichen",
        })
        await fireEvent.click(publishButton)

        expect(renderResult.emitted().publishADocument).toBeFalsy()
      })

      it("with invalid email address", async () => {
        const inputReceiverAddress = renderResult.getByLabelText(
          "Empfängeradresse E-Mail"
        )
        await fireEvent.update(inputReceiverAddress, "test-email")

        const publishButton = renderResult.getByRole("button", {
          name: "Dokumentationseinheit veröffentlichen",
        })
        await fireEvent.click(publishButton)

        expect(renderResult.emitted().publishADocument).toBeFalsy()
      })

      it("with valid email address", async () => {
        const inputReceiverAddress = renderResult.getByLabelText(
          "Empfängeradresse E-Mail"
        )
        await fireEvent.update(inputReceiverAddress, "test.email@test.com")

        const publishButton = renderResult.getByRole("button", {
          name: "Dokumentationseinheit veröffentlichen",
        })
        await fireEvent.click(publishButton)

        expect(renderResult.emitted().publishADocument).toBeTruthy()
      })
    })
  })

  it("without earlier published document unit", async () => {
    renderResult = render(PublicationDocument, {
      global: { plugins: [vuetify] },
    })

    expect(renderResult.container.textContent).match(
      new RegExp(
        "VeröffentlichenPlausibilitätsprüfung help\\s{2}Durch Klick auf Veröffentlichen " +
          "wird die Plausibilitätsprüfung ausgelöst.\\s{2}Empfänger-E-Mail-Adresse: Dokumentationseinheit " +
          "veröffentlichenLetzte Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht "
      )
    )
  })

  it("with error message", async () => {
    renderResult = render(PublicationDocument, {
      global: { plugins: [vuetify] },
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

    expect(renderResult.container.textContent).match(
      new RegExp(
        "VeröffentlichenPlausibilitätsprüfung keyboard_arrow_down 2 Pflichtfelder nicht befüllt " +
          "error message 1error message 2 Empfänger-E-Mail-Adresse: Dokumentationseinheit " +
          "veröffentlichenerror message titleerror message descriptionLetzte " +
          "Veröffentlichungen Diese Dokumentationseinheit wurde bisher nicht veröffentlicht"
      )
    )
  })

  it("with stubbing", () => {
    renderResult = render(PublicationDocument, {
      global: {
        plugins: [vuetify],
        stubs: {
          CodeSnippet: true,
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

    expect(renderResult.container.textContent).match(
      new RegExp(
        "VeröffentlichenPlausibilitätsprüfung help\\s{2}Durch Klick auf Veröffentlichen wird die " +
          "Plausibilitätsprüfung ausgelöst.\\s{2}Empfänger-E-Mail-Adresse: Dokumentationseinheit " +
          "veröffentlichenLetzte Veröffentlichungen Letzte Veröffentlichung am 01.02.2000über " +
          "E-Mail an: receiver address Betreff: mail subjectals"
      )
    )
    expect(renderResult.html()).match(
      /<code-snippet-stub xml="xml content" title="xml"><\/code-snippet-stub>/
    )
  })
})
