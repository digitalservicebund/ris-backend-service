<script lang="ts" setup>
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()
</script>

<template>
  <v-container v-if="store.hasSelected()">
    <v-row>
      <v-col>
        <router-link
          :to="{
            name: 'Rubriken',
            params: { id: store.getSelected()?.id },
          }"
        >
          RUBRIKEN
        </router-link>
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
        RECHTSZUG
        <br />
        <router-link
          :to="{
            name: 'Dokumente',
            params: { id: store.getSelected()?.id },
          }"
          >DOKUMENTE</router-link
        >
        <br />
        BEARBEITUNGSSTAND
        <br />
        <br />
        <router-link to="/docx">docx -> html</router-link>
      </v-col>
    </v-row>
  </v-container>
</template>
