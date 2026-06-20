<template>
  <div class="rich-text-editor">
    <Editor
      v-model="content"
      :init="editorConfig"
      :disabled="readonly"
      :tinymceScriptSrc="'/tinymce_7.7.0/tinymce/js/tinymce/tinymce.min.js'"
      @onInit="handleInit"
    />
  </div>
</template>

<script lang="ts">
/// <reference types="vite/client" />

import { defineComponent, ref, watch, onMounted } from 'vue';
import Editor from '@tinymce/tinymce-vue';
import type { Editor as TinyMCEEditor } from 'tinymce';
import { ElMessage } from 'element-plus';
import type { EmitsOptions } from 'vue';
import {API_BASE_URL} from '../config/env.js';

// 编辑器配置类型
interface EditorConfig {
  language: string;
  height: number;
  menubar: string;
  plugins: string[];
  toolbar: string;
  toolbar_mode?: string;
  toolbar_sticky: boolean;
  toolbar_location?: string;
  image_advtab?: boolean;
  image_title?: boolean;
  automatic_uploads?: boolean;
  file_picker_types: string;
  content_style: string;
  images_upload_handler: (blobInfo: any, progress: any) => Promise<string>;
  autosave_ask_before_unload: boolean;
  autosave_interval: string;
  branding: boolean;
  resize: boolean;
  statusbar: boolean;
  elementpath: boolean;
  paste_data_images: boolean;
  paste_as_text: boolean;
  browser_spellcheck: boolean;
  contextmenu: string;
  setup: (editor: TinyMCEEditor) => void;
  convert_urls: boolean;
  relative_urls: boolean;
  remove_script_host: boolean;
  document_base_url: string;
  quickbars_selection_toolbar?: string;
  quickbars_insert_toolbar?: string;
  autosave_prefix?: string;
  autosave_restore_when_empty?: boolean;
  images_upload_url?: string;
  images_upload_base_path?: string;
  images_upload_credentials?: boolean;
  images_upload_max_file_size?: string;
  file_picker_callback?: (callback: Function, value: string, meta: any) => void;
  fixed_toolbar_container?: string;
  fixed_toolbar_container_target?: boolean;
  popup_css: string;
  toolbar_sticky_offset?: number;
  images_reuse_filename: boolean;
  images_dataimg_filter: (img: HTMLImageElement) => boolean;
  promotion: boolean;
  custom_domain_warning: boolean;
  upgrade_notify: boolean;
  trusted_domains: string[];
  verify_domain: boolean;
  license_key?: string;
}

interface Props {
  modelValue: string;
  isEdit?: boolean;
  readonly?: boolean;
  placeholder?: string;
}

// 定义 emit 类型
type Emits = {
  'update:modelValue': (content: string) => void;
}

