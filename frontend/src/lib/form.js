/**
 * 검증 오류가 있을 때 입력 테두리를 error 색으로 바꾼다.
 * daisyUI 버전에 따라 사라질 수 있는 상태 클래스 대신 Tailwind 유틸리티를 쓴다.
 */
export const inputClass = (error) => `input input-bordered w-full${error ? ' border-error' : ''}`
