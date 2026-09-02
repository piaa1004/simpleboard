import { useState } from 'react'

import { useAddCommentMutation, useDeleteCommentMutation } from './postApi'
import { useDialog } from '@/hooks/useDialog'

export default function CommentSection({ postId, comments, blocked, currentUserId }) {
  const { toast, confirm } = useDialog()
  const [draft, setDraft] = useState('')

  const [addComment, { isLoading: adding }] = useAddCommentMutation()
  const [deleteComment] = useDeleteCommentMutation()

  const submit = async (event) => {
    event.preventDefault()

    if (!currentUserId) {
      toast('로그인 후에 이용해주세요.', 'error')
      return
    }
    if (!draft.trim()) {
      toast('입력값이 없습니다.', 'error')
      return
    }

    try {
      await addComment({ postId, content: draft }).unwrap()
      setDraft('')
    } catch (error) {
      toast(error.message, 'error')
    }
  }

  const remove = async (commentId) => {
    if (!(await confirm('댓글을 삭제하시겠습니까?'))) return

    try {
      await deleteComment({ commentId, postId }).unwrap()
    } catch (error) {
      toast(error.message, 'error')
    }
  }

  return (
    <section className="space-y-4">
      {blocked ? (
        <p className="text-sm text-base-content/60">댓글이 차단된 게시글입니다.</p>
      ) : (
        <form className="space-y-2" onSubmit={submit}>
          <textarea
            className="textarea textarea-bordered w-full"
            rows={3}
            maxLength={200}
            placeholder="댓글을 입력해주세요."
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
          />
          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary btn-sm" disabled={adding}>
              댓글 등록
            </button>
          </div>
        </form>
      )}

      {/* 회색 블록을 쌓는 대신 구분선만 둔다. */}
      <ul className="divide-y divide-base-300 border-t border-base-300">
        {comments.map((comment) => (
          <li key={comment.commentId} className="group py-4">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium">{comment.writer}</span>
              <span className="text-xs text-base-content/50">{comment.postDate}</span>

              {/* 평소엔 숨기고 마우스를 올렸을 때만 노출해 목록을 조용하게 유지한다. */}
              {comment.writer === currentUserId && (
                <span className="ml-auto opacity-0 transition-opacity group-hover:opacity-100 focus-within:opacity-100">
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs text-error"
                    onClick={() => remove(comment.commentId)}
                  >
                    삭제
                  </button>
                </span>
              )}
            </div>
            <p className="mt-2 whitespace-pre-wrap break-words text-sm">{comment.content}</p>
          </li>
        ))}
      </ul>
    </section>
  )
}
