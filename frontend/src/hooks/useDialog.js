import { useContext } from 'react'
import { DialogContext } from '@/components/ui/dialogContext'

/**
 * window.alert / confirm / prompt 대신 쓰는 훅.
 *
 *   const { toast, confirm, prompt } = useDialog()
 *   toast('저장했습니다.')
 *   toast(message, 'error')
 *   if (await confirm('삭제하시겠습니까?')) { ... }
 *   const reason = await prompt({ title: '신고 사유', maxLength: 100 })
 */
export function useDialog() {
  const ctx = useContext(DialogContext)
  if (!ctx) throw new Error('useDialog 는 DialogProvider 안에서만 사용할 수 있습니다.')
  return ctx
}
