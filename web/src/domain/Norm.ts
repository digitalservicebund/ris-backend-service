export type Article = {
  guid: string
  title: string
  marker?: string
  readonly paragraphs: Paragraph[]
}

export type Paragraph = {
  guid: string
  marker: string
  text: string
}

export default class Norm {
  readonly longTitle: string
  readonly guid: string
  readonly articles: Article[]

  constructor(longTitle: string, guid: string, articles: Article[]) {
    ;(this.longTitle = longTitle),
      (this.guid = guid),
      (this.articles = articles)
  }
}
