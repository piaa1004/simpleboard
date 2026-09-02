import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'

import { useCreatePostMutation, useGetPostQuery, useUpdatePostMutation } from './postApi'
import { useGetMeQuery } from '@/features/auth/authApi'
import RichTextEditor from '@/components/ui/RichTextEditor'
import Field, { CheckboxField } from '@/components/ui/Field'
import QueryState from '@/components/ui/QueryState'
import { useDialog } from '@/hooks/useDialog'

const MAX_TAGS = 5
const MAX_TITLE = 50
const MAX_CONTENT = 1000

export default function PostEditorPage() {
  const { postId } = useParams()
  const isEdit = Boolean(postId)
  const navigate = useNavigate()
  const { toast } = useDialog()

  const { data: me, isLoading: meLoading } = useGetMeQuery()
  const { data: post, isLoading: postLoading, error } = useGetPostQuery(postId, { skip: !isEdit })

  const [createPost, { isLoading: creating }] = useCreatePostMutation()
  const [updatePost, { isLoading: updating }] = useUpdatePostMutation()

  const [form, setForm] = useState({ title: '', content: '' })
  const [options, setOptions] = useState({ privatePost: false, blockComment: false })
  const [tags, setTags] = useState([])
  const [tagDraft, setTagDraft] = useState('')
  const [seededPostId, setSeededPostId] = useState(null)

  useEffect(() => {
    if (meLoading || me) return
    toast('로그인 후 사용해주세요.', 'error')
    navigate('/login', { replace: true })
  }, [me, meLoading, navigate, toast])

  useEffect(() => {
    if (!isEdit || !post || !me || post.writer === me.id) return
    toast('본인이 작성한 게시글만 수정할 수 있습니다.', 'error')
    navigate('/posts', { replace: true })
  }, [isEdit, post, me, navigate, toast])

  // 서버에서 받은 글을 폼 초기값으로 한 번만 옮긴다.
  // useEffect + setState 는 렌더를 한 번 더 유발하므로 렌더 중 조정 패턴을 쓴다.
  if (isEdit && post && seededPostId !== post.postId) {
    setSeededPostId(post.postId)
    setForm({ title: post.title ?? '', content: post.content ?? '' })
    setOptions({ privatePost: Boolean(post.private), blockComment: Boolean(post.blockComment) })
    setTags(post.tags ?? [])
  }

  const addTag = (event) => {
    if (event.key !== 'Enter') return
    event.preventDefault()

    const value = tagDraft.trim()
    setTagDraft('')
    if (!value) return
    if (tags.length >= MAX_TAGS) {
      toast(`태그는 최대 ${MAX_TAGS}개까지 등록할 수 있습니다.`, 'warning')
      return
    }
    if (tags.includes(value)) {
      toast('이미 동일한 태그가 있습니다.', 'warning')
      return
    }

    setTags((prev) => [...prev, value])
  }

  const submit = async (event) => {
    event.preventDefault()

    if (!form.title.trim()) {
      toast('제목을 입력해주세요.', 'error')
      return
    }
    if (!form.content.trim()) {
      toast('내용을 입력해주세요.', 'error')
      return
    }
    if (form.content.length > MAX_CONTENT) {
      toast(`내용이 ${MAX_CONTENT}자를 초과했습니다.`, 'error')
      return
    }

    const body = {
      title: form.title,
      content: form.content,
      tags,
      privatePost: options.privatePost,
      blockComment: options.blockComment,
    }

    try {
      if (isEdit) {
        await updatePost({ postId, body }).unwrap()
        toast('게시글이 수정되었습니다.', 'success')
        navigate(`/posts/${postId}`, { replace: true })
      } else {
        const response = await createPost(body).unwrap()
        toast('게시글이 등록되었습니다.', 'success')
        navigate(`/posts/${response?.data ?? ''}`, { replace: true })
      }
    } catch (err) {
      toast(err.message, 'error')
    }
  }

  return (
    <QueryState isLoading={isEdit && postLoading} error={isEdit ? error : null}>
      <form className="grid gap-4 lg:grid-cols-[1fr_20rem]" onSubmit={submit}>
        <div className="card border border-base-300 bg-base-100">
          <div className="card-body gap-2">
            <RichTextEditor
              value={form.content}
              onChange={(content) => setForm((prev) => ({ ...prev, content }))}
            />
            <p className="text-right text-xs text-base-content/60">
              {form.content.length} / {MAX_CONTENT}
            </p>
          </div>
        </div>

        <aside className="card h-fit border border-base-300 bg-base-100">
          <div className="card-body gap-4">
            <div className="flex gap-2">
              <button
                type="submit"
                className="btn btn-primary flex-1"
                disabled={creating || updating}
              >
                {(creating || updating) && <span className="loading loading-spinner loading-sm" />}
                {isEdit ? '수정' : '등록'}
              </button>
              <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
                취소
              </button>
            </div>

            <Field label={`제목 (${form.title.length} / ${MAX_TITLE})`}>
              <input
                className="input input-bordered w-full"
                maxLength={MAX_TITLE}
                placeholder="제목"
                value={form.title}
                onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
              />
            </Field>

            <div>
              <span className="mb-1 block text-sm font-medium">공개 설정</span>
              <CheckboxField
                label="비공개 글"
                checked={options.privatePost}
                onChange={(event) =>
                  setOptions((prev) => ({ ...prev, privatePost: event.target.checked }))
                }
              />
              <CheckboxField
                label="댓글 차단"
                checked={options.blockComment}
                onChange={(event) =>
                  setOptions((prev) => ({ ...prev, blockComment: event.target.checked }))
                }
              />
            </div>

            <div>
              <span className="mb-1 block text-sm font-medium">태그 (최대 {MAX_TAGS}개)</span>
              <div className="mt-1 flex flex-wrap gap-1">
                {tags.map((tag) => (
                  <span key={tag} className="badge badge-outline gap-1">
                    {tag}
                    <button
                      type="button"
                      aria-label={`${tag} 태그 제거`}
                      onClick={() => setTags((prev) => prev.filter((item) => item !== tag))}
                    >
                      ✕
                    </button>
                  </span>
                ))}
              </div>
              <input
                className="input input-bordered input-sm mt-2 w-full"
                maxLength={15}
                placeholder="태그 입력 후 Enter"
                value={tagDraft}
                onChange={(event) => setTagDraft(event.target.value)}
                onKeyDown={addTag}
              />
            </div>
          </div>
        </aside>
      </form>
    </QueryState>
  )
}
