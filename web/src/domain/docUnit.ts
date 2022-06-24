export type CoreData = {
  aktenzeichen?: string
  gerichtstyp?: string
  dokumenttyp?: string
  vorgang?: string
  ecli?: string
  spruchkoerper?: string
  entscheidungsdatum?: string
  gerichtssitz?: string
  rechtskraft?: string
  eingangsart?: string
  dokumentationsstelle?: string
  region?: string
}

export type Texts = {
  entscheidungsname?: string
  titelzeile?: string
  leitsatz?: string
  orientierungssatz?: string
  tenor?: string
  gruende?: string
  tatbestand?: string
  entscheidungsgruende?: string
}

export default class DocUnit implements CoreData, Texts {
  readonly id: string
  readonly uuid?: string
  readonly documentnumber?: string
  readonly creationtimestamp?: string

  readonly fileuploadtimestamp?: string
  readonly s3path?: string
  readonly filetype?: string
  readonly filename?: string
  readonly originalFileAsHTML?: string

  public aktenzeichen?: string
  public gerichtstyp?: string
  public dokumenttyp?: string
  public vorgang?: string
  public ecli?: string
  public spruchkoerper?: string
  public entscheidungsdatum?: string
  public gerichtssitz?: string
  public rechtskraft?: string
  public eingangsart?: string
  public dokumentationsstelle?: string
  public region?: string

  public entscheidungsname?: string
  public titelzeile?: string
  public leitsatz?: string
  public orientierungssatz?: string
  public tenor?: string
  public gruende?: string
  public tatbestand?: string
  public entscheidungsgruende?: string

  constructor(id: string, data: Partial<DocUnit> = {}) {
    this.id = String(id)

    let key: keyof DocUnit
    for (key in data) {
      if (data[key] === null) delete data[key]
    }
    Object.assign(this, data)
  }
  get coreData(): CoreData {
    return {
      aktenzeichen: this.aktenzeichen,
      gerichtstyp: this.gerichtstyp,
      dokumenttyp: this.dokumenttyp,
      vorgang: this.vorgang,
      ecli: this.ecli,
      spruchkoerper: this.spruchkoerper,
      entscheidungsdatum: this.entscheidungsdatum,
      gerichtssitz: this.gerichtssitz,
      rechtskraft: this.rechtskraft,
      eingangsart: this.eingangsart,
      dokumentationsstelle: this.dokumentationsstelle,
      region: this.region,
    }
  }
  get texts(): Texts {
    return {
      entscheidungsname: this.entscheidungsname,
      titelzeile: this.titelzeile,
      leitsatz: this.leitsatz,
      orientierungssatz: this.orientierungssatz,
      tenor: this.tenor,
      gruende: this.gruende,
      tatbestand: this.tatbestand,
      entscheidungsgruende: this.entscheidungsgruende,
    }
  }
  get hasFile(): boolean {
    return !!this.s3path
  }
}
