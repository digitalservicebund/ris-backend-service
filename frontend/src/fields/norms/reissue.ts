import { InputField, InputType } from "@/shared/components/input/types"

export const reissue: InputField[] = [
  {
    name: "reissueNote",
    id: "reissueNote",
    type: InputType.TEXT,
    label: "Neufassungshinweis",
    inputAttributes: {
      ariaLabel: "Neufassungshinweis",
    },
  },
  {
    name: "reissueArticle",
    id: "reissueArticle",
    type: InputType.TEXT,
    label: "Bezeichnung der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Bezeichnung der Bekanntmachung",
    },
  },
  {
    name: "reissueDate",
    id: "reissueDate",
    type: InputType.DATE,
    label: "Datum der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Datum der Bekanntmachung",
      isFutureDate: true,
    },
  },
  {
    name: "reissueReference",
    id: "reissueReference",
    type: InputType.TEXT,
    label: "Fundstelle der Bekanntmachung",
    inputAttributes: {
      ariaLabel: "Fundstelle der Bekanntmachung",
    },
  },
]
