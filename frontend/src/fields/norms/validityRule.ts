import { InputField, InputType } from "@/domain"

export const validityRule: InputField[] = [
  {
    name: "validityRule",
    type: InputType.TEXT,
    label: "Gültigkeitsregelung",
    inputAttributes: {
      ariaLabel: "Gültigkeitsregelung",
    },
  },
]
