import { InputField, InputType } from "@/shared/components/input/types"

export const documentStatus: InputField[] = [
  {
    name: "documentStatusWorkNote",
    id: "documentStatusWorkNote",
    type: InputType.TEXT,
    label: "Bearbeitungshinweis",
    inputAttributes: {
      ariaLabel: "Bearbeitungshinweis",
    },
  },
  {
    name: "documentStatusDescription",
    id: "documentStatusDescription",
    type: InputType.TEXT,
    label: "Bezeichnung der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Bezeichnung der Änderungsvorschrift",
    },
  },
  {
    name: "documentStatusDate",
    id: "documentStatusDate",
    type: InputType.DATE,
    label: "Datum der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Datum der Änderungsvorschrift",
      isFutureDate: true,
    },
  },
  {
    name: "documentStatusReference",
    id: "documentStatusReference",
    type: InputType.TEXT,
    label: "Fundstelle der Änderungsvorschrift",
    inputAttributes: {
      ariaLabel: "Fundstelle der Änderungsvorschrift",
    },
  },
  {
    name: "documentStatusEntryIntoForceDate",
    id: "documentStatusEntryIntoForceDate",
    type: InputType.DATE,
    label: "Datum des Inkrafttretens der Änderung",
    inputAttributes: {
      ariaLabel: "Datum des Inkrafttretens der Änderung",
      isFutureDate: true,
    },
  },
  {
    name: "documentStatusProof",
    id: "documentStatusProof",
    type: InputType.TEXT,
    label: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
    inputAttributes: {
      ariaLabel: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
    },
  },
]
