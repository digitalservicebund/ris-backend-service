import { InputField, InputType } from "@/domain"

export const repeal: InputField[] = [
  {
    name: "repealNote",
    type: InputType.TEXT,
    label: "Aufhebungshinweis",
    inputAttributes: {
      ariaLabel: "Aufhebungshinweis",
    },
  },
  {
    name: "repealArticle",
    type: InputType.TEXT,
    label: "Artikel der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Artikel der Änderungsvorschrift",
    },
  },
  {
    name: "repealDate",
    type: InputType.DATE,
    label: "Datum der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Datum der Änderungsvorschrift",
      isFutureDate: true,
    },
  },
  {
    name: "repealReferences",
    type: InputType.TEXT,
    label: "Fundstellen der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Fundstellen der Änderungsvorschrift",
    },
  },
]
