#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ETMS ER图生成器 v5 — 严格参照 LEMS_ER_Diagram 参考图风格

风格要点:
  - 实体: 黑边矩形(尖角无圆角), 1px边框, 白色填充, 粗体中文名(单行)
  - 属性: 黑边椭圆, 1px边框, 白色填充, 属性名(单行, 黑色)
  - PK: 属性名文字下方划下划线
  - FK: 椭圆外侧标注小字"FK"
  - 关系: 黑边菱形(正方形45°旋转), 1px边框, 白色填充, 关系名(单行)
  - 连线: 1px黑色实线, 连接形状边缘(非中心)
  - 基数: 1/N 文字标在连线旁边(无边框)
  - 标题: 顶部居中两行(粗体大号 + 正常小号)
  - 图例: 底部矩形框内
  - 全图纯黑白, 无阴影/渐变/颜色
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse, Polygon, Rectangle
import matplotlib.font_manager as fm
import os, gc

fm.fontManager.addfont('/usr/share/fonts/truetype/chinese/SimHei.ttf')
fm.fontManager.addfont('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

BLACK = '#000000'
WHITE = '#FFFFFF'
OUT  = '/home/z/my-project/ETMS/docs/'


class ER:
    """ER图画布"""

    def __init__(self, title1, title2='', w=8.5, h=6.5):
        self.W, self.H = w, h
        self.fig, self.ax = plt.subplots(figsize=(w, h), dpi=200)
        self.fig.patch.set_facecolor(WHITE)
        self.ax.set_facecolor(WHITE)
        self.ax.set_xlim(0, w)
        self.ax.set_ylim(0, h)
        self.ax.set_aspect('equal')
        self.ax.axis('off')
        self.fig.canvas.draw()
        gc.collect()
        # 标题
        self.ax.text(w/2, h - 0.15, title1,
                     ha='center', va='top', fontsize=14,
                     fontweight='bold', color=BLACK, family='SimHei')
        if title2:
            self.ax.text(w/2, h - 0.45, title2,
                         ha='center', va='top', fontsize=10,
                         color=BLACK, family='SimHei')

    def _tw(self, text, fs):
        t = self.ax.text(0, 0, text, fontsize=fs, visible=False)
        bb = t.get_window_extent(renderer=self.fig.canvas.get_renderer())
        w, h = bb.width / self.fig.dpi, bb.height / self.fig.dpi
        t.remove()
        return w, h

    # ── 实体矩形 (尖角, 1px) ──
    def entity(self, cx, cy, name):
        tw, th = self._tw(name, 12)
        bw = tw + 0.30
        bh = th + 0.18
        r = Rectangle((cx - bw/2, cy - bh/2), bw, bh,
                       lw=1.0, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(r)
        self.ax.text(cx, cy, name, ha='center', va='center',
                     fontsize=12, fontweight='bold',
                     color=BLACK, family='SimHei', zorder=6)
        return bw, bh

    # ── 属性椭圆 (1px, 单行文字) ──
    def attr(self, cx, cy, name, is_pk=False, is_fk=False):
        tw, th = self._tw(name, 9)
        ew = tw + 0.22
        eh = th + 0.14
        e = Ellipse((cx, cy), ew, eh,
                    lw=1.0, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(e)
        self.ax.text(cx, cy, name, ha='center', va='center',
                     fontsize=9, color=BLACK, family='SimHei', zorder=6)
        # PK: 下划线
        if is_pk:
            ulw = tw * 0.85
            self.ax.plot([cx - ulw/2, cx + ulw/2],
                         [cy - th/2 + 0.01, cy - th/2 + 0.01],
                         color=BLACK, lw=0.8, zorder=7)
        # FK: 椭圆外侧标注
        if is_fk:
            self.ax.text(cx + ew/2 + 0.02, cy - eh/4, 'FK',
                         ha='left', va='center', fontsize=7,
                         color=BLACK, family='SimHei', zorder=7)
        return ew, eh

    # ── 关系菱形 (正方形45°旋转) ──
    def diamond(self, cx, cy, text):
        tw, th = self._tw(text, 9)
        s = max(tw, th) + 0.28
        hs = s / 2
        d = Polygon([(cx, cy+hs), (cx+hs, cy), (cx, cy-hs), (cx-hs, cy)],
                    closed=True, lw=1.0, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(d)
        self.ax.text(cx, cy, text, ha='center', va='center',
                     fontsize=9, color=BLACK, family='SimHei', zorder=6)
        return s

    # ── 连线 (1px) ──
    def line(self, x1, y1, x2, y2):
        self.ax.plot([x1, x2], [y1, y2], color=BLACK, lw=1.0, zorder=3)

    # ── 基数标注 (无边框, 旁边) ──
    def card(self, x, y, label, dx=0.08, dy=0.08):
        self.ax.text(x + dx, y + dy, label, ha='center', va='center',
                     fontsize=8, color=BLACK, family='SimHei', zorder=8)

    # ── 图例 (底部) ──
    def legend(self):
        lx = self.W - 1.6
        ly = 0.8
        lw, lh = 1.5, 1.2
        self.ax.add_patch(Rectangle((lx, ly), lw, lh,
                                     lw=0.8, ec=BLACK, fc=WHITE, zorder=4))
        self.ax.text(lx + lw/2, ly + lh - 0.08, '图 例',
                     ha='center', va='top', fontsize=8,
                     fontweight='bold', color=BLACK, family='SimHei', zorder=6)
        items = ['矩形=实体', '椭圆=属性', '菱形=关系',
                 '下划线=PK', 'FK标注=外键', '1/N=基数']
        for i, t in enumerate(items):
            self.ax.text(lx + 0.08, ly + lh - 0.28 - i*0.15, t,
                         ha='left', va='top', fontsize=6.5,
                         color=BLACK, family='SimHei', zorder=6)

    def save(self, name):
        self.fig.savefig(os.path.join(OUT, name), dpi=200,
                         bbox_inches='tight', facecolor=WHITE,
                         edgecolor='none', pad_inches=0.1)
        plt.close(self.fig)
        gc.collect()
        print(f'  OK {name}')


# ═══════════════════  属性布局 ═══════════════════
#  dirs: 逐个属性方向列表 'T'=上, 'B'=下, 'L'=左, 'R'=右

def _asz(d, name):
    tw, th = d._tw(name, 9)
    return tw + 0.22, th + 0.14


def place_attrs(d, cx, cy, bw, bh, attrs, dirs):
    """将属性按指定方向放置在实体周围"""
    GAP = 0.06

    groups = {'T': [], 'B': [], 'L': [], 'R': []}
    for i, dr in enumerate(dirs):
        groups[dr].append(i)

    for dr, ids in groups.items():
        cnt = len(ids)
        if cnt == 0:
            continue
        ss = [_asz(d, attrs[i][0]) for i in ids]

        if dr in ('T', 'B'):
            total_w = sum(s[0] for s in ss) + GAP * (cnt - 1)
            sx = cx - total_w / 2
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                ax = sx + cur + ew / 2
                if dr == 'T':
                    ay = cy + bh/2 + 0.25 + eh/2
                    d.line(ax, ay - eh/2, ax, cy + bh/2)
                else:
                    ay = cy - bh/2 - 0.25 - eh/2
                    d.line(ax, ay + eh/2, ax, cy - bh/2)
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2])
                cur += ew + GAP
        else:
            total_h = sum(s[1] for s in ss) + GAP * (cnt - 1)
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                if dr == 'L':
                    ax = cx - bw/2 - 0.25 - ew/2
                else:
                    ax = cx + bw/2 + 0.25 + ew/2
                ay = cy + total_h/2 - cur - eh/2
                if dr == 'L':
                    d.line(ax + ew/2, ay, cx - bw/2, ay)
                else:
                    d.line(ax - ew/2, ay, cx + bw/2, ay)
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2])
                cur += eh + GAP


