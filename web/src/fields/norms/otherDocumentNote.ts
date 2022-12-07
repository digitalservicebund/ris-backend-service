import { InputField, InputType } from "@/domain"

export const otherDocumentNote: InputField[] = [
  {
    name: "otherDocumentNote",
    type: InputType.TEXT,
    label: "Sonstiger Hinweis",
    inputAttributes: {
      ariaLabel: "Sonstiger Hinweis",
    },
  },
]
