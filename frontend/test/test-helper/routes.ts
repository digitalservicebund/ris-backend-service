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
    path: "/caselaw/pendingProceeding/:documentNumber/categories",
    name: "caselaw-pending-proceeding-documentNumber-categories",
    component: {},
  },
  {
    path: "/caselaw/pendingProceeding/new",
    name: "caselaw-pending-proceeding-new",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/preview",
    name: "caselaw-documentUnit-documentNumber-preview",
    component: {},
  },
  {
    path: "/caselaw/pendingProceeding/:documentNumber/preview",
    name: "caselaw-pending-proceeding-documentNumber-preview",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/files",
    name: "caselaw-documentUnit-documentNumber-files",
    component: {},
  },
  {
    path: "/caselaw/documentUnit/:documentNumber/managementdata",
    name: "caselaw-documentUnit-documentNumber-managementdata",
    component: {},
  },
  {
    path: "/caselaw/periodical-evaluation/:editionId",
    name: "caselaw-periodical-evaluation-editionId",
    component: {},
  },
  {
    path: "/caselaw/periodical-evaluation/:editionId/references",
    name: "caselaw-periodical-evaluation-editionId-references",
    component: {},
  },
  {
    path: "/caselaw/periodical-evaluation/:editionId/edition",
    name: "caselaw-periodical-evaluation-editionId-edition",
    component: {},
  },
  {
    path: "/caselaw/periodical-evaluation/:editionId/handover",
    name: "caselaw-periodical-evaluation-editionId-handover",
    component: {},
  },
  {
    path: "/caselaw/inbox",
    name: "caselaw-inbox",
    component: {},
  },
]

export default routes
