import { useEffect, useState } from 'react'

/**
 * 인증번호 재전송처럼 연타를 막아야 하는 버튼용 카운트다운.
 * 서버도 같은 간격으로 막고 있고, 이건 그걸 화면에 보여주는 쪽이다.
 *
 *   const [left, start] = useCooldown(30)
 *   <button disabled={left > 0} onClick={() => { send(); start() }}>
 *     {left > 0 ? `재전송 (${left}초)` : '재전송'}
 *   </button>
 */
export function useCooldown(seconds) {
  const [left, setLeft] = useState(0)

  useEffect(() => {
    if (left <= 0) return undefined
    const timer = setTimeout(() => setLeft((value) => value - 1), 1000)
    return () => clearTimeout(timer)
  }, [left])

  return [left, () => setLeft(seconds)]
}
