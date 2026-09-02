import { useEffect } from 'react'
import { Link, useNavigate, useParams } from 'react-router'

import {
  useDeletePostMutation,
  useGetCommentsQuery,
  useGetPostQuery,
  useIncreaseViewsMutation,
  useLikePostMutation,
} from './postApi'
import CommentSection from './CommentSection'
import { useGetMeQuery } from '@/features/auth/authApi'
import QueryState from '@/components/ui/QueryState'
import { ArrowLeftIcon, PencilIcon, ThumbsUpIcon, TrashIcon } from '@/components/ui/icons'
import { useDialog } from '@/hooks/useDialog'

// StrictMode 의 이중 마운트로 조회수가 두 번 오르지 않도록 세션 동안 기억한다.
const viewed = new Set()

export default function PostViewPage() {
  const { postId } = useParams()
  const navigate = useNavigate()
  const { toast, confirm } = useDialog()

  const { data: me } = useGetMeQuery()
  const { data: post, isLoading, error } = useGetPostQuery(postId)
  const { data: comments = [] } = useGetCommentsQuery(postId)

  const [increaseViews] = useIncreaseViewsMutation()
  const [likePost] = useLikePostMutation()
  const [deletePost] = useDeletePostMutation()

  useEffect(() => {
    if (viewed.has(postId)) return
    viewed.add(postId)
    increaseViews(postId)
  }, [postId, increaseViews])

  const isMine = Boolean(post && me && post.writer === me.id)

  const onLike = async () => {
    if (!me) {
      toast('로그인 후 이용해주세요.', 'error')
      return
    }

    try {
      await likePost(postId).unwrap()
    } catch (err) {
      toast(err.message, 'error')
    }
  }

  const onDelete = async () => {
    if (!(await confirm('게시글을 삭제하시겠습니까?'))) return

    try {
      await deletePost(postId).unwrap()
      toast('게시글이 삭제되었습니다.', 'success')
      navigate('/posts', { replace: true })
    } catch (err) {
      toast(err.message, 'error')
    }
  }

  return (
    <QueryState isLoading={isLoading} error={error} isEmpty={!post}>
      {post && (
        // 읽기 화면이라 카드로 감싸지 않고 본문 폭 자체를 좁힌다.
        <article className="mx-auto max-w-3xl">
          <div className="flex flex-wrap items-center gap-2">
            <Link to="/posts" className="btn btn-ghost btn-sm gap-1.5">
              <ArrowLeftIcon />
              목록
            </Link>

            <div className="ml-auto flex gap-0.5">
              <button type="button" className="btn btn-ghost btn-sm gap-1.5" onClick={onLike}>
                <ThumbsUpIcon />
                추천
              </button>
              {isMine && (
                <>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm gap-1.5"
                    onClick={() => navigate(`/posts/${postId}/edit`)}
                  >
                    <PencilIcon />
                    수정
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm gap-1.5 text-error"
                    onClick={onDelete}
                  >
                    <TrashIcon />
                    삭제
                  </button>
                </>
              )}
            </div>
          </div>

          <header className="mt-4 border-b border-base-300 pb-5">
            <h1 className="text-3xl font-semibold">{post.title}</h1>
            <p className="mt-3 text-sm text-base-content/55">
              {post.writer}
              <span className="mx-1.5">·</span>
              {post.postDate}
              <span className="mx-1.5">·</span>
              조회 {post.views}
              <span className="mx-1.5">·</span>
              추천 {post.likes}
              <span className="mx-1.5">·</span>
              댓글 {comments.length}
              {post.private && (
                <>
                  <span className="mx-1.5">·</span>
                  <span className="badge badge-outline badge-sm">비공개</span>
                </>
              )}
            </p>
          </header>

          {/* 서버가 CKEditor 로 작성된 HTML 을 그대로 내려준다. */}
          <div className="post-content py-6" dangerouslySetInnerHTML={{ __html: post.content }} />

          {post.tags?.length > 0 && (
            <div className="flex flex-wrap items-center gap-1.5 pb-4">
              {post.tags.map((tag) => (
                <Link
                  key={tag}
                  to={`/posts?type=tag&data=${encodeURIComponent(tag)}`}
                  className="badge badge-outline badge-sm hover:bg-base-200"
                >
                  #{tag}
                </Link>
              ))}
            </div>
          )}

          <section className="mt-10">
            <h2 className="text-sm font-medium text-base-content/60">댓글 {comments.length}</h2>
            <div className="mt-3">
              <CommentSection
                postId={postId}
                comments={comments}
                blocked={post.blockComment}
                currentUserId={me?.id}
              />
            </div>
          </section>
        </article>
      )}
    </QueryState>
  )
}
