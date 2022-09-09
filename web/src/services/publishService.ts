import api from "./api"
import XmlMail from "@/domain/xmlMail"

export default {
  async publishADocument(
    docUnitUuid: string,
    receiverEmail: string
  ): Promise<{
    xmlMail?: XmlMail
    errorMessage?: { title: string; description: string }
  }> {
    return api
      .put<string, XmlMail>(
        `docunits/${docUnitUuid}/publish`,
        {
          headers: { "Content-Type": "text/plain" },
        },
        receiverEmail
      )
      .then((response) => {
        if (response.status === 200) {
          if (response.data.statusCode === "200") {
            return { xmlMail: response.data }
          } else {
            return {
              xmlMail: response.data,
              errorMessage: {
                title: "Leider ist ein Fehler aufgetreten.",
                description:
                  "Die Dokumentationseinheit kann nicht veröffentlich werden.",
              },
            }
          }
        }
        return {
          errorMessage: {
            title: "Fehler beim E-Mail-Versand",
            description:
              "Die Dokumentationseinheit konnte nicht per E-Mail versendet werden.",
          },
        }
      })
      .catch(() => {
        return {
          errorMessage: {
            title: "Fehler beim E-Mail-Versand",
            description:
              "Die Dokumentationseinheit konnte nicht per E-Mail versendet werden.",
          },
        }
      })
  },
  async getLastPublishedXML(docUnitUuid: string): Promise<{
    xmlMail?: XmlMail
    errorMessage?: { title: string; description: string }
  }> {
    return api
      .get<XmlMail>(`docunits/${docUnitUuid}/publish`)
      .then((response) => {
        if (response.status === 200) {
          return {
            xmlMail: response.data,
          }
        } else {
          return {
            errorMessage: {
              title: "Fehler beim Laden der letzten Veröffentlichung",
              description:
                "Die Daten der letzten Veröffentlichung konnten nicht geladen werden.",
            },
          }
        }
      })
      .catch(() => {
        return {
          errorMessage: {
            title: "Fehler beim Laden der letzten Veröffentlichung",
            description:
              "Die Daten der letzten Veröffentlichung konnten nicht geladen werden.",
          },
        }
      })
  },
}