def fe(d, key, cx, cy, dirs):
    """画完整实体 = 矩形 + 属性"""
    name, attrs = D[key]
    assert len(dirs) == len(attrs), f'{key}: {len(dirs)} dirs != {len(attrs)} attrs'
    bw, bh = d.entity(cx, cy, name)
    place_attrs(d, cx, cy, bw, bh, attrs, dirs)
    return bw, bh


# ═══════════════════  实体数据 ═══════════════════
#  每条: (显示名, is_pk, is_fk)

D = {
    'dept': ('部门', [
        ('id', 1, 0), ('parent_id', 0, 1), ('dept_name', 0, 0),
        ('dept_code', 0, 0), ('leader_id', 0, 1),
        ('level', 0, 0), ('status', 0, 0),
    ]),
    'pos': ('岗位', [
        ('id', 1, 0), ('position_name', 0, 0),
        ('position_code', 0, 0), ('dept_id', 0, 1), ('status', 0, 0),
    ]),
    'user': ('用户', [
        ('id', 1, 0), ('username', 0, 0), ('real_name', 0, 0),
        ('gender', 0, 0), ('email', 0, 0),
        ('dept_id', 0, 1), ('position_id', 0, 1), ('status', 0, 0),
    ]),
    'role': ('角色', [
        ('id', 1, 0), ('role_code', 0, 0),
        ('role_name', 0, 0), ('data_scope', 0, 0),
    ]),
    'perm': ('权限', [
        ('id', 1, 0), ('perm_code', 0, 0),
        ('perm_name', 0, 0), ('perm_type', 0, 0), ('parent_id', 0, 1),
    ]),
    'ur': ('用户角色', [
        ('id', 1, 0), ('user_id', 0, 1), ('role_id', 0, 1),
    ]),
    'rp': ('角色权限', [
        ('id', 1, 0), ('role_id', 0, 1), ('permission_id', 0, 1),
    ]),
    'course': ('课程', [
        ('id', 1, 0), ('course_name', 0, 0),
        ('course_type', 0, 0), ('category_id', 0, 1),
        ('duration', 0, 0), ('credit', 0, 0), ('status', 0, 0),
    ]),
    'cres': ('课程资源', [
        ('id', 1, 0), ('course_id', 0, 1),
        ('resource_name', 0, 0), ('resource_type', 0, 0), ('resource_url', 0, 0),
    ]),
    'plan': ('培训计划', [
        ('id', 1, 0), ('plan_name', 0, 0), ('plan_type', 0, 0),
        ('start_date', 0, 0), ('end_date', 0, 0),
        ('pass_score', 0, 0), ('status', 0, 0),
    ]),
    'pc': ('计划课程', [
        ('id', 1, 0), ('plan_id', 0, 1),
        ('course_id', 0, 1), ('required', 0, 0),
    ]),
    'up': ('用户计划', [
        ('id', 1, 0), ('user_id', 0, 1),
        ('plan_id', 0, 1), ('status', 0, 0),
    ]),
    'prog': ('学习进度', [
        ('id', 1, 0), ('user_id', 0, 1), ('plan_id', 0, 1),
        ('course_id', 0, 1), ('progress', 0, 0), ('status', 0, 0),
    ]),
    'ques': ('题目', [
        ('id', 1, 0), ('question_type', 0, 0),
        ('difficulty', 0, 0), ('score', 0, 0), ('course_id', 0, 1),
    ]),
    'paper': ('试卷', [
        ('id', 1, 0), ('paper_name', 0, 0), ('plan_id', 0, 1),
        ('total_score', 0, 0), ('pass_score', 0, 0),
    ]),
    'pq': ('试卷题目', [
        ('id', 1, 0), ('paper_id', 0, 1),
        ('question_id', 0, 1), ('question_score', 0, 0),
    ]),
    'rec': ('考试记录', [
        ('id', 1, 0), ('user_id', 0, 1), ('plan_id', 0, 1),
        ('paper_id', 0, 1), ('user_score', 0, 0), ('passed', 0, 0),
    ]),
    'res': ('考核成绩', [
        ('id', 1, 0), ('user_id', 0, 1), ('plan_id', 0, 1),
        ('exam_record_id', 0, 1), ('exam_score', 0, 0), ('pass_status', 0, 0),
    ]),
    'retr': ('补训记录', [
        ('id', 1, 0), ('user_id', 0, 1),
        ('original_plan_id', 0, 1), ('reason', 0, 0),
        ('trigger_type', 0, 0), ('status', 0, 0),
    ]),
    'oplog': ('操作日志', [
        ('id', 1, 0), ('user_id', 0, 1), ('module', 0, 0),
        ('operation_type', 0, 0), ('ip_address', 0, 0), ('status', 0, 0),
    ]),
    'llog': ('登录日志', [
        ('id', 1, 0), ('user_id', 0, 1), ('username', 0, 0),
        ('login_type', 0, 0), ('status', 0, 0),
    ]),
    'file': ('文件', [
        ('id', 1, 0), ('file_name', 0, 0), ('file_type', 0, 0),
        ('upload_user_id', 0, 1),
    ]),
    'job': ('定时任务', [
        ('id', 1, 0), ('job_name', 0, 0), ('cron_expression', 0, 0),
        ('invoke_target', 0, 0), ('status', 0, 0),
    ]),
    'jlog': ('任务日志', [
        ('id', 1, 0), ('job_id', 0, 1), ('job_name', 0, 0),
        ('execute_time', 0, 0), ('status', 0, 0),
    ]),
}


