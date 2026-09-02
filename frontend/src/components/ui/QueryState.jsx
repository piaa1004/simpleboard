/** 목록/상세 화면의 로딩·에러·빈 상태를 한 곳에서 처리한다. */
export default function QueryState({ isLoading, error, isEmpty, emptyText = '데이터가 없습니다.', children }) {
  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="alert alert-error my-6">
        <span>{error.message ?? '데이터를 불러오지 못했습니다.'}</span>
      </div>
    )
  }

  if (isEmpty) {
    return <div className="py-16 text-center text-base-content/60">{emptyText}</div>
  }

  return children
}
