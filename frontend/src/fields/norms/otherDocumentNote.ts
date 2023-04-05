import { InputField, InputType } from "@/shared/components/input/types"

export const otherDocumentNote: InputField[] = [
  {
    name: "otherDocumentNote",
    id: "otherDocumentNote",
    type: InputType.TEXT,
    label: "Sonstiger Hinweis",
    inputAttributes: {
      ariaLabel: "Sonstiger Hinweis",
    },
  },
]
