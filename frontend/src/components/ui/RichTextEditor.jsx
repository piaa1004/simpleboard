import { CKEditor } from '@ckeditor/ckeditor5-react'
import {
  Autoformat,
  BlockQuote,
  Bold,
  ClassicEditor,
  Essentials,
  Heading,
  Italic,
  Link,
  List,
  Paragraph,
  Underline,
} from 'ckeditor5'
import 'ckeditor5/ckeditor5.css'

const CONFIG = {
  // 자체 호스팅 GPL 사용임을 명시해야 에디터가 뜬다. (CKEditor 5 v46+)
  licenseKey: 'GPL',
  plugins: [
    Essentials,
    Paragraph,
    Autoformat,
    Heading,
    Bold,
    Italic,
    Underline,
    Link,
    List,
    BlockQuote,
  ],
  toolbar: [
    'undo',
    'redo',
    '|',
    'heading',
    '|',
    'bold',
    'italic',
    'underline',
    '|',
    'link',
    'bulletedList',
    'numberedList',
    'blockQuote',
  ],
}

export default function RichTextEditor({ value, onChange }) {
  return (
    <div className="ck-editor-shell overflow-hidden rounded-box border border-base-300">
      <CKEditor
        editor={ClassicEditor}
        config={CONFIG}
        data={value}
        onChange={(event, editor) => onChange(editor.getData())}
      />
    </div>
  )
}
