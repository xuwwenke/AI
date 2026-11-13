<template>
  <el-dialog :title="knowledgeBaseId ? '管理知识库标签' : '标签管理'" :visible="visible" width="620px" custom-class="knowledge-tags-dialog" append-to-body :close-on-click-modal="false" :close-on-press-escape="!busy" :show-close="!busy" :before-close="closeDialog" @update:visible="$emit('update:visible', $event)">
    <div class="tags-manager">
      <p v-if="knowledgeBaseId" class="manager-hint">为“{{ knowledgeName || '当前知识库' }}”勾选标签，绑定和解绑会立即保存。</p>
      <p v-else class="manager-hint">标签可用于分类和筛选知识库。重命名会同步到已绑定的知识库。</p>
      <div class="create-tag-row"><el-input v-model="newName" size="small" maxlength="50" show-word-limit placeholder="输入新标签名称" :disabled="busy || loading || !!loadError" @keyup.enter.native="createTag" /><el-button type="primary" size="small" :loading="pending === 'create'" :disabled="busy || loading || !!loadError || !newName.trim()" @click="createTag">创建标签</el-button></div>
      <div v-if="loadError" class="manager-error"><i class="el-icon-warning-outline"></i> {{ loadError }} <el-button type="text" :disabled="busy || loading" @click="loadTags">重试</el-button></div>
      <div v-loading="loading" class="tag-list">
        <div v-if="!loading && !loadError && !tags.length" class="tags-empty"><i class="el-icon-price-tag"></i><p>暂无标签，创建一个试试</p></div>
        <div v-for="tag in tags" :key="tag.id" class="tag-row">
          <el-checkbox v-if="knowledgeBaseId" :value="isBound(tag.id)" :disabled="busy || loading || !!loadError" :aria-label="'绑定标签 ' + tag.name" @change="toggleBinding(tag, $event)" />
          <template v-if="editingId === tag.id"><el-input v-model="editName" size="small" maxlength="50" class="tag-edit-input" :disabled="busy" @keyup.enter.native="renameTag(tag)" /><el-button type="text" :loading="pending === 'rename:' + tag.id" :disabled="busy || !editName.trim()" @click="renameTag(tag)">保存</el-button><el-button type="text" :disabled="busy" @click="editingId = ''">取消</el-button></template>
          <template v-else><span class="tag-name" :title="tag.name"><i class="el-icon-price-tag"></i>{{ tag.name }}</span><small v-if="tag.binding_count !== undefined" class="binding-count">{{ tag.binding_count }} 个绑定</small><el-button type="text" icon="el-icon-edit" :disabled="busy || loading || !!loadError" @click="startRename(tag)">重命名</el-button><el-button type="text" icon="el-icon-delete" class="delete-tag" :disabled="busy || loading || !!loadError" :loading="pending === 'delete:' + tag.id" @click="removeTag(tag)">删除</el-button></template>
        </div>
      </div>
      <p class="manager-footnote">删除标签会解除它与所有知识库的关联，但不会删除知识库及文档。</p>
    </div>
    <span slot="footer"><el-button size="small" :disabled="busy" @click="closeDialog">完成</el-button></span>
  </el-dialog>
</template>

<script>
import { getKnowledgeTagList, createKnowledgeTag, renameKnowledgeTag, deleteKnowledgeTag, getKnowledgeBaseTags, bindKnowledgeBaseTags, unbindKnowledgeBaseTag } from '@/api/llmops/knowledge'
import { parseKnowledgeTags } from '@/utils/knowledgeTags'

