<script lang="ts" setup>
import { ref, onMounted } from "vue"
import { useRoute } from "vue-router"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import IconBadge from "@/components/IconBadge.vue"
import { User } from "@/domain/user"
import { getName } from "@/services/authService"
import FeatureToggleService from "@/services/featureToggleService"
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

const route = useRoute()
const user = ref<User>()
const fontColor = ref<string>()

onMounted(async () => {
  const nameResponse = await getName()
  if (nameResponse.data) user.value = nameResponse.data

  const featureToggle = (
    await FeatureToggleService.isEnabled("neuris.environment-test")
  ).data
  if (featureToggle) {
    fontColor.value = "green"
  } else {
    fontColor.value = "black"
  }
})
</script>

<template>
  <nav
    class="flex items-center justify-between border-y border-gray-400 py-16 pe-32 ps-16 print:hidden"
  >
    <div class="flex items-center gap-44">
      <div class="flex items-center">
        <span class="px-[1rem] text-14 font-bold leading-16">
          <span aria-hidden="true" :style="{ color: fontColor }">
            Rechtsinformationen</span
          >
          <br />
          <span aria-hidden="true">des Bundes</span>
        </span>
      </div>

      <router-link
        class="ds-label-01-reg p-8 hover:bg-yellow-500 hover:underline"
        :class="{
          underline:
            route.path.includes('caselaw') &&
            !route.path.includes('procedures'),
        }"
        :to="{ name: 'caselaw' }"
        >Suche
      </router-link>
      <router-link
        class="ds-label-01-reg p-8 hover:bg-yellow-500 hover:underline"
        :class="{ underline: route.path.includes('procedures') }"
        :to="{ name: 'caselaw-procedures' }"
        >Vorgänge
      </router-link>
    </div>

    <div v-if="user" class="grid grid-cols-[auto,1fr] gap-10">
      <IconPermIdentity />
      <div>
        <div class="ds-label-01-reg">
          <router-link :to="{ name: 'settings' }">
            <FlexContainer>
              <FlexItem class="pe-8">{{ user.name }}</FlexItem>
              <FlexItem>
                <IconBadge
                  v-if="user.documentationOffice"
                  background-color="bg-blue-300"
                  color="text-black"
                  :label="user.documentationOffice.abbreviation"
                />
              </FlexItem>
            </FlexContainer>
          </router-link>
        </div>
        <div v-if="user.documentationOffice" class="ds-label-03-reg"></div>
      </div>
    </div>
  </nav>
</template>
