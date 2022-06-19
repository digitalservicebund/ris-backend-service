<script lang="ts" setup>
import { useRouter } from "vue-router"
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()
const router = useRouter()

const linkStyling = (componentName: string) => {
  return router.currentRoute.value.name === componentName
    ? "side-navbar-active-link"
    : ""
}
</script>

<template>
  <v-container v-if="store.hasSelected()" fluid>
    <v-row>
      <v-col>
        <router-link
          :class="linkStyling('Rubriken')"
          :to="{
            name: 'Rubriken',
            params: { id: store.getSelected()?.id },
          }"
        >
          RUBRIKEN
        </router-link>
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sub-rubriken">
        <ul>
          <li>
            <router-link
              :to="{
                name: 'Rubriken',
                params: { id: store.getSelected()?.id },
                hash: '#stammdaten',
              }"
              >Stammdaten</router-link
            >
          </li>
          <li>
            <router-link
              :to="{
                name: 'Rubriken',
                params: { id: store.getSelected()?.id },
                hash: '#kurzUndLangtexte',
              }"
              >Kurz- & Langtexte</router-link
            >
          </li>
        </ul>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col> RECHTSZUG </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <router-link
          :class="linkStyling('Dokumente')"
          :to="{
            name: 'Dokumente',
            params: { id: store.getSelected()?.id },
          }"
          >DOKUMENTE</router-link
        >
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col> BEARBEITUNGSSTAND </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col> </v-col>
    </v-row>
    <v-row>
      <v-col>
        <router-link style="color: gray" to="/docx">docx --> html</router-link>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.sub-rubriken {
  margin-left: 20px;
}
.side-navbar-active-link {
  text-decoration: underline;
}
</style>
