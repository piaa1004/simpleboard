import { Link, useSearchParams } from 'react-router'

import { useGetPostCountQuery, useGetPostsQuery } from './postApi'
import Pagination from '@/components/ui/Pagination'
import QueryState from '@/components/ui/QueryState'
import { ChatBubbleIcon } from '@/components/ui/icons'

const PAGE_SIZE = 10

export default function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const mode = searchParams.get('type') ?? 'normal'
  const keyword = searchParams.get('data') ?? ''
  const page = Number(searchParams.get('page')) || 1

  const { data: posts = [], isLoading, error } = useGetPostsQuery({
    mode,
    keyword,
    page: page - 1, // 서버는 0부터 센다.
    size: PAGE_SIZE,
  })
  const { data: totalCount = 0 } = useGetPostCountQuery({ mode, keyword })

  const search = (event) => {
    if (event.key !== 'Enter') return
    const value = event.target.value.trim()
    setSearchParams(value ? { type: 'search', data: value, page: '1' } : {})
  }

  const goToPage = (next) => {
    const params = { page: String(next) }
    if (mode !== 'normal') Object.assign(params, { type: mode, data: keyword })
    setSearchParams(params)
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold">게시판</h1>
          {mode !== 'normal' && (
            <p className="mt-1 text-sm text-base-content/60">
              {mode === 'tag' ? `#${keyword}` : `"${keyword}"`} 검색 결과 {totalCount}건
              <button
                type="button"
                className="link link-hover ml-2"
                onClick={() => setSearchParams({})}
              >
                전체 보기
              </button>
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <input
            className="input input-bordered input-sm w-56"
            placeholder="제목 및 내용 검색"
            defaultValue={mode === 'search' ? keyword : ''}
            onKeyDown={search}
          />
          <Link to="/posts/new" className="btn btn-primary btn-sm">
            새 글
          </Link>
        </div>
      </div>

      <QueryState
        isLoading={isLoading}
        error={error}
        isEmpty={posts.length === 0}
        emptyText="게시글이 없습니다."
      >
        {/* 제목을 앞세우고 나머지 정보는 회색 메타 한 줄로 내린다. */}
        <ul className="divide-y divide-base-300 border-y border-base-300">
          {posts.map((post) => (
            <li key={post.numbers}>
              <Link
                to={`/posts/${post.numbers}`}
                className="group flex items-baseline gap-3 py-4 transition-colors hover:bg-base-200/60"
              >
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-[1.0625rem] font-medium group-hover:underline">
                    {post.title}
                  </span>
                  <span className="mt-1.5 block text-sm text-base-content/55">
                    {post.writer}
                    <span className="mx-1.5">·</span>
                    {post.postDate}
                    <span className="mx-1.5">·</span>
                    조회 {post.views}
                    <span className="mx-1.5">·</span>
                    추천 {post.likes}
                  </span>
                </span>

                {post.count ? (
                  <span className="flex shrink-0 items-center gap-1 text-sm text-base-content/45">
                    <ChatBubbleIcon width={14} height={14} />
                    <span className="tabular-nums">{post.count}</span>
                  </span>
                ) : null}
              </Link>
            </li>
          ))}
        </ul>
      </QueryState>

      <div className="flex justify-center">
        <Pagination page={page} totalCount={totalCount} pageSize={PAGE_SIZE} onChange={goToPage} />
      </div>
    </section>
  )
}