# ═══════════════════════════════════════════════════════
#  图1: 用户与组织架构 (3实体: 部门/岗位/用户)
# ═══════════════════════════════════════════════════════
def g01():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (一) 用户与组织架构', w=8.5, h=6.5)

    fe(d,'dept', 2.0, 3.8,
       ['T','T','T','L','L','L','L'])
    fe(d,'pos', 6.0, 3.8,
       ['T','T','T','R','R'])
    fe(d,'user', 4.0, 1.2,
       ['B','B','B','B','L','R','R','R'])

    # 部门 → 岗位: 所属 1:N
    s = d.diamond(4.0, 3.8, '所属')
    d.line(2.0+0.4, 3.8, 4.0-s/2, 3.8)
    d.line(4.0+s/2, 3.8, 6.0-0.4, 3.8)
    d.card(2.8, 3.8, '1')
    d.card(5.2, 3.8, 'N')

    # 部门自引用: 隶属 1:N
    s2 = d.diamond(0.5, 3.8, '隶属')
    d.line(0.5+s2/2, 3.8, 1.6, 3.8)
    d.line(0.5-s2/2, 3.8, 0.1, 3.8)
    d.line(0.1, 3.8, 0.1, 2.8)
    d.line(0.1, 2.8, 1.3, 2.8)
    d.line(1.3, 2.8, 1.3, 3.3)
    d.card(1.2, 3.8, '1')
    d.card(0.2, 3.0, 'N')

    # 部门 → 用户: 所属 1:N
    d.line(2.0, 3.3, 2.0, 2.3)
    d.line(2.0, 2.3, 3.2, 1.6)
    d.card(1.8, 2.5, '1')
    d.card(2.8, 1.8, 'N')

    # 岗位 → 用户: 担任 1:N
    d.line(6.0, 3.3, 6.0, 2.3)
    d.line(6.0, 2.3, 4.8, 1.6)
    d.card(6.2, 2.5, '1')
    d.card(5.2, 1.8, 'N')

    d.legend()
    d.save('ER_A4_01_用户与组织架构.png')


