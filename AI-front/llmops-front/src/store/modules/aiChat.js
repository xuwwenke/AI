import Vue from 'vue'

const state = {
  sessions: {}
}

const getters = {
  getSession: (state) => (key) => {
    return state.sessions[key] || null
  }
}

const mutations = {
  initSession(state, { key, welcomeMessage }) {
    if (!state.sessions[key]) {
      Vue.set(state.sessions, key, {
        chatMessages: [{ role: 'assistant', content: welcomeMessage, ts: Date.now() }],
        sending: false,
        streamStage: null,
        abortCtrl: null
      })
    }
  },
  pushMessage(state, { key, msg }) {
    if (state.sessions[key]) {
      state.sessions[key].chatMessages.push(msg)
    }
  },
  appendToken(state, { key, token }) {
    const session = state.sessions[key]
    if (!session) return
    const msgs = session.chatMessages
    const idx = msgs.length - 1
    const last = msgs[idx]
    if (last && last.role === 'assistant' && last.loading) {
      Vue.set(msgs, idx, Object.assign({}, last, { content: last.content + token }))
    }
  },
  setLastAiMsgField(state, { key, field, value }) {
    const session = state.sessions[key]
    if (!session) return
    const msgs = session.chatMessages
    for (let i = msgs.length - 1; i >= 0; i--) {
      if (msgs[i].role === 'assistant') {
        Vue.set(msgs, i, Object.assign({}, msgs[i], { [field]: value }))
        break
      }
    }
  },
  setSending(state, { key, value }) {
    if (state.sessions[key]) {
      state.sessions[key].sending = value
    }
  },
  setStreamStage(state, { key, value }) {
    if (state.sessions[key]) {
      state.sessions[key].streamStage = value
    }
  },
  setAbortCtrl(state, { key, ctrl }) {
    if (state.sessions[key]) {
      state.sessions[key].abortCtrl = ctrl
    }
  },
  abortSession(state, { key }) {
    const session = state.sessions[key]
    if (session && session.abortCtrl) {
      try { session.abortCtrl.abort() } catch (e) {}
    }
  },
  clearSession(state, { key, welcomeMessage }) {
    if (state.sessions[key]) {
      state.sessions[key].chatMessages = [
        { role: 'assistant', content: welcomeMessage, ts: Date.now() }
      ]
    }
  },
  loadMessages(state, { key, messages }) {
    if (state.sessions[key]) {
      state.sessions[key].chatMessages = messages
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations
}
