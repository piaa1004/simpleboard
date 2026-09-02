import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { DialogContext } from './dialogContext'

const TOAST_MS = 3200

const TOAST_CLASS = {
  info: 'alert-info',
  success: 'alert-success',
  error: 'alert-error',
  warning: 'alert-warning',
}

/**
 * 앱 전역의 toast / confirm / prompt 를 담당한다.
 * 레거시의 window.alert · confirm · prompt 를 daisyUI 컴포넌트로 대체한다.
 */
export default function DialogProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const [modal, setModal] = useState(null)
  const [inputValue, setInputValue] = useState('')
  const seq = useRef(0)
  const resolver = useRef(null)
  const timers = useRef(new Set())

  useEffect(() => {
    const pending = timers.current
    return () => {
      pending.forEach(clearTimeout)
      pending.clear()
    }
  }, [])

  const toast = useCallback((message, type = 'info') => {
    if (!message) return
    const id = ++seq.current
    setToasts((prev) => [...prev, { id, message: String(message), type }])
    const timer = setTimeout(() => {
      setToasts((prev) => prev.filter((item) => item.id !== id))
      timers.current.delete(timer)
    }, TOAST_MS)
    timers.current.add(timer)
  }, [])

  const openModal = useCallback((next) => {
    setInputValue(next.defaultValue ?? '')
    setModal(next)
    return new Promise((resolve) => {
      resolver.current = resolve
    })
  }, [])

  const confirm = useCallback(
    (message, options = {}) =>
      openModal({
        kind: 'confirm',
        title: options.title ?? '확인',
        message,
        confirmText: options.confirmText ?? '확인',
        cancelText: options.cancelText ?? '취소',
      }),
    [openModal],
  )

  const prompt = useCallback(
    (options = {}) =>
      openModal({
        kind: 'prompt',
        title: options.title ?? '입력',
        message: options.message ?? '',
        placeholder: options.placeholder ?? '',
        maxLength: options.maxLength ?? 200,
        defaultValue: options.defaultValue ?? '',
        confirmText: options.confirmText ?? '확인',
        cancelText: options.cancelText ?? '취소',
      }),
    [openModal],
  )

  const settle = useCallback((value) => {
    setModal(null)
    setInputValue('')
    const resolve = resolver.current
    resolver.current = null
    resolve?.(value)
  }, [])

  const onSubmit = useCallback(
    (event) => {
      event.preventDefault()
      if (!modal) return
      settle(modal.kind === 'prompt' ? inputValue : true)
    },
    [modal, inputValue, settle],
  )

  const value = useMemo(() => ({ toast, confirm, prompt }), [toast, confirm, prompt])

  return (
    <DialogContext.Provider value={value}>
      {children}

      <div className="toast toast-end z-100">
        {toasts.map((item) => (
          <div key={item.id} className={`alert ${TOAST_CLASS[item.type] ?? TOAST_CLASS.info}`}>
            <span className="max-w-xs whitespace-pre-wrap break-words">{item.message}</span>
          </div>
        ))}
      </div>

      {modal && (
        <div className="modal modal-open" role="dialog">
          <form className="modal-box" onSubmit={onSubmit}>
            <h3 className="text-lg font-bold">{modal.title}</h3>
            {modal.message && <p className="py-3 whitespace-pre-wrap">{modal.message}</p>}

            {modal.kind === 'prompt' && (
              <input
                className="input input-bordered w-full"
                autoFocus
                maxLength={modal.maxLength}
                placeholder={modal.placeholder}
                value={inputValue}
                onChange={(event) => setInputValue(event.target.value)}
              />
            )}

            <div className="modal-action">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => settle(modal.kind === 'prompt' ? null : false)}
              >
                {modal.cancelText}
              </button>
              <button type="submit" className="btn btn-primary">
                {modal.confirmText}
              </button>
            </div>
          </form>
          <div
            className="modal-backdrop"
            onClick={() => settle(modal.kind === 'prompt' ? null : false)}
          />
        </div>
      )}
    </DialogContext.Provider>
  )
}
