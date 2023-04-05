import { InputField, InputType } from "@/shared/components/input/types"

export const status: InputField[] = [
  {
    name: "statusNote",
    id: "statusNote",
    type: InputType.TEXT,
    label: "Änderungshinweis",
    inputAttributes: {
      ariaLabel: "Änderungshinweis",
    },
  },
  {
    name: "statusDescription",
    id: "statusDescription",
    type: InputType.TEXT,
    label: "Bezeichnung der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Bezeichnung der Änderungsvorschrift",
    },
  },
  {
    name: "statusDate",
    id: "statusDate",
    type: InputType.DATE,
    label: "Datum der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Datum der Änderungsvorschrift",
      isFutureDate: true,
    },
  },
  {
    name: "statusReference",
    id: "statusReference",
    type: InputType.TEXT,
    label: "Fundstellen der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Fundstellen der Änderungsvorschrift",
    },
  },
]
