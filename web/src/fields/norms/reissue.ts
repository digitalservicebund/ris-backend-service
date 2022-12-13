import { InputField, InputType } from "@/domain"

export const reissue: InputField[] = [
  {
    name: "reissueNote",
    type: InputType.TEXT,
    label: "Neufassungshinweis",
    inputAttributes: {
      ariaLabel: "Neufassungshinweis",
    },
  },
  {
    name: "reissueArticle",
    type: InputType.TEXT,
    label: "Bezeichnung der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Bezeichnung der Bekanntmachung",
    },
  },
  {
    name: "reissueDate",
    type: InputType.DATE,
    label: "Datum der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Datum der Bekanntmachung",
      isFutureDate: true,
    },
  },
  {
    name: "reissueReference",
    type: InputType.TEXT,
    label: "Fundstelle der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Fundstelle der Bekanntmachung",
    },
  },
]
