let handler = null

const addListeners = (cb) => {
    if (handler) return
    handler = () => {
        try {
            cb && cb()
        } catch (e) {
            console.error('[userActivityManager]', e)
        }
    }
    window.addEventListener('visibilitychange', handler)
    window.addEventListener('focus', handler)
}

const removeListeners = () => {
    if (!handler) return
    window.removeEventListener('visibilitychange', handler)
    window.removeEventListener('focus', handler)
    handler = null
}

export default {
    startListening: (cb) => addListeners(cb),
    stopListening: () => removeListeners()
}

