// 门店预约相关的状态管理
const state = {
  // 当前选中的门店
  currentDept: null
}

const mutations = {
  SET_CURRENT_DEPT: (state, dept) => {
    state.currentDept = dept
    // 同时保存到 localStorage，防止刷新丢失
    if (dept) {
      localStorage.setItem('currentDept', JSON.stringify(dept))
    } else {
      localStorage.removeItem('currentDept')
    }
  }
}

const actions = {
  // 设置当前门店
  setCurrentDept({ commit }, dept) {
    commit('SET_CURRENT_DEPT', dept)
  },

  // 清除当前门店
  clearCurrentDept({ commit }) {
    commit('SET_CURRENT_DEPT', null)
  }
}

const getters = {
  // 获取当前门店，优先从state取，没有则从localStorage取
  currentDept: state => {
    if (state.currentDept) {
      return state.currentDept
    }
    // 尝试从localStorage恢复
    const saved = localStorage.getItem('currentDept')
    if (saved) {
      state.currentDept = JSON.parse(saved)
      return state.currentDept
    }
    return null
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