# ═══════════════════════════════════════════════════════
#  图2: 权限控制 (5实体)
# ═══════════════════════════════════════════════════════
def g02():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (二) 权限控制', w=8.5, h=7.0)

    fe(d,'user', 2.2, 5.8,
       ['T','T','T','L','L','L','L','L'])
    fe(d,'ur', 2.2, 3.2,
       ['L','L','R'])
    fe(d,'role', 4.8, 3.2,
       ['T','T','R','R'])
    fe(d,'rp', 4.8, 1.0,
       ['L','L','R'])
    fe(d,'perm', 7.0, 3.2,
       ['T','T','T','R','R'])

    # 用户 → 用户角色: 分配 M:N
    s1 = d.diamond(2.2, 4.5, '分配')
    d.line(2.2, 5.3, 2.2, 4.5+s1/2)
    d.line(2.2, 4.5-s1/2, 2.2, 3.8)
    d.card(1.9, 5.0, 'M')
    d.card(1.9, 4.0, 'N')

    # 用户角色 → 角色: 对应 N:1
    s2 = d.diamond(3.5, 3.2, '对应')
    d.line(2.2+0.4, 3.2, 3.5-s2/2, 3.2)
    d.line(3.5+s2/2, 3.2, 4.8-0.4, 3.2)
    d.card(2.7, 3.0, 'N')
    d.card(4.2, 3.0, '1')

    # 角色 → 角色权限: 授予 1:N
    d.line(4.8, 2.6, 4.8, 1.0+0.2+0.15)
    d.card(4.5, 1.8, '1')
    d.card(4.5, 1.3, 'N')

    # 角色权限 → 权限: 关联 N:1
    s4 = d.diamond(6.0, 1.0, '关联')
    d.line(4.8+0.35, 1.0, 6.0-s4/2, 1.0)
    d.line(6.0+s4/2, 1.0, 7.0-0.45, 1.0)
    # 连线: 从权限下来
    d.line(7.0, 2.6, 7.0, 1.5)
    d.line(7.0, 1.5, 6.0+s4/2, 1.0)
    d.card(5.4, 0.8, 'N')
    d.card(6.6, 0.8, '1')

    # 权限自引用: 父子 1:N
    s5 = d.diamond(7.0, 5.0, '父子')
    d.line(7.0, 3.8, 7.0, 5.0-s5/2)
    d.line(7.0, 5.0+s5/2, 7.0, 5.8)
    d.line(7.0, 5.8, 8.0, 5.8)
    d.line(8.0, 5.8, 8.0, 4.2)
    d.line(8.0, 4.2, 7.5, 3.6)
    d.card(6.7, 5.3, '1')
    d.card(8.2, 5.0, 'N')

    d.legend()
    d.save('ER_A4_02_权限控制.png')


