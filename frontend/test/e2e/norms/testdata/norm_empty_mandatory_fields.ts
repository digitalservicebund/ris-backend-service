import { NormData } from "../fixtures"

export const normEmptyMandatoryFields: NormData = {
  jurisZipFileName: "",
  articles: [
    {
      marker: "§ 1",
      title: "Allgemeiner Anwendungsbereich",
      paragraphs: [
        {
          marker: "(1)",
          text: "Dieses Gesetz regelt Mindestziele und deren Sicherstellung bei der Beschaffung bestimmter Straßenfahrzeuge und Dienstleistungen, für die diese Straßenfahrzeuge eingesetzt werden, durch öffentliche Auftraggeber und Sektorenauftraggeber.",
        },
        {
          marker: "(2)",
          text: "Soweit in diesem Gesetz oder aufgrund dieses Gesetzes nichts anderes geregelt ist, sind die allgemeinen vergaberechtlichen Vorschriften anzuwenden.",
        },
      ],
    },
  ],
  metadataSections: {
    NORM: [
      {
        OFFICIAL_LONG_TITLE: [""],
      },
    ],
  },
}
