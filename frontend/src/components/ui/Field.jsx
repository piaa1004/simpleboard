/**
 * 폼 필드의 라벨 + 컨트롤 + 검증 메시지 묶음.
 *
 * daisyUI 5 에는 `form-control` / `label-text` 클래스가 없다(v4 에서 제거됨).
 * 그 클래스에 기대면 라벨과 입력이 같은 줄에 겹치므로, 레이아웃은 여기서 Tailwind 로 직접 잡는다.
 *
 * 검증 오류는 토스트가 아니라 **필드 바로 아래**에 띄운다.
 * 토스트는 화면 구석에 뜨고 몇 초 뒤 사라져서, 어느 칸이 틀렸는지 알려주지 못한다.
 */
export default function Field({ label, hint, error, required, className = '', children }) {
  return (
    <label className={`block ${className}`}>
      {label && (
        <span className="mb-1 block text-sm font-medium">
          {label}
          {required && <span className="ml-0.5 text-error">*</span>}
        </span>
      )}

      {children}

      {error ? (
        <span className="mt-1 block text-xs text-error">{error}</span>
      ) : hint ? (
        <span className="mt-1 block text-xs text-base-content/60">{hint}</span>
      ) : null}
    </label>
  )
}

/** 체크박스 한 줄. daisyUI 5 의 `label` 은 inline-flex 라 세로 목록에 그대로 쓰면 줄이 붙는다. */
export function CheckboxField({ label, checked, onChange }) {
  return (
    <label className="flex cursor-pointer items-center gap-2 py-1">
      <input type="checkbox" className="checkbox checkbox-sm" checked={checked} onChange={onChange} />
      <span className="text-sm">{label}</span>
    </label>
  )
}
