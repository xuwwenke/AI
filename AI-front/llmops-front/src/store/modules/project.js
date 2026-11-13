import userApi from "@/api/llmops/user";
import {getProjectToSelect} from "../../api/llmops/project";

const state = {
  currentProject: JSON.parse(localStorage.getItem('currentProject')) || { name: 'llmops' },
  currentCluster: JSON.parse(localStorage.getItem('currentCluster')) || { name: 'default' },
  projectList: []
};

const mutations = {
  SET_CURRENT_PROJECT(state, project) {
    state.currentProject = project;
    localStorage.setItem('currentProject', JSON.stringify(project));
  },
  SET_CURRENT_CLUSTER(state, cluster) {
    state.currentCluster = cluster;
    localStorage.setItem('currentCluster', JSON.stringify(cluster));
  },
  SET_PROJECT_LIST(state, list) {
    state.projectList = list;
  }
};

const actions = {
  async fetchProjectList({ commit }) {
    try {
      //const res = await getProjectToSelect({ pageNum: 1, pageSize: 1000 });
      const res = await getProjectToSelect();
      if (res && res.resultData) {
        commit('SET_PROJECT_LIST', res.resultData);
      }
    } catch (error) {
      console.error('Fetch project list failed:', error);
    }
  },
  async initProject({ commit, dispatch }, username) {
    try {
      const res = await userApi.getProjectManagersByUsername({ username });
      if (res && res.length > 0) {
        const firstProject = res[0];
        // If currentProject is default or empty, switch to the first project
        const currentProject = JSON.parse(localStorage.getItem('currentProject'));
        if (!currentProject || currentProject.name === 'llmops') {
          dispatch('switchProject', firstProject);
        }
        commit('SET_PROJECT_LIST', res);
        commit('SET_CURRENT_CLUSTER', { name: firstProject.clusterName });
      }
    } catch (error) {
      console.error('Init project failed:', error);
    }
  },
  switchProject({ commit }, project) {
    commit('SET_CURRENT_PROJECT', project);
    // If project has cluster info, update it too
    if (project.projectManager && project.projectManager.clusterName) {
      commit('SET_CURRENT_CLUSTER', { name: project.projectManager.clusterName });
    }
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
};
