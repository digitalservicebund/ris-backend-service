import Reference from "@/domain/reference"

export enum SourceValue {
  UnaufgefordertesOriginal = "O",
  AngefordertesOriginal = "A",
  Zeitschrift = "Z",
  Email = "E",
  LaenderEuGH = "L",
  Sonstige = "S",
}

export type Source = {
  value?: SourceValue
  reference?: Reference
  sourceRawValue?: string
}
