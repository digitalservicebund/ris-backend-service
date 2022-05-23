export type DocUnit = {
  id: number

  // Original file
  s3path: string
  filetype: string

  // Stammdaten
  aktenzeichen: string
  entscheidungsdatum: string
  gericht: string
  dokumenttyp: string
  gerichtssitz: string
  spruchkoerper: string
  vorgang: string
  eclinummer: string
  eingangsart: string

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
