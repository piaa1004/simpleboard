const WINDOW = 2

/** 1-based 페이지 번호를 다루는 공용 페이지네이션. 서버로 나갈 때 0-based 로 바꾸는 건 호출부의 몫이다. */
export default function Pagination({ page, totalCount, pageSize, onChange }) {
  const totalPages = Math.max(1, Math.ceil((totalCount || 0) / pageSize))
  if (totalPages <= 1) return null

  const pages = []
  const push = (value) => {
    if (pages.at(-1) !== value) pages.push(value)
  }

  push(1)
  if (page - WINDOW > 2) push('…')
  for (let i = Math.max(2, page - WINDOW); i <= Math.min(totalPages - 1, page + WINDOW); i += 1) {
    push(i)
  }
  if (page + WINDOW < totalPages - 1) push('…')
  if (totalPages > 1) push(totalPages)

  const go = (next) => {
    const clamped = Math.min(totalPages, Math.max(1, next))
    if (clamped !== page) onChange(clamped)
  }

  return (
    <div className="join">
      <button
        type="button"
        className="join-item btn btn-ghost"
        disabled={page <= 1}
        onClick={() => go(page - 1)}
      >
        «
      </button>

      {pages.map((item, index) =>
        item === '…' ? (
          <span
            key={`gap-${index}`}
            className="join-item flex items-center px-2 text-sm text-base-content/40"
          >
            …
          </span>
        ) : (
          <button
            type="button"
            key={item}
            className={`join-item btn ${item === page ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => go(item)}
          >
            {item}
          </button>
        ),
      )}

      <button
        type="button"
        className="join-item btn btn-ghost"
        disabled={page >= totalPages}
        onClick={() => go(page + 1)}
      >
        »
      </button>
    </div>
  )
}
