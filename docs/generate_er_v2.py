#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ETMS 标准ER图生成器 - Chen标记法
白底黑字，几何边框大小适中，适配A4打印
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, Polygon
import matplotlib.font_manager as fm
import numpy as np
import os

# ========== 字体设置 ==========
fm.fontManager.addfont('/usr/share/fonts/truetype/chinese/SimHei.ttf')
fm.fontManager.addfont('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

# ========== 全局常量 ==========
BLACK = '#000000'
WHITE = '#FFFFFF'
A4_W = 7.87   # 200mm in inches
A4_H = 10.83  # 275mm in inches
OUTPUT = '/home/z/my-project/ETMS/docs/'


# =====================================================================
#  绘图工具函数
# =====================================================================

class ER:
    """ER图画布"""

    def __init__(self, title, subtitle=''):
        self.fig, self.ax = plt.subplots(figsize=(A4_W, A4_H), dpi=200)
        self.fig.patch.set_facecolor(WHITE)
        self.ax.set_facecolor(WHITE)
        self.ax.set_xlim(0, A4_W)
        self.ax.set_ylim(0, A4_H)
        self.ax.set_aspect('equal')
        self.ax.axis('off')
        self.ax.text(A4_W / 2, A4_H - 0.2, title,
                     ha='center', va='top', fontsize=11,
                     fontweight='bold', color=BLACK)
        if subtitle:
            self.ax.text(A4_W / 2, A4_H - 0.5, subtitle,
                         ha='center', va='top', fontsize=7, color='#333')

    def _tw(self, text, fs):
        """获取文字像素宽度(英寸)"""
        t = self.ax.text(0, 0, text, fontsize=fs, visible=False)
        bb = t.get_window_extent(renderer=self.fig.canvas.get_renderer())
        w = bb.width / self.fig.dpi
        h = bb.height / self.fig.dpi
        t.remove()
        return w, h

    def entity(self, cx, cy, cn, en):
        """绘制实体矩形"""
        w1, h1 = self._tw(cn, 9)
        w2, h2 = self._tw(en, 7.5)
        tw = max(w1, w2) + 0.5
        th = h1 + h2 + 0.12
        bw = tw + 0.4
        bh = th + 0.35
        r = FancyBboxPatch((cx - bw/2, cy - bh/2), bw, bh,
                           boxstyle="round,pad=0.05",
                           lw=1.8, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(r)
        self.ax.text(cx, cy + h1/2 + 0.04, cn,
                     ha='center', va='center', fontsize=9,
                     fontweight='bold', color=BLACK, zorder=6)
        self.ax.text(cx, cy - h2/2 - 0.02, en,
                     ha='center', va='center', fontsize=7.5,
                     color='#333', zorder=6)
        return bw, bh

    def attr(self, cx, cy, name, dtype, pk=False, fk=False):
        """绘制属性椭圆"""
        if pk and fk:
            label2 = f"{dtype} [PK][FK]"
        elif pk:
            label2 = f"{dtype} [PK]"
        elif fk:
            label2 = f"{dtype} [FK]"
        else:
            label2 = dtype
        w1, h1 = self._tw(name, 6.5)
        w2, h2 = self._tw(label2, 5.5)
        tw = max(w1, w2) + 0.35
        th = h1 + h2 + 0.08
        ew = tw + 0.32
        eh = th + 0.28
        e = Ellipse((cx, cy), ew, eh,
                    lw=1.6 if pk else 0.9,
                    ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(e)
        self.ax.text(cx, cy + h1/2 + 0.02, name,
                     ha='center', va='center', fontsize=6.5,
                     fontweight='bold' if pk else 'normal',
                     color=BLACK, zorder=6)
        self.ax.text(cx, cy - h2/2 - 0.01, label2,
                     ha='center', va='center', fontsize=5.5,
                     color='#444', zorder=6)
        # PK下划线
        if pk:
            ulw = w1 * 0.8
            uly = cy + 0.01
            self.ax.plot([cx - ulw/2, cx + ulw/2], [uly, uly],
                         color=BLACK, lw=0.7, zorder=6)
        return ew, eh

    def diamond(self, cx, cy, text):
        """绘制关系菱形"""
        tw, th = self._tw(text, 7)
        s = max(tw, th) + 0.5
        h = s / 2
        d = Polygon([(cx, cy+h), (cx+h, cy), (cx, cy-h), (cx-h, cy)],
                    closed=True, lw=1.5, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(d)
        self.ax.text(cx, cy, text, ha='center', va='center',
                     fontsize=7, fontweight='bold', color=BLACK, zorder=6)
        return s

    def line(self, x1, y1, x2, y2):
        self.ax.plot([x1, x2], [y1, y2], color=BLACK, lw=1.0, zorder=3)

    def card(self, x, y, label, dx=0):
        self.ax.text(x + dx, y, label, ha='center', va='center',
                     fontsize=6, fontweight='bold', color=BLACK,
                     bbox=dict(boxstyle='round,pad=0.1', fc=WHITE,
                               ec=BLACK, lw=0.7), zorder=7)

    def note(self, x, y, text):
        self.ax.text(x, y, text, ha='left', va='center',
                     fontsize=6, color='#333', zorder=6)

    def save(self, name):
        self.fig.savefig(os.path.join(OUTPUT, name), dpi=200,
                         bbox_inches='tight', facecolor=WHITE,
                         edgecolor='none', pad_inches=0.12)
        plt.close(self.fig)
        print(f"  OK: {name}")


# =====================================================================
#  属性布局引擎
# =====================================================================

def layout_attrs(d, cx, cy, bw, bh, attrs, skip_dirs=None):
    """
    在实体四周自动布局属性椭圆
    attrs: [(name, type, pk, fk), ...]
    skip_dirs: set of directions to avoid (e.g. {'top'} if top is used by connection)
    """
    n = len(attrs)
    if n == 0:
        return

    if skip_dirs is None:
        skip_dirs = set()

    # 可用方向
    avail = set(['top', 'bottom', 'left', 'right']) - skip_dirs
    if not avail:
        avail = {'top', 'bottom', 'left', 'right'}

    # 按可用方向均分
    dirs = list(avail)
    per_dir = {}
    q, r = divmod(n, len(dirs))
    idx = 0
    for i, direction in enumerate(dirs):
        count = q + (1 if i < r else 0)
        per_dir[direction] = list(range(idx, idx + count))
        idx += count

    LINE = 0.38  # 连接线长度
    GAP = 0.06   # 属性间间距

    # 计算每个方向属性的总尺寸
    def attr_size(a):
        w1, h1 = d._tw(a[0], 6.5)
        lbl = a[1]
        if a[2] and a[3]:
            lbl += ' [PK][FK]'
        elif a[2]:
            lbl += ' [PK]'
        elif a[3]:
            lbl += ' [FK]'
        w2, h2 = d._tw(lbl, 5.5)
        ew = max(w1, w2) + 0.35 + 0.32
        eh = h1 + h2 + 0.08 + 0.28
        return ew, eh

    for direction, indices in per_dir.items():
        count = len(indices)
        if count == 0:
            continue

        # 计算总尺寸
        sizes = [attr_size(attrs[i]) for i in indices]
        if direction in ('top', 'bottom'):
            total_w = sum(s[0] for s in sizes) + GAP * (count - 1)
            start_x = cx - total_w / 2
        else:
            total_h = sum(s[1] for s in sizes) + GAP * (count - 1)

        curr = 0
        for j, idx in enumerate(indices):
            a = attrs[idx]
            ew, eh = sizes[j]
            if direction == 'top':
                ax = start_x + curr + ew / 2
                ay = cy + bh / 2 + LINE + eh / 2
                d.attr(ax, ay, a[0], a[1], a[2], a[3])
                d.line(ax, ay - eh / 2, ax, cy + bh / 2)
                curr += ew + GAP
            elif direction == 'bottom':
                ax = start_x + curr + ew / 2
                ay = cy - bh / 2 - LINE - eh / 2
                d.attr(ax, ay, a[0], a[1], a[2], a[3])
                d.line(ax, ay + eh / 2, ax, cy - bh / 2)
                curr += ew + GAP
            elif direction == 'left':
                ax = cx - bw / 2 - LINE - ew / 2
                ay = cy + total_h / 2 - curr - eh / 2
                d.attr(ax, ay, a[0], a[1], a[2], a[3])
                d.line(ax + ew / 2, ay, cx - bw / 2, ay)
                curr += eh + GAP
            elif direction == 'right':
                ax = cx + bw / 2 + LINE + ew / 2
                ay = cy + total_h / 2 - curr - eh / 2
                d.attr(ax, ay, a[0], a[1], a[2], a[3])
                d.line(ax - ew / 2, ay, cx + bw / 2, ay)
                curr += eh + GAP


def full_entity(d, cx, cy, cn, en, attrs, skip_dirs=None):
    """绘制实体+所有属性"""
    bw, bh = d.entity(cx, cy, cn, en)
    layout_attrs(d, cx, cy, bw, bh, attrs, skip_dirs)
    return bw, bh


# =====================================================================
#  实体数据 (核心属性)
# =====================================================================

E = {
    'dept': ('部门', 'sys_dept', [
        ('id', 'BIGINT', True, False),
        ('parent_id', 'BIGINT', False, True),
        ('dept_name', 'VARCHAR(50)', False, False),
        ('dept_code', 'VARCHAR(50)', False, False),
        ('leader_id', 'BIGINT', False, True),
        ('level', 'INT', False, False),
        ('status', 'TINYINT', False, False),
        ('sort_order', 'INT', False, False),
        ('create_time', 'DATETIME', False, False),
    ]),
    'position': ('岗位', 'sys_position', [
        ('id', 'BIGINT', True, False),
        ('position_name', 'VARCHAR(50)', False, False),
        ('position_code', 'VARCHAR(50)', False, False),
        ('position_level', 'VARCHAR(20)', False, False),
        ('dept_id', 'BIGINT', False, True),
        ('status', 'TINYINT', False, False),
        ('sort_order', 'INT', False, False),
    ]),
    'user': ('用户', 'sys_user', [
        ('id', 'BIGINT', True, False),
        ('username', 'VARCHAR(50)', False, False),
        ('password', 'VARCHAR(100)', False, False),
        ('real_name', 'VARCHAR(50)', False, False),
        ('gender', 'TINYINT', False, False),
        ('email', 'VARCHAR(100)', False, False),
        ('phone', 'VARCHAR(20)', False, False),
        ('dept_id', 'BIGINT', False, True),
        ('position_id', 'BIGINT', False, True),
        ('status', 'TINYINT', False, False),
        ('login_time', 'DATETIME', False, False),
    ]),
    'role': ('角色', 'sys_role', [
        ('id', 'BIGINT', True, False),
        ('role_code', 'VARCHAR(50)', False, False),
        ('role_name', 'VARCHAR(50)', False, False),
        ('role_desc', 'VARCHAR(200)', False, False),
        ('data_scope', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('sort_order', 'INT', False, False),
    ]),
    'perm': ('权限', 'sys_permission', [
        ('id', 'BIGINT', True, False),
        ('perm_code', 'VARCHAR(100)', False, False),
        ('perm_name', 'VARCHAR(50)', False, False),
        ('perm_type', 'TINYINT', False, False),
        ('parent_id', 'BIGINT', False, True),
        ('path', 'VARCHAR(200)', False, False),
        ('sort_order', 'INT', False, False),
        ('status', 'TINYINT', False, False),
    ]),
    'user_role': ('用户角色', 'sys_user_role', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('role_id', 'BIGINT', False, True),
        ('create_time', 'DATETIME', False, False),
    ]),
    'role_perm': ('角色权限', 'sys_role_permission', [
        ('id', 'BIGINT', True, False),
        ('role_id', 'BIGINT', False, True),
        ('permission_id', 'BIGINT', False, True),
        ('create_time', 'DATETIME', False, False),
    ]),
    'course': ('课程', 'training_course', [
        ('id', 'BIGINT', True, False),
        ('course_name', 'VARCHAR(100)', False, False),
        ('course_code', 'VARCHAR(50)', False, False),
        ('course_type', 'TINYINT', False, False),
        ('category_id', 'BIGINT', False, True),
        ('duration', 'INT', False, False),
        ('credit', 'INT', False, False),
        ('difficulty', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('create_by', 'BIGINT', False, True),
    ]),
    'course_res': ('课程资源', 'course_resource', [
        ('id', 'BIGINT', True, False),
        ('course_id', 'BIGINT', False, True),
        ('resource_name', 'VARCHAR(100)', False, False),
        ('resource_type', 'TINYINT', False, False),
        ('resource_url', 'VARCHAR(500)', False, False),
        ('file_size', 'BIGINT', False, False),
        ('sort_order', 'INT', False, False),
    ]),
    'plan': ('培训计划', 'training_plan', [
        ('id', 'BIGINT', True, False),
        ('plan_name', 'VARCHAR(100)', False, False),
        ('plan_code', 'VARCHAR(50)', False, False),
        ('plan_type', 'TINYINT', False, False),
        ('start_date', 'DATE', False, False),
        ('end_date', 'DATE', False, False),
        ('course_id', 'BIGINT', False, True),
        ('pass_score', 'INT', False, False),
        ('need_exam', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('create_by', 'BIGINT', False, True),
    ]),
    'plan_course': ('计划课程', 'etms_plan_course', [
        ('id', 'BIGINT', True, False),
        ('plan_id', 'BIGINT', False, True),
        ('course_id', 'BIGINT', False, True),
        ('sort_order', 'INT', False, False),
        ('required', 'TINYINT', False, False),
    ]),
    'user_plan': ('用户计划', 'etms_user_plan', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('plan_id', 'BIGINT', False, True),
        ('status', 'TINYINT', False, False),
    ]),
    'progress': ('学习进度', 'learning_progress', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('plan_id', 'BIGINT', False, True),
        ('course_id', 'BIGINT', False, True),
        ('progress', 'INT', False, False),
        ('study_time', 'INT', False, False),
        ('status', 'TINYINT', False, False),
        ('last_study_time', 'DATETIME', False, False),
    ]),
    'question': ('题目', 'exam_question', [
        ('id', 'BIGINT', True, False),
        ('question_code', 'VARCHAR(50)', False, False),
        ('question_type', 'TINYINT', False, False),
        ('question_content', 'TEXT', False, False),
        ('answer', 'VARCHAR(500)', False, False),
        ('difficulty', 'TINYINT', False, False),
        ('score', 'INT', False, False),
        ('course_id', 'BIGINT', False, True),
        ('status', 'TINYINT', False, False),
        ('create_by', 'BIGINT', False, True),
    ]),
    'paper': ('试卷', 'exam_paper', [
        ('id', 'BIGINT', True, False),
        ('paper_name', 'VARCHAR(100)', False, False),
        ('paper_code', 'VARCHAR(50)', False, False),
        ('plan_id', 'BIGINT', False, True),
        ('course_id', 'BIGINT', False, True),
        ('total_score', 'INT', False, False),
        ('pass_score', 'INT', False, False),
        ('exam_duration', 'INT', False, False),
        ('paper_type', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('create_by', 'BIGINT', False, True),
    ]),
    'paper_q': ('试卷题目', 'paper_question', [
        ('id', 'BIGINT', True, False),
        ('paper_id', 'BIGINT', False, True),
        ('question_id', 'BIGINT', False, True),
        ('question_score', 'INT', False, False),
        ('sort_order', 'INT', False, False),
    ]),
    'record': ('考试记录', 'exam_record', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('plan_id', 'BIGINT', False, True),
        ('paper_id', 'BIGINT', False, True),
        ('total_score', 'INT', False, False),
        ('user_score', 'INT', False, False),
        ('passed', 'TINYINT', False, False),
        ('duration_used', 'INT', False, False),
        ('status', 'TINYINT', False, False),
    ]),
    'result': ('考核成绩', 'exam_result', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('plan_id', 'BIGINT', False, True),
        ('exam_record_id', 'BIGINT', False, True),
        ('exam_score', 'INT', False, False),
        ('total_score', 'INT', False, False),
        ('pass_status', 'TINYINT', False, False),
        ('retake_count', 'INT', False, False),
        ('exam_time', 'DATETIME', False, False),
    ]),
    'retrain': ('补训记录', 'retraining_record', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('original_plan_id', 'BIGINT', False, True),
        ('retrain_plan_id', 'BIGINT', False, True),
        ('reason', 'VARCHAR(500)', False, False),
        ('trigger_type', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('deadline', 'DATETIME', False, False),
    ]),
    'oplog': ('操作日志', 'sys_operation_log', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('module', 'VARCHAR(50)', False, False),
        ('operation_type', 'VARCHAR(50)', False, False),
        ('operation_desc', 'VARCHAR(500)', False, False),
        ('request_url', 'VARCHAR(200)', False, False),
        ('ip_address', 'VARCHAR(64)', False, False),
        ('status', 'TINYINT', False, False),
        ('cost_time', 'INT', False, False),
        ('create_time', 'DATETIME', False, False),
    ]),
    'loginlog': ('登录日志', 'sys_login_log', [
        ('id', 'BIGINT', True, False),
        ('user_id', 'BIGINT', False, True),
        ('username', 'VARCHAR(50)', False, False),
        ('login_type', 'TINYINT', False, False),
        ('status', 'TINYINT', False, False),
        ('ip_address', 'VARCHAR(64)', False, False),
        ('browser', 'VARCHAR(100)', False, False),
        ('create_time', 'DATETIME', False, False),
    ]),
    'file': ('文件', 'sys_file', [
        ('id', 'BIGINT', True, False),
        ('file_name', 'VARCHAR(100)', False, False),
        ('file_path', 'VARCHAR(500)', False, False),
        ('file_url', 'VARCHAR(500)', False, False),
        ('file_size', 'BIGINT', False, False),
        ('file_type', 'VARCHAR(50)', False, False),
        ('upload_user_id', 'BIGINT', False, True),
        ('module', 'VARCHAR(50)', False, False),
        ('create_time', 'DATETIME', False, False),
    ]),
    'job': ('定时任务', 'sys_job', [
        ('id', 'BIGINT', True, False),
        ('job_name', 'VARCHAR(100)', False, False),
        ('job_group', 'VARCHAR(50)', False, False),
        ('job_type', 'TINYINT', False, False),
        ('cron_expression', 'VARCHAR(50)', False, False),
        ('invoke_target', 'VARCHAR(500)', False, False),
        ('status', 'TINYINT', False, False),
        ('max_retry', 'INT', False, False),
    ]),
    'joblog': ('任务日志', 'sys_job_log', [
        ('id', 'BIGINT', True, False),
        ('job_id', 'BIGINT', False, True),
        ('job_name', 'VARCHAR(100)', False, False),
        ('execute_time', 'DATETIME', False, False),
        ('duration', 'INT', False, False),
        ('status', 'TINYINT', False, False),
        ('error_msg', 'TEXT', False, False),
    ]),
}


def fe(d, key, cx, cy, skip=None):
    """绘制完整实体(矩形+属性), 返回 (bw, bh)"""
    cn, en, attrs = E[key]
    return full_entity(d, cx, cy, cn, en, attrs, skip)


# =====================================================================
#  图1: 用户与组织架构
# =====================================================================
def g01():
    d = ER('ETMS ER图 (一) 用户与组织架构', 'User, Department & Position')
    fe(d, 'dept', 3.2, 8.6, skip={'left'})
    fe(d, 'position', 6.2, 8.6, skip={'left'})
    fe(d, 'user', 3.5, 4.5, skip={'top'})

    # 部门自引用: 隶属 1:N
    s = d.diamond(1.2, 8.6, '隶属')
    d.line(1.2 + s/2 + 0.05, 8.6, 2.3, 8.6)
    d.line(1.2 - s/2 - 0.05, 8.6, 0.4, 8.6)
    d.line(0.4, 8.6, 0.4, 7.5)
    d.line(0.4, 7.5, 1.8, 7.5)
    d.line(1.8, 7.5, 1.8, 8.2)
    d.card(2.0, 8.6, '1', dx=0.15)
    d.card(0.5, 8.1, 'N', dx=-0.15)

    # 部门 -> 岗位: 所属 1:N
    s2 = d.diamond(4.7, 8.6, '所属')
    d.line(3.2 + 0.6, 8.6, 4.7 - s2/2 - 0.05, 8.6)
    d.line(4.7 + s2/2 + 0.05, 8.6, 6.2 - 0.65, 8.6)
    d.card(3.7, 8.6, '1', dx=0.1)
    d.card(5.8, 8.6, 'N', dx=-0.1)

    # 部门 -> 用户: 所属 1:N
    s3 = d.diamond(2.2, 6.5, '所属')
    d.line(2.2, 8.2, 2.2, 6.5 + s3/2 + 0.05)
    d.line(2.2, 6.5 - s3/2 - 0.05, 2.2, 5.8)
    d.line(2.2, 5.8, 3.0, 5.3)
    d.card(1.8, 7.8, '1', dx=-0.2)
    d.card(2.7, 5.5, 'N', dx=-0.2)

    # 岗位 -> 用户: 担任 1:N
    s4 = d.diamond(6.0, 6.5, '担任')
    d.line(6.0, 8.2, 6.0, 6.5 + s4/2 + 0.05)
    d.line(6.0, 6.5 - s4/2 - 0.05, 6.0, 5.8)
    d.line(6.0, 5.8, 4.8, 5.0)
    d.card(6.2, 7.8, '1', dx=0.2)
    d.card(5.0, 5.2, 'N', dx=0.2)

    d.save('ER_A4_01_用户与组织架构.png')


# =====================================================================
#  图2: 权限控制
# =====================================================================
def g02():
    d = ER('ETMS ER图 (二) 权限控制', 'Role, Permission & User-Role Assignment')
    fe(d, 'user', 3.5, 8.2, skip={'bottom'})
    fe(d, 'user_role', 3.5, 4.6)
    fe(d, 'role', 6.5, 4.6, skip={'left'})
    fe(d, 'role_perm', 3.5, 1.3)
    fe(d, 'perm', 6.5, 1.3, skip={'left'})

    # 用户 --M:N--> 用户角色
    s1 = d.diamond(3.5, 6.4, '分配')
    d.line(3.5, 7.7, 3.5, 6.4 + s1/2 + 0.05)
    d.line(3.5, 6.4 - s1/2 - 0.05, 3.5, 5.3)
    d.card(3.0, 7.5, 'M', dx=-0.2)
    d.card(3.0, 5.5, 'N', dx=-0.2)

    # 用户角色 --N:1--> 角色
    s2 = d.diamond(5.0, 4.6, '对应')
    d.line(3.5 + 0.55, 4.6, 5.0 - s2/2 - 0.05, 4.6)
    d.line(5.0 + s2/2 + 0.05, 4.6, 6.5 - 0.6, 4.6)
    d.card(3.8, 4.4, 'N', dx=0)
    d.card(6.0, 4.4, '1', dx=0)

    # 角色 --1:N--> 角色权限
    s3 = d.diamond(6.5, 2.95, '授予')
    d.line(6.5, 4.0, 6.5, 2.95 + s3/2 + 0.05)
    d.line(6.5, 2.95 - s3/2 - 0.05, 6.5, 2.1)
    d.card(6.9, 3.8, '1', dx=0.2)
    d.card(6.9, 2.3, 'N', dx=0.2)

    # 角色权限 --N:1--> 权限
    s4 = d.diamond(5.0, 1.3, '关联')
    d.line(3.5 + 0.55, 1.3, 5.0 - s4/2 - 0.05, 1.3)
    d.line(5.0 + s4/2 + 0.05, 1.3, 6.5 - 0.65, 1.3)
    d.card(3.8, 1.1, 'N', dx=0)
    d.card(6.0, 1.1, '1', dx=0)

    # 权限自引用: 父子 1:N
    s5 = d.diamond(7.9, 1.3, '父子')
    d.line(6.5 + 0.65, 1.3, 7.9 - s5/2 - 0.05, 1.3)
    d.line(7.9 + s5/2 + 0.05, 1.3, 7.9 + 0.4, 1.3)
    d.line(7.9 + 0.4, 1.3, 7.9 + 0.4, 0.5)
    d.line(7.9 + 0.4, 0.5, 7.3, 0.5)
    d.line(7.3, 0.5, 7.3, 0.7)
    d.card(6.9, 1.1, '1', dx=0)
    d.card(7.4, 0.6, 'N', dx=0.2)

    d.save('ER_A4_02_权限控制.png')


# =====================================================================
#  图3: 课程管理
# =====================================================================
def g03():
    d = ER('ETMS ER图 (三) 课程管理', 'Course & Course Resource')
    fe(d, 'course', 3.8, 6.8, skip={'bottom'})
    fe(d, 'course_res', 3.8, 2.0, skip={'top'})

    s = d.diamond(3.8, 4.4, '包含')
    d.line(3.8, 6.0, 3.8, 4.4 + s/2 + 0.05)
    d.line(3.8, 4.4 - s/2 - 0.05, 3.8, 3.0)
    d.card(3.3, 5.8, '1', dx=-0.2)
    d.card(3.3, 3.2, 'N', dx=-0.2)

    d.save('ER_A4_03_课程管理.png')


# =====================================================================
#  图4: 培训计划
# =====================================================================
def g04():
    d = ER('ETMS ER图 (四) 培训计划', 'Training Plan & Plan-Course Assignment')
    fe(d, 'plan', 3.8, 8.2, skip={'bottom', 'left'})
    fe(d, 'plan_course', 3.8, 4.8, skip={'top'})
    fe(d, 'course', 3.8, 1.3, skip={'top', 'left'})

    # 计划 -> 计划课程
    s1 = d.diamond(5.8, 6.5, '安排')
    d.line(4.8, 7.7, 5.0, 6.5 + s1/2 + 0.05)
    d.line(5.5, 6.5 - s1/2 - 0.05, 4.8, 5.6)
    d.card(5.2, 7.5, '1', dx=0.2)
    d.card(5.2, 5.8, 'N', dx=0.2)

    # 计划课程 -> 课程
    s2 = d.diamond(5.8, 3.0, '包含')
    d.line(4.8, 4.2, 5.0, 3.0 + s2/2 + 0.05)
    d.line(5.5, 3.0 - s2/2 - 0.05, 4.5, 2.2)
    d.card(5.2, 4.0, 'N', dx=0.2)
    d.card(5.0, 2.4, '1', dx=0.2)

    d.save('ER_A4_04_培训计划.png')


# =====================================================================
#  图5: 学习进度
# =====================================================================
def g05():
    d = ER('ETMS ER图 (五) 学习进度', 'User Plan & Learning Progress')
    fe(d, 'user', 1.8, 8.0, skip={'bottom', 'right'})
    fe(d, 'plan', 6.0, 8.0, skip={'bottom', 'left'})
    fe(d, 'user_plan', 3.8, 4.8, skip={'top'})
    fe(d, 'progress', 3.8, 1.3, skip={'top'})

    # 用户 --M--> 用户计划
    s1 = d.diamond(2.3, 6.4, '参与')
    d.line(2.0, 7.3, 2.0, 6.4 + s1/2 + 0.05)
    d.line(2.2, 6.4 - s1/2 - 0.05, 3.0, 5.5)
    d.card(1.6, 7.1, 'M', dx=-0.2)
    d.card(2.8, 5.6, 'N', dx=-0.2)

    # 计划 --M--> 用户计划
    s2 = d.diamond(5.5, 6.4, '分配')
    d.line(5.8, 7.3, 5.6, 6.4 + s2/2 + 0.05)
    d.line(5.4, 6.4 - s2/2 - 0.05, 4.8, 5.5)
    d.card(6.2, 7.1, 'M', dx=0.2)
    d.card(5.0, 5.6, 'N', dx=0.2)

    # 用户计划 -> 学习进度 1:N
    s3 = d.diamond(3.8, 3.05, '记录')
    d.line(3.8, 4.1, 3.8, 3.05 + s3/2 + 0.05)
    d.line(3.8, 3.05 - s3/2 - 0.05, 3.8, 2.4)
    d.card(3.3, 3.8, '1', dx=-0.2)
    d.card(3.3, 2.6, 'N', dx=-0.2)

    d.save('ER_A4_05_学习进度.png')


# =====================================================================
#  图6: 题库与试卷
# =====================================================================
def g06():
    d = ER('ETMS ER图 (六) 题库与试卷', 'Question Bank & Exam Paper')
    fe(d, 'paper', 1.8, 7.5, skip={'bottom', 'right'})
    fe(d, 'paper_q', 3.8, 4.5, skip={'top'})
    fe(d, 'question', 6.2, 4.5, skip={'left'})

    # 试卷 -> 试卷题目 1:N
    s1 = d.diamond(2.3, 6.0, '组成')
    d.line(2.0, 6.7, 2.0, 6.0 + s1/2 + 0.05)
    d.line(2.2, 6.0 - s1/2 - 0.05, 3.0, 5.2)
    d.card(1.6, 6.5, '1', dx=-0.2)
    d.card(2.8, 5.3, 'N', dx=-0.2)

    # 试卷题目 -> 题目 N:1
    s2 = d.diamond(5.2, 4.5, '引用')
    d.line(3.8 + 0.55, 4.5, 5.2 - s2/2 - 0.05, 4.5)
    d.line(5.2 + s2/2 + 0.05, 4.5, 6.2 - 0.7, 4.5)
    d.card(4.1, 4.3, 'N', dx=0)
    d.card(5.8, 4.3, '1', dx=0)

    d.save('ER_A4_06_题库与试卷.png')


# =====================================================================
#  图7: 考试与成绩
# =====================================================================
def g07():
    d = ER('ETMS ER图 (七) 考试与成绩', 'Exam Record, Exam Result & Retraining')
    fe(d, 'user', 1.5, 8.0, skip={'bottom', 'right'})
    fe(d, 'record', 5.5, 8.0, skip={'bottom', 'left'})
    fe(d, 'result', 3.5, 4.2, skip={'top', 'bottom'})
    fe(d, 'retrain', 3.5, 1.2, skip={'top'})

    # 用户 --M:N--> 考试记录
    s1 = d.diamond(3.5, 8.0, '参加')
    d.line(1.5 + 0.7, 8.0, 3.5 - s1/2 - 0.05, 8.0)
    d.line(3.5 + s1/2 + 0.05, 8.0, 5.5 - 0.7, 8.0)
    d.card(2.0, 7.8, 'M', dx=0)
    d.card(5.0, 7.8, 'N', dx=0)

    # 考试记录 -> 考核成绩 1:N
    s2 = d.diamond(5.3, 6.0, '生成')
    d.line(5.5, 7.2, 5.3, 6.0 + s2/2 + 0.05)
    d.line(5.1, 6.0 - s2/2 - 0.05, 4.6, 5.0)
    d.card(5.7, 7.0, '1', dx=0.2)
    d.card(4.8, 5.1, 'N', dx=0.2)

    # 考核成绩 -> 补训记录 1:0..N
    s3 = d.diamond(3.5, 2.7, '触发')
    d.line(3.5, 3.3, 3.5, 2.7 + s3/2 + 0.05)
    d.line(3.5, 2.7 - s3/2 - 0.05, 3.5, 2.0)
    d.card(3.0, 3.1, '1', dx=-0.2)
    d.card(3.0, 2.2, '0..N', dx=-0.3)

    d.save('ER_A4_07_考试与成绩.png')


# =====================================================================
#  图8: 系统支撑
# =====================================================================
def g08():
    d = ER('ETMS ER图 (八) 系统支撑', 'Operation Log, Login Log, File, Job & Job Log')
    fe(d, 'oplog', 1.8, 8.0, skip={'bottom'})
    fe(d, 'loginlog', 6.2, 8.0, skip={'bottom'})
    fe(d, 'file', 1.8, 3.5, skip={'top'})
    fe(d, 'job', 6.2, 5.0, skip={'bottom'})
    fe(d, 'joblog', 6.2, 1.8, skip={'top'})

    # 定时任务 -> 任务日志 1:N
    s1 = d.diamond(6.2, 3.4, '产生')
    d.line(6.2, 4.2, 6.2, 3.4 + s1/2 + 0.05)
    d.line(6.2, 3.4 - s1/2 - 0.05, 6.2, 2.5)
    d.card(5.7, 4.0, '1', dx=-0.2)
    d.card(5.7, 2.7, 'N', dx=-0.2)

    d.note(0.5, 1.0, '注: sys_user 与操作日志(sys_operation_log)、登录日志(sys_login_log)、')
    d.note(0.5, 0.6, '文件(sys_file)均为 1:N 关系(通过 user_id / upload_user_id 外键关联)')
    d.note(0.5, 0.2, '图中虚线省略,避免与图(一)图(七)重复')

    d.save('ER_A4_08_系统支撑.png')


# =====================================================================
#  主函数
# =====================================================================
if __name__ == '__main__':
    print('=' * 50)
    print('ETMS 标准ER图生成 (Chen标记法, A4适配, 白底黑字)')
    print('=' * 50)
    g01()
    g02()
    g03()
    g04()
    g05()
    g06()
    g07()
    g08()
    print('\n全部完成! 共生成8张ER图。')
