import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { describe, vi } from "vitest"
import { ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import HandoverDuplicateCheckView from "@/components/HandoverDuplicateCheckView.vue"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
} from "@/domain/managementData"
import { PublicationState } from "@/domain/publicationStatus"
import routes from "~pages"

describe("HandoverDocumentDuplicateCheckView:", () => {
  test("without duplicates", async () => {
    await renderComponent([])

    expect(screen.getByText("Dublettenprüfung")).toBeInTheDocument()
    expect(
      screen.getByText("Es besteht kein Dublettenverdacht."),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Es besteht Dublettenverdacht."),
    ).not.toBeInTheDocument()
    expect(screen.queryByText("Dokumentationseinheit")).not.toBeInTheDocument()
    expect(
      screen.queryByRole("button", {
        name: "Dublettenwarnung prüfen",
      }),
    ).not.toBeInTheDocument()
  })

  test("with pending unpublished duplicate", async () => {
    const duplicate: DuplicateRelation = {
      documentNumber: "documentNumber",
      fileNumber: "AZ ABC",
      courtLabel: "AG Aachen",
      decisionDate: "2025-01-15",
      documentType: "Beschluss",
      status: DuplicateRelationStatus.PENDING,
      isJdvDuplicateCheckActive: true,
      publicationStatus: PublicationState.UNPUBLISHED,
    }
    const pendingDuplicates = [duplicate]
    await renderComponent(pendingDuplicates)

    expect(screen.getByText("Dublettenprüfung")).toBeInTheDocument()
    expect(
      screen.getByText("Es besteht Dublettenverdacht."),
    ).toBeInTheDocument()
    expect(screen.getByText("Dokumentationseinheit")).toBeInTheDocument()
    const decisionSummary = screen.getByTestId(
      "decision-summary-documentNumber",
    )
    expect(decisionSummary).toHaveTextContent(
      "AG Aachen, 15.01.2025, AZ ABC, Beschluss, Unveröffentlicht",
    )
    expect(
      screen.getByRole("button", {
        name: "Dublettenwarnung prüfen",
      }),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Es besteht kein Dublettenverdacht."),
    ).not.toBeInTheDocument()
  })

  test("with pending unpublished and published duplicates", async () => {
    const unpublishedDuplicate: DuplicateRelation = {
      documentNumber: "documentNumber",
      status: DuplicateRelationStatus.PENDING,
      isJdvDuplicateCheckActive: true,
      publicationStatus: PublicationState.UNPUBLISHED,
    }
    const publishedDuplicate: DuplicateRelation = {
      documentNumber: "documentNumber",
      status: DuplicateRelationStatus.PENDING,
      isJdvDuplicateCheckActive: true,
      publicationStatus: PublicationState.PUBLISHED,
    }
    await renderComponent([publishedDuplicate, unpublishedDuplicate])

    expect(screen.getByText("Dublettenprüfung")).toBeInTheDocument()
    expect(
      screen.getByText("Es besteht Dublettenverdacht."),
    ).toBeInTheDocument()
    expect(screen.getByText("Dokumentationseinheiten")).toBeInTheDocument()
    expect(screen.getByText("Unveröffentlicht")).toBeInTheDocument()
    expect(screen.getByText("Veröffentlicht")).toBeInTheDocument()
    expect(
      screen.getByRole("button", {
        name: "Dublettenwarnung prüfen",
      }),
    ).toBeInTheDocument()
    expect(
      screen.queryByText("Es besteht kein Dublettenverdacht."),
    ).not.toBeInTheDocument()
  })

  test("with external user the button should be hidden", async () => {
    isInternalUser.value = false
    const duplicate: DuplicateRelation = {
      documentNumber: "documentNumber",
      status: DuplicateRelationStatus.PENDING,
      isJdvDuplicateCheckActive: true,
      publicationStatus: PublicationState.PUBLISHED,
    }
    await renderComponent([duplicate])

    expect(screen.getByText("Dublettenprüfung")).toBeInTheDocument()
    expect(
      screen.getByText("Es besteht Dublettenverdacht."),
    ).toBeInTheDocument()
    expect(screen.getByText("Dokumentationseinheit")).toBeInTheDocument()
    expect(
      screen.queryByRole("button", {
        name: "Dublettenwarnung prüfen",
      }),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByText("Es besteht kein Dublettenverdacht."),
    ).not.toBeInTheDocument()
  })
})

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const isInternalUser = ref(true)
vi.mock("@/composables/useInternalUser", () => {
  return {
    useInternalUser: () => isInternalUser,
  }
})

async function renderComponent(pendingDuplicates: DuplicateRelation[]) {
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  render(HandoverDuplicateCheckView, {
    props: { pendingDuplicates },
    global: {
      plugins: [[createTestingPinia()], [router]],
    },
  })
}