# ═══════════════════════════════════════════════════════
#  图3: 课程管理 (2实体)
# ═══════════════════════════════════════════════════════
def g03():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (三) 课程管理', w=8.5, h=5.5)

    fe(d,'course', 4.25, 4.0,
       ['T','T','T','T','L','L','R'])
    fe(d,'cres', 4.25, 1.2,
       ['B','B','B','L','R'])

    s = d.diamond(4.25, 2.6, '包含')
    d.line(4.25, 3.5, 4.25, 2.6+s/2)
    d.line(4.25, 2.6-s/2, 4.25, 1.8)
    d.card(3.9, 3.2, '1')
    d.card(3.9, 2.0, 'N')

    d.legend()
    d.save('ER_A4_03_课程管理.png')


# ═══════════════════════════════════════════════════════
#  图4: 培训计划 (3实体)
# ═══════════════════════════════════════════════════════
def g04():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (四) 培训计划', w=8.5, h=6.5)

    fe(d,'plan', 4.25, 5.5,
       ['T','T','T','T','L','L','R'])
    fe(d,'pc', 4.25, 3.0,
       ['L','L','R','R'])
    fe(d,'course', 4.25, 0.8,
       ['B','B','B','B','L','L','R'])

    s1 = d.diamond(6.5, 4.2, '安排')
    d.line(4.25+0.5, 5.2, 5.5, 4.5)
    d.line(5.8, 4.0, 5.0, 3.5)
    d.card(5.6, 5.0, '1')
    d.card(5.3, 3.7, 'N')

    s2 = d.diamond(6.5, 1.8, '包含')
    d.line(4.25+0.5, 3.0, 5.5, 2.2)
    d.line(5.8, 1.5, 5.0, 1.2)
    d.card(5.6, 2.8, 'N')
    d.card(5.3, 1.4, '1')

    d.legend()
    d.save('ER_A4_04_培训计划.png')


# ═══════════════════════════════════════════════════════
#  图5: 学习进度 (4实体)
# ═══════════════════════════════════════════════════════
def g05():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (五) 学习进度', w=8.5, h=6.5)

    fe(d,'user', 2.2, 5.5,
       ['T','T','T','L','L','L','L','L'])
    fe(d,'plan', 6.2, 5.5,
       ['T','T','T','T','R','R','R'])
    fe(d,'up', 4.25, 3.2,
       ['L','L','R','R'])
    fe(d,'prog', 4.25, 1.0,
       ['B','B','B','B','L','R'])

    s1 = d.diamond(2.8, 4.3, '参与')
    d.line(2.4, 5.0, 2.6, 4.6)
    d.line(3.2, 4.0, 3.6, 3.6)
    d.card(2.1, 4.8, 'M')
    d.card(3.3, 3.8, 'N')

    s2 = d.diamond(5.6, 4.3, '分配')
    d.line(5.9, 5.0, 5.7, 4.6)
    d.line(5.3, 4.0, 4.9, 3.6)
    d.card(6.3, 4.8, 'M')
    d.card(5.1, 3.8, 'N')

    d.line(4.25, 2.6, 4.25, 1.6)
    d.card(3.9, 2.3, '1')
    d.card(3.9, 1.8, 'N')

    d.legend()
    d.save('ER_A4_05_学习进度.png')


