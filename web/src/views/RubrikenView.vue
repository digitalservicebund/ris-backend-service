<script lang="ts" setup>
import DocUnitCoreData from "../components/DocUnitCoreData.vue"
import RouteHelper from "../components/RouteHelper.vue"
import TextInput from "../components/TextInput.vue"
import { useDocUnitsStore } from "../store"

const store = useDocUnitsStore()

const onClick = () => {
  // TODO
}
</script>

<template>
  <RouteHelper />
  <span v-if="store.hasSelected()">
    <v-row>
      <v-col cols="9">
        <DocUnitCoreData id="stammdaten" />
        <TextInput id="kurzUndLangtexte" />
      </v-col>
      <v-col cols="3" align="right">
        <div class="odoc-toggle" :onclick="onClick">
          <div class="odoc-toggle-text">Originaldokument</div>
          <div class="odoc-toggle-icon-wrapper">
            <v-icon class="odoc-toggle-icon"> arrow_back_ios_new </v-icon>
          </div>
        </div>
      </v-col>
    </v-row>
  </span>
</template>

<style lang="scss">
.odoc-toggle {
  background-color: $yellow500;
  border-radius: 10px;
  border: 3px solid $blue800;
  width: 200px;
  height: 65px;
  display: flex;
  justify-content: center; // align horizontal
  align-items: center; // align vertical
  margin-right: 6px;
  transform: rotate(-90deg);
  transform-origin: right;
}
.odoc-toggle-text {
  margin-left: 30px;
}
.odoc-toggle-icon-wrapper {
  background-color: $blue800;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  transform: rotate(90deg);
  translate: 25px;
}
.odoc-toggle-icon {
  color: white;
  margin-right: 9px;
  margin-top: 8px;
}
</style>
