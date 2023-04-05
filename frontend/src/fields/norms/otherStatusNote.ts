import { InputField, InputType } from "@/shared/components/input/types"

export const otherStatusNote: InputField[] = [
  {
    name: "otherStatusNote",
    id: "otherStatusNote",
    type: InputType.TEXT,
    label: "Sonstiger Hinweis",
    inputAttributes: {
      ariaLabel: "Sonstiger Hinweis",
    },
  },
]
