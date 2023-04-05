import { InputField, InputType } from "@/shared/components/input/types"

export const repeal: InputField[] = [
  {
    name: "repealNote",
    id: "repealNote",
    type: InputType.TEXT,
    label: "Aufhebungshinweis",
    inputAttributes: {
      ariaLabel: "Aufhebungshinweis",
    },
  },
  {
    name: "repealArticle",
    id: "repealArticle",
    type: InputType.TEXT,
    label: "Artikel der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Artikel der Änderungsvorschrift",
    },
  },
  {
    name: "repealDate",
    id: "repealDate",
    type: InputType.DATE,
    label: "Datum der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Datum der Änderungsvorschrift",
      isFutureDate: true,
    },
  },
  {
    name: "repealReferences",
    id: "repealReferences",
    type: InputType.TEXT,
    label: "Fundstellen der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Fundstellen der Änderungsvorschrift",
    },
  },
]
