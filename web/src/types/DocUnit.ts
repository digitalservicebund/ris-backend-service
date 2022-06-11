export type DocUnit = {
  id: number
  creationtimestamp: string

  // Original file
  fileuploadtimestamp: string
  s3path: string
  filetype: string
  filename: string

  // RUBRIKEN
  // - Stammdaten
  aktenzeichen: string
  gerichtstyp: string
  dokumenttyp: string
  vorgang: string
  ecli: string
  spruchkoerper: string
  entscheidungsdatum: string
  gerichtssitz: string
  rechtskraft: string
  eingangsart: string
  dokumentationsstelle: string
  region: string

  // - Kurz- & Langtexte
  entscheidungsname: string
  titelzeile: string
  leitsatz: string
  orientierungssatz: string
  tenor: string
  gruende: string
  tatbestand: string
  entscheidungsgruende: string
}

// worth adding this dependency to make this shorter? https://stackoverflow.com/a/53995901
export function buildEmptyDocUnit(): DocUnit {
  return {
    id: -1,
    creationtimestamp: "",
    fileuploadtimestamp: "",
    s3path: "",
    filetype: "",
    filename: "",
    aktenzeichen: "",
    gerichtstyp: "",
    dokumenttyp: "",
    vorgang: "",
    ecli: "",
    spruchkoerper: "",
    entscheidungsdatum: "",
    gerichtssitz: "",
    rechtskraft: "",
    eingangsart: "",
    dokumentationsstelle: "",
    region: "",
    entscheidungsname: "",
    titelzeile: "",
    leitsatz: "",
    orientierungssatz: "",
    tenor: "",
    gruende: "",
    tatbestand: "",
    entscheidungsgruende: "",
  }
}