export default {
  name: 'KnowledgeTagsManager',
  props: {
    visible: { type: Boolean, default: false },
    knowledgeBaseId: { type: [String, Number], default: '' },
    knowledgeName: { type: String, default: '' }
  },
  data() { return { tags: [], boundTags: [], newName: '', editingId: '', editName: '', pending: '', loading: false, loadError: '', requestVersion: 0 } },
  computed: { busy() { return !!this.pending } },
  watch: {
    visible: { immediate: true, handler(value) { if (value) { this.newName = ''; this.editingId = ''; this.loadTags() } else this.requestVersion += 1 } },
    knowledgeBaseId() { if (this.visible) { this.editingId = ''; this.loadTags() } }
  },
  methods: {
    closeDialog() { if (!this.busy) this.$emit('update:visible', false) },
    isBound(id) { return this.boundTags.some(tag => tag.id === id) },
    validName(name, exceptId) {
      const value = name.trim()
      if (!value || value.length > 50) { this.$message.warning('标签名称不能为空，且不能超过 50 个字符'); return '' }
      if (this.tags.some(tag => tag.id !== exceptId && tag.name === value)) { this.$message.warning('标签名称已存在'); return '' }
      return value
    },
    async loadTags() {
      const version = ++this.requestVersion
      const id = this.knowledgeBaseId
      this.loading = true
      this.loadError = ''
      try {
        const [all, bound] = await Promise.all([getKnowledgeTagList(), id ? getKnowledgeBaseTags(id) : Promise.resolve([])])
        if (version !== this.requestVersion) return false
        this.tags = parseKnowledgeTags(all)
        this.boundTags = parseKnowledgeTags(bound)
        return true
      } catch (e) {
        if (version === this.requestVersion) { this.tags = []; this.boundTags = []; this.loadError = e.message || '标签加载失败，请重试' }
        return false
      } finally { if (version === this.requestVersion) this.loading = false }
    },
    async mutate(key, request, message) {
      if (this.busy || this.loading || this.loadError) return false
      this.pending = key
      try {
        await request()
        // Notify even if the read-back fails: a successful mutation has already changed the server.
        const refreshed = await this.loadTags()
        this.$emit('change', { knowledgeBaseId: this.knowledgeBaseId })
        if (refreshed) this.$message.success(message)
        else this.$message.warning('操作已提交，但标签刷新失败，请重试确认最新状态')
        return true
      } catch (e) { this.$message.error(e.message || '标签操作失败，请重试'); return false }
      finally { this.pending = '' }
    },
    async createTag() {
      if (this.busy || this.loading || this.loadError) return
      const name = this.validName(this.newName)
      if (!name) return
      if (await this.mutate('create', () => createKnowledgeTag(name), '标签已创建')) this.newName = ''
    },
    startRename(tag) { this.editingId = tag.id; this.editName = tag.name },
    async renameTag(tag) {
      if (this.busy || this.loading || this.loadError) return
      const name = this.validName(this.editName, tag.id)
      if (!name) return
      if (name === tag.name) { this.editingId = ''; return }
      if (await this.mutate(`rename:${tag.id}`, () => renameKnowledgeTag(tag.id, name), '标签已重命名')) this.editingId = ''
    },
    async removeTag(tag) {
      if (this.busy || this.loading || this.loadError) return
      // Keep other writes and closing disabled while confirmation is open.
      this.pending = `confirm:${tag.id}`
      try { await this.$confirm(`删除标签“${tag.name}”将解除它与所有知识库的关联，知识库和文档不会删除。是否继续？`, '删除标签', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }) }
      catch (e) { this.pending = ''; return }
      this.pending = ''
      await this.mutate(`delete:${tag.id}`, () => deleteKnowledgeTag(tag.id), '标签已删除')
    },
    async toggleBinding(tag, checked) {
      if (!this.knowledgeBaseId || this.isBound(tag.id) === checked) return
      const id = this.knowledgeBaseId
      await this.mutate(`bind:${tag.id}`, () => checked ? bindKnowledgeBaseTags(id, [tag.id]) : unbindKnowledgeBaseTag(id, tag.id), checked ? '标签已绑定' : '标签已解绑')
    }
  }
}
</script>

<style scoped>
.manager-hint, .manager-footnote { margin: 0 0 16px; color: #667085; font-size: 13px; line-height: 1.7; }.manager-footnote { margin: 16px 0 0; color: #98a2b3; font-size: 12px; }
.create-tag-row { display: flex; gap: 12px; margin-bottom: 18px; }.create-tag-row .el-input { flex: 1; min-width: 0; }.create-tag-row .el-button--primary { background: #1649f5; border-color: #1649f5; }
.tag-list { min-height: 120px; max-height: 400px; overflow-y: auto; }.tag-row { display: flex; align-items: center; gap: 12px; min-height: 52px; border-bottom: 1px solid #edf0f4; }.tag-row .el-button + .el-button { margin-left: 0; }.tag-name { flex: 1; min-width: 0; color: #344054; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.tag-name i { margin-right: 8px; color: #98a2b3; }.tag-edit-input { flex: 1; min-width: 0; }.binding-count { color: #98a2b3; font-size: 12px; white-space: nowrap; }.delete-tag { color: #f56c6c; }.delete-tag.is-disabled { color: #c0c4cc; }
.tags-empty { padding: 28px 0; text-align: center; color: #98a2b3; }.tags-empty i { font-size: 30px; }.manager-error { padding: 10px 12px; margin-bottom: 12px; border-radius: 6px; color: #b54708; background: #fffaeb; line-height: 1.7; }
@media (max-width: 650px) { .binding-count { display: none; }.tag-row { gap: 8px; } }
</style>

<style>
.knowledge-tags-dialog { max-width: calc(100% - 32px); border-radius: 12px; }.knowledge-tags-dialog .el-dialog__body { padding-top: 14px; }
</style>