export default defineComponent({
  name: 'RichTextEditor',
  components: {
    Editor
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props: Props, { emit }: { emit: (event: keyof Emits, ...args: any[]) => void }) {
    const editorRef = ref<any>(null);
    const content = ref(props.modelValue);

    // 文件上传处理函数
    const handleFileUpload = async (blobInfo: any, progress: any): Promise<string> => {
      try {
        const formData = new FormData();
        formData.append('file', blobInfo.blob());
        formData.append('type', 'images');

        const xhr = new XMLHttpRequest();
        xhr.open('POST', import.meta.env.VITE_UPLOAD_API_URL);

        return new Promise((resolve, reject) => {
          xhr.upload.onprogress = (e) => {
            if (e.lengthComputable) {
              const percentage = Math.round((e.loaded * 100) / e.total);
              progress(percentage);
            }
          };

          xhr.onload = () => {
            if (xhr.status === 200) {
              try {
                const json = JSON.parse(xhr.responseText);
                if (json.url) {
                  resolve(json.url);
                  ElMessage.success('图片上传成功');
                } else {
                  reject('上传失败：未获取到图片URL');
                }
              } catch (e) {
                reject(`上传失败: ${e}`);
              }
            } else {
              reject(`上传失败: ${xhr.status}`);
            }
          };

          xhr.onerror = () => {
            reject('上传出错，请重试');
          };

          xhr.send(formData);
        });
      } catch (error: unknown) {
        if (error instanceof Error) {
          console.error('文件上传失败:', error);
          ElMessage.error(error.message);
        } else {
          console.error('文件上传失败:', error);
          ElMessage.error('文件上传失败');
        }
        throw error;
      }
    };

    // 文件选择回调
    const handleFilePicker = (callback: Function, value: string, meta: any) => {
      const input = document.createElement('input');
      input.setAttribute('type', 'file');
      
      switch (meta.filetype) {
        case 'image':
          input.setAttribute('accept', 'image/*');
          break;
        case 'media':
          input.setAttribute('accept', 'video/*,audio/*');
          break;
        case 'file':
          break;
      }

      input.onchange = async () => {
        const file = input.files?.[0];
        if (!file) return;

        try {
          const maxSize = 100 * 1024 * 1024; // 100MB
          if (file.size > maxSize) {
            throw new Error('文件大小不能超过100MB');
          }

          const formData = new FormData();
          formData.append('file', file);
          if (meta.filetype === 'image') { formData.append('type', 'images'); } else if (meta.filetype === 'media') { const isAudio = file.type && file.type.startsWith('audio'); formData.append('type', isAudio ? 'music' : 'video'); } else { formData.append('type', 'file'); }

          const response = await fetch(import.meta.env.VITE_UPLOAD_API_URL, {
            method: 'POST',
            body: formData
          });

          if (!response.ok) {
            throw new Error('上传失败');
          }

          const data = await response.json();
          callback(data.url, { title: file.name });
        } catch (error) {
          console.error('文件上传失败:', error);
          ElMessage.error(error instanceof Error ? error.message : '文件上传失败');
        }
      };

      input.click();
    };

    const uploadUrl = import.meta.env.VITE_UPLOAD_API_URL || `${API_BASE_URL}/upload`;

    // 编辑器配置（使用 public/tinymce_7.7.0/tinymce 提供的静态资源，GPL 模式）
    const editorConfig: Partial<EditorConfig> = {
      language: 'zh_CN',
      height: 500,
      license_key: 'gpl',
      plugins: [
        'advlist',
        'autolink',
        'lists',
        'link',
        'image',
        'charmap',
        'preview',
        'anchor',
        'searchreplace',
        'visualblocks',
        'code',
        'fullscreen',
        'insertdatetime',
        'media',
        'table',
        'help',
        'wordcount',
        'codesample'
      ],
      toolbar: 'undo redo | ' +
        'formatselect | ' +
        'bold italic underline strikethrough | ' +
        'alignleft aligncenter alignright alignjustify | ' +
        'bullist numlist outdent indent | ' +
        'link image media | ' +
        'codesample | ' +
        'removeformat code fullscreen help',
      quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote',
      quickbars_insert_toolbar: 'quickimage quicktable',
      autosave_interval: '30s',
      autosave_prefix: 'tinymce-autosave-{path}{query}-{id}-',
      autosave_restore_when_empty: true,
      automatic_uploads: false,
      images_upload_url: uploadUrl,
      images_upload_base_path: '',
      images_upload_credentials: true,
      images_upload_max_file_size: '100mb',
      file_picker_types: 'file image media',
      file_picker_callback: (callback: Function, value: string, meta: any) => {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        
        switch (meta.filetype) {
          case 'image':
            input.setAttribute('accept', 'image/*');
            break;
          case 'media':
            input.setAttribute('accept', 'video/*,audio/*');
            break;
          case 'file':
            input.setAttribute('accept', '.pdf,.doc,.docx,.xls,.xlsx');
            break;
        }

        input.onchange = async () => {
          const file = input.files?.[0];
          if (!file) return;

          try {
            const maxSize = 100 * 1024 * 1024;
            if (file.size > maxSize) {
              throw new Error('文件大小不能超过100MB');
            }

            const formData = new FormData();
            formData.append('file', file);
            if (meta.filetype === 'image') { formData.append('type', 'images'); } else if (meta.filetype === 'media') { const isAudio = file.type && file.type.startsWith('audio'); formData.append('type', isAudio ? 'music' : 'video'); } else { formData.append('type', 'file'); }

            const response = await fetch(uploadUrl, {
              method: 'POST',
              body: formData
            });

            if (!response.ok) {
              throw new Error('上传失败');
            }

            const data = await response.json();
            callback(data.url, { title: file.name });
          } catch (error) {
            console.error('文件上传失败:', error);
            ElMessage.error(error instanceof Error ? error.message : '文件上传失败');
          }
        };

        input.click();
      },
      menubar: 'file edit view insert format tools table help',
      convert_urls: false,
      relative_urls: false,
      remove_script_host: false,
      document_base_url: window.location.origin,
      setup: (editor: TinyMCEEditor) => {
        editor.on('init', () => {
          if (props.modelValue) {
            editor.setContent(props.modelValue);
          }
        });

        editor.on('change', () => {
          emit('update:modelValue', editor.getContent());
        });

        editor.on('error', (e) => {
          console.error('编辑器错误:', e);
          ElMessage.error('编辑器发生错误，请刷新页面重试');
        });
      },
      content_style: `
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 14px; line-height: 1.6; padding: 20px; }
        .mce-content-body { position: relative; z-index: 1; }
      `,
      toolbar_location: 'top',
      toolbar_sticky: true,
      toolbar_sticky_offset: 0,
      popup_css: `.tox-dialog{border-radius:4px;box-shadow:0 2px 12px 0 rgba(0,0,0,0.1);}`,
      images_upload_handler: async (blobInfo, success, failure) => {
        const blobUrl = blobInfo.blobUri();
        if (blobUrl.startsWith('http://') || blobUrl.startsWith('https://')) {
          success(blobUrl);
          return;
        }
        try {
          const formData = new FormData();
          formData.append('file', blobInfo.blob(), blobInfo.filename());
          formData.append('type', 'images');
          const response = await fetch(uploadUrl, {method: 'POST', body: formData});
          if (!response.ok) { throw new Error('Upload failed'); }
          const data = await response.json();
          success(data.url);
        } catch (error) {
          console.error('文件上传失败:', error);
          failure('图片上传失败，请重试');
        }
      },
      autosave_ask_before_unload: true,
      branding: false,
      resize: true,
      statusbar: true,
      elementpath: true,
      paste_data_images: true,
      images_reuse_filename: true,
      images_dataimg_filter: function(img) {
        return !img.src.startsWith('http://') && !img.src.startsWith('https://');
      },
      promotion: false,
      custom_domain_warning: false,
      upgrade_notify: false,
      trusted_domains: ['localhost', '110.40.181.58'],
      verify_domain: false
    };

    const reset = () => {
      content.value = '';
      if (editorRef.value) {
        const editor = (editorRef.value as any).editor;
        if (editor) {
          editor.setContent('');
          editor.undoManager.clear();
          editor.setDirty(false);
          emit('update:modelValue', '');
        }
      }
    };

    watch(() => props.modelValue, (newVal) => {
      if (newVal !== content.value) {
        content.value = newVal;
        if (editorRef.value?.editor) {
          editorRef.value.editor.setContent(newVal || '');
        }
      }
    });

    const handleInit = (editor: any) => {
      editorRef.value = { editor };
      if (props.modelValue) {
        editor.setContent(props.modelValue);
      }
    };

    return {
      content,
      editorConfig,
      handleInit,
      editorRef,
      reset
    };
  }
});
</script>

<style>
:global(.tox-tinymce-aux) { z-index: 9999 !important; }
:global(.tox-dialog-wrap) { z-index: 9999 !important; }
:global(.tox-dialog__header) { z-index: 9999 !important; }
:global(.tox-dialog__footer) { z-index: 9999 !important; }
:global(.tox-collection--toolbar .tox-collection__group) { z-index: 9999 !important; }
:global(.tox-menu) { z-index: 9999 !important; }
:global(.tox-silver-sink) { z-index: 10000 !important; }
</style>

<style scoped>
.rich-text-editor {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin: 10px 0;
}

:deep(.tox-tinymce) { border: none !important; }
:deep(.tox-toolbar__primary) { background-color: #f5f7fa !important; border-bottom: 1px solid #dcdfe6 !important; padding: 4px !important; }
:deep(.tox-statusbar) { border-top: 1px solid #dcdfe6 !important; }
:deep(.tox-editor-container) { background: #fff; }
:deep(.readonly .tox-editor-container) { opacity: 0.7; pointer-events: none; }
</style>