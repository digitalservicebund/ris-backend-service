import { InputField, InputType } from "@/shared/components/input/types"

export const citationDate: InputField[] = [
  {
    name: "citationDate",
    id: "citationDate",
    type: InputType.DATE,
    label: "Zitierdatum",
    inputAttributes: {
      ariaLabel: "Zitierdatum",
      isFutureDate: true,
    },
  },
]
