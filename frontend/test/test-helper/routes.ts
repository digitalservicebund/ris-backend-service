const routes = [
  {
    path: "/caselaw/documentUnit/new",
    name: "new",
    component: {},
  },
  {
    path: "/",
    name: "home",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/categories",
    name: "caselaw-documentUnit-documentNumber-categories",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/preview",
    name: "caselaw-documentUnit-documentNumber-preview",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/files",
    name: "caselaw-documentUnit-documentNumber-files",
    component: {},
  },
  {
    path: "/caselaw/periodical-evaluation/:uuid",
    name: "caselaw-periodical-evaluation-uuid",
    component: {},
  },
]

export default routes
