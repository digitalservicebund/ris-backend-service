export type DocUnit = {
  id: number

  // Original file
  s3path: string
  filetype: string
  filename: string

  // Stammdaten
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

  // Rubrikenfelder
  tenor: string
  gruende: string
  tatbestand: string
  entscheidungsgruende: string
  abweichendemeinung: string
  sonstigerlangtext: string
  gliederung: string
  berichtigung: string
}

// worth adding this dependency to make this shorter? https://stackoverflow.com/a/53995901 TODO
export function buildEmptyDocUnit(): DocUnit {
  return {
    id: -1,
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
    tenor: "",
    gruende: "",
    tatbestand: "",
    entscheidungsgruende: "",
    abweichendemeinung: "",
    sonstigerlangtext: "",
    gliederung: "",
    berichtigung: "",
  }
}