# ═══════════════════════════════════════════════════════
#  图6: 题库与试卷 (3实体)
# ═══════════════════════════════════════════════════════
def g06():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (六) 题库与试卷', w=8.5, h=5.5)

    fe(d,'paper', 2.0, 3.5,
       ['T','T','T','L','L'])
    fe(d,'pq', 4.25, 3.5,
       ['L','L','R','R'])
    fe(d,'ques', 6.5, 3.5,
       ['T','T','T','R','R'])

    s1 = d.diamond(3.1, 3.5, '组成')
    d.line(2.0+0.5, 3.5, 3.1-0.4, 3.5)
    d.line(3.1+0.4, 3.5, 4.25-0.4, 3.5)
    d.card(2.5, 3.3, '1')
    d.card(3.6, 3.3, 'N')

    s2 = d.diamond(5.4, 3.5, '引用')
    d.line(4.25+0.4, 3.5, 5.4-0.4, 3.5)
    d.line(5.4+0.4, 3.5, 6.5-0.5, 3.5)
    d.card(4.6, 3.3, 'N')
    d.card(6.1, 3.3, '1')

    d.legend()
    d.save('ER_A4_06_题库与试卷.png')


# ═══════════════════════════════════════════════════════
#  图7: 考试与成绩 (4实体)
# ═══════════════════════════════════════════════════════
def g07():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (七) 考试与成绩', w=8.5, h=6.5)

    fe(d,'user', 2.2, 5.5,
       ['T','T','T','L','L','L','L','L'])
    fe(d,'rec', 4.25, 5.5,
       ['T','T','T','T','R','R'])
    fe(d,'res', 4.25, 3.0,
       ['L','L','L','R','R','R'])
    fe(d,'retr', 6.5, 3.0,
       ['T','T','T','R','R','R'])

    s1 = d.diamond(3.2, 5.5, '参加')
    d.line(2.2+0.4, 5.5, 3.2-0.4, 5.5)
    d.line(3.2+0.4, 5.5, 4.25-0.5, 5.5)
    d.card(2.6, 5.3, 'M')
    d.card(3.7, 5.3, 'N')

    d.line(4.25, 5.0, 4.25, 3.5)
    d.card(3.9, 4.5, '1')
    d.card(3.9, 3.8, 'N')

    s3 = d.diamond(5.6, 3.0, '触发')
    d.line(4.25+0.4, 3.0, 5.6-0.4, 3.0)
    d.line(5.6+0.4, 3.0, 6.5-0.5, 3.0)
    d.card(4.7, 2.8, '1')
    d.card(6.1, 2.8, 'N')

    d.legend()
    d.save('ER_A4_07_考试与成绩.png')


# ═══════════════════════════════════════════════════════
#  图8: 系统支撑 (5实体)
# ═══════════════════════════════════════════════════════
def g08():
    d = ER('ETMS 企业员工培训管理系统', 'E-R 图 (八) 系统支撑', w=8.5, h=7.0)

    fe(d,'oplog', 1.8, 5.5,
       ['T','T','T','T','L','L'])
    fe(d,'llog', 4.25, 5.5,
       ['T','T','T','L','L'])
    fe(d,'file', 1.8, 2.5,
       ['B','B','B','L'])
    fe(d,'job', 6.5, 5.5,
       ['T','T','T','R','R'])
    fe(d,'jlog', 6.5, 3.0,
       ['B','B','B','B','R'])

    d.line(6.5, 4.8, 6.5, 3.5)
    d.card(6.1, 4.3, '1')
    d.card(6.1, 3.7, 'N')

    d.legend()
    d.save('ER_A4_08_系统支撑.png')


# ═══════════════════════════════════════════════════════
if __name__ == '__main__':
    print('=' * 55)
    print(' ETMS ER图 v5  参照 LEMS_ER_Diagram 风格')
    print('=' * 55)
    g01(); g02(); g03(); g04()
    g05(); g06(); g07(); g08()
    print('\n 完成! 8 张图已保存到 docs/')
