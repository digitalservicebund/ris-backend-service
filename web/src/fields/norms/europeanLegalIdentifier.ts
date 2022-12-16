import { InputField, InputType } from "@/domain"

export const europeanLegalIdentifier: InputField[] = [
  {
    name: "europeanLegalIdentifier",
    type: InputType.TEXT,
    label: "ELI",
    inputAttributes: {
      ariaLabel: "ELI",
    },
  },
]
