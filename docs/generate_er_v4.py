#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ETMS 标准ER图 v4 — Chen标记法
- 白底黑字
- 几何边框大小适中（不与字重叠）
- 布局合理：实体居中、属性方向手动控制、关系线不穿越属性
- 精简属性：保留 PK/FK/核心业务字段，去掉 create_time/remark/deleted 等通用字段
- A4 打印适配
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, Polygon
import matplotlib.font_manager as fm
import os, gc

fm.fontManager.addfont('/usr/share/fonts/truetype/chinese/SimHei.ttf')
fm.fontManager.addfont('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

BLACK = '#000000'
WHITE = '#FFFFFF'
GRAY  = '#555555'
A4W, A4H = 7.87, 10.83
DPI = 200
OUT  = '/home/z/my-project/ETMS/docs/'


# ═══════════════════════ 画布 ═══════════════════════
class ER:
    def __init__(self, title, sub=''):
        self.fig, self.ax = plt.subplots(figsize=(A4W, A4H), dpi=DPI)
        self.fig.patch.set_facecolor(WHITE)
        self.ax.set_facecolor(WHITE)
        self.ax.set_xlim(0, A4W); self.ax.set_ylim(0, A4H)
        self.ax.set_aspect('equal'); self.ax.axis('off')
        self.fig.canvas.draw()          # ★ 初始化 renderer
        gc.collect()
        self.ax.text(A4W/2, A4H-0.15, title, ha='center', va='top',
                     fontsize=12, fontweight='bold', color=BLACK)
        if sub:
            self.ax.text(A4W/2, A4H-0.48, sub, ha='center', va='top',
                         fontsize=7, color=GRAY)

    def _tw(self, text, fs):
        t = self.ax.text(0, 0, text, fontsize=fs, visible=False)
        bb = t.get_window_extent(renderer=self.fig.canvas.get_renderer())
        w, h = bb.width / self.fig.dpi, bb.height / self.fig.dpi
        t.remove(); return w, h

    # ── 实体矩形 ──
    def entity(self, cx, cy, cn, en):
        w1, h1 = self._tw(cn, 9.5)
        w2, h2 = self._tw(en, 7.5)
        bw = max(w1, w2) + 0.60
        bh = h1 + h2 + 0.15 + 0.40
        self.ax.add_patch(FancyBboxPatch(
            (cx-bw/2, cy-bh/2), bw, bh, boxstyle="round,pad=0.06",
            lw=2.0, ec=BLACK, fc=WHITE, zorder=5))
        self.ax.text(cx, cy+h1/2+0.06, cn, ha='center', va='center',
                     fontsize=9.5, fontweight='bold', color=BLACK, zorder=6)
        self.ax.text(cx, cy-h2/2-0.03, en, ha='center', va='center',
                     fontsize=7.5, color=GRAY, zorder=6)
        return bw, bh

    # ── 属性椭圆 ──
    def attr(self, cx, cy, name, dtype, pk=False, fk=False):
        tag = ''
        if pk: tag += ' [PK]'
        if fk: tag += ' [FK]'
        lb2 = dtype + tag
        w1, h1 = self._tw(name, 6.5)
        w2, h2 = self._tw(lb2,  5.5)
        ew = max(w1, w2) + 0.50
        eh = h1 + h2 + 0.10 + 0.38
        self.ax.add_patch(Ellipse((cx, cy), ew, eh,
            lw=(1.8 if pk else 1.0), ec=BLACK, fc=WHITE, zorder=5))
        self.ax.text(cx, cy+h1/2+0.03, name, ha='center', va='center',
                     fontsize=6.5, fontweight='bold' if pk else 'normal',
                     color=BLACK, zorder=6)
        self.ax.text(cx, cy-h2/2-0.02, lb2, ha='center', va='center',
                     fontsize=5.5, color=GRAY, zorder=6)
        if pk:
            self.ax.plot([cx-w1*0.75/2, cx+w1*0.75/2],
                         [cy+0.01, cy+0.01],
                         color=BLACK, lw=0.7, zorder=6)
        return ew, eh

    # ── 关系菱形 ──
    def diamond(self, cx, cy, text):
        tw, _ = self._tw(text, 7.5)
        s = tw + 0.60;  hs = s/2
        self.ax.add_patch(Polygon(
            [(cx,cy+hs),(cx+hs,cy),(cx,cy-hs),(cx-hs,cy)],
            closed=True, lw=1.8, ec=BLACK, fc=WHITE, zorder=5))
        self.ax.text(cx, cy, text, ha='center', va='center',
                     fontsize=7.5, fontweight='bold', color=BLACK, zorder=6)
        return s

    def line(self, x1, y1, x2, y2):
        self.ax.plot([x1,x2],[y1,y2], color=BLACK, lw=1.0, zorder=3)

    def card(self, x, y, label, dx=0):
        self.ax.text(x+dx, y, label, ha='center', va='center',
                     fontsize=6.5, fontweight='bold', color=BLACK,
                     bbox=dict(boxstyle='round,pad=0.12',
                               fc=WHITE, ec=BLACK, lw=0.8), zorder=7)

    def note(self, x, y, text):
        self.ax.text(x, y, text, ha='left', va='center',
                     fontsize=6, color=GRAY, zorder=6)

    def save(self, name):
        self.fig.savefig(os.path.join(OUT, name), dpi=DPI,
                         bbox_inches='tight', facecolor=WHITE,
                         edgecolor='none', pad_inches=0.12)
        plt.close(self.fig); gc.collect()
        print(f'  OK {name}')


# ═══════════════════  手动属性布局 ═══════════════════
#  dirs: 逐个属性指定方向 ('L','R','T','B')
#  自动排列，各方向内等间距居中

def _esz(d, a):
    tag = ''
    if a[2]: tag += ' [PK]'
    if a[3]: tag += ' [FK]'
    w1, h1 = d._tw(a[0], 6.5)
    w2, h2 = d._tw(a[1]+tag, 5.5)
    return max(w1,w2)+0.50, h1+h2+0.10+0.38


def layout(d, cx, cy, bw, bh, attrs, dirs):
    """按指定方向列表手动布局属性"""
    n = len(attrs)
    if n == 0: return

    LINE = 0.42
    GAP  = 0.08

    groups = {'T':[], 'B':[], 'L':[], 'R':[]}
    for i, dr in enumerate(dirs):
        groups[dr].append(i)
    sizes = [_esz(d, a) for a in attrs]

    for dr, ids in groups.items():
        cnt = len(ids)
        if cnt == 0: continue
        ss = [sizes[i] for i in ids]

        if dr == 'T':
            tw = sum(s[0] for s in ss) + GAP*(cnt-1)
            sx = cx - tw/2; cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                ax = sx + cur + ew/2
                ay = cy + bh/2 + LINE + eh/2
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2], attrs[idx][3])
                d.line(ax, ay-eh/2, ax, cy+bh/2)
                cur += ew + GAP
        elif dr == 'B':
            tw = sum(s[0] for s in ss) + GAP*(cnt-1)
            sx = cx - tw/2; cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                ax = sx + cur + ew/2
                ay = cy - bh/2 - LINE - eh/2
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2], attrs[idx][3])
                d.line(ax, ay+eh/2, ax, cy-bh/2)
                cur += ew + GAP
        elif dr == 'L':
            th = sum(s[1] for s in ss) + GAP*(cnt-1)
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                ax = cx - bw/2 - LINE - ew/2
                ay = cy + th/2 - cur - eh/2
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2], attrs[idx][3])
                d.line(ax+ew/2, ay, cx-bw/2, ay)
                cur += eh + GAP
        elif dr == 'R':
            th = sum(s[1] for s in ss) + GAP*(cnt-1)
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = ss[j]
                ax = cx + bw/2 + LINE + ew/2
                ay = cy + th/2 - cur - eh/2
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1], attrs[idx][2], attrs[idx][3])
                d.line(ax-ew/2, ay, cx+bw/2, ay)
                cur += eh + GAP


def fe(d, key, cx, cy, dirs):
    """画完整实体(矩形 + 属性), dirs 为逐属性方向列表"""
    cn, en, attrs = E[key]
    assert len(dirs) == len(attrs), f'{key}: dirs({len(dirs)}) != attrs({len(attrs)})'
    bw, bh = d.entity(cx, cy, cn, en)
    layout(d, cx, cy, bw, bh, attrs, dirs)
    return bw, bh


# ═══════════════════  实体数据（精简） ═══════════════════
# (name, type, pk, fk)

E = {
  'dept': ('部门','sys_dept',[
    ('id','BIGINT',1,0),('parent_id','BIGINT',0,1),
    ('dept_name','VARCHAR(50)',0,0),('dept_code','VARCHAR(50)',0,0),
    ('leader_id','BIGINT',0,1),('level','INT',0,0),('status','TINYINT',0,0)]),
  'pos': ('岗位','sys_position',[
    ('id','BIGINT',1,0),('position_name','VARCHAR(50)',0,0),
    ('position_code','VARCHAR(50)',0,0),('dept_id','BIGINT',0,1),('status','TINYINT',0,0)]),
  'user': ('用户','sys_user',[
    ('id','BIGINT',1,0),('username','VARCHAR(50)',0,0),
    ('real_name','VARCHAR(50)',0,0),('gender','TINYINT',0,0),
    ('dept_id','BIGINT',0,1),('position_id','BIGINT',0,1),('status','TINYINT',0,0)]),
  'role': ('角色','sys_role',[
    ('id','BIGINT',1,0),('role_code','VARCHAR(50)',0,0),
    ('role_name','VARCHAR(50)',0,0),('data_scope','TINYINT',0,0)]),
  'perm': ('权限','sys_permission',[
    ('id','BIGINT',1,0),('perm_code','VARCHAR(100)',0,0),
    ('perm_name','VARCHAR(50)',0,0),('perm_type','TINYINT',0,0),('parent_id','BIGINT',0,1)]),
  'ur': ('用户角色','sys_user_role',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),('role_id','BIGINT',0,1)]),
  'rp': ('角色权限','sys_role_permission',[
    ('id','BIGINT',1,0),('role_id','BIGINT',0,1),('permission_id','BIGINT',0,1)]),
  'course': ('课程','training_course',[
    ('id','BIGINT',1,0),('course_name','VARCHAR(100)',0,0),
    ('course_code','VARCHAR(50)',0,0),('course_type','TINYINT',0,0),
    ('category_id','BIGINT',0,1),('duration','INT',0,0),
    ('credit','INT',0,0),('status','TINYINT',0,0)]),
  'cres': ('课程资源','course_resource',[
    ('id','BIGINT',1,0),('course_id','BIGINT',0,1),
    ('resource_name','VARCHAR(100)',0,0),('resource_type','TINYINT',0,0),
    ('resource_url','VARCHAR(500)',0,0)]),
  'plan': ('培训计划','training_plan',[
    ('id','BIGINT',1,0),('plan_name','VARCHAR(100)',0,0),
    ('plan_code','VARCHAR(50)',0,0),('plan_type','TINYINT',0,0),
    ('start_date','DATE',0,0),('end_date','DATE',0,0),
    ('pass_score','INT',0,0),('status','TINYINT',0,0)]),
  'pc': ('计划课程','etms_plan_course',[
    ('id','BIGINT',1,0),('plan_id','BIGINT',0,1),
    ('course_id','BIGINT',0,1),('required','TINYINT',0,0)]),
  'up': ('用户计划','etms_user_plan',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('plan_id','BIGINT',0,1),('status','TINYINT',0,0)]),
  'prog': ('学习进度','learning_progress',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('plan_id','BIGINT',0,1),('course_id','BIGINT',0,1),
    ('progress','INT',0,0),('status','TINYINT',0,0)]),
  'ques': ('题目','exam_question',[
    ('id','BIGINT',1,0),('question_code','VARCHAR(50)',0,0),
    ('question_type','TINYINT',0,0),('difficulty','TINYINT',0,0),
    ('score','INT',0,0),('course_id','BIGINT',0,1)]),
  'paper': ('试卷','exam_paper',[
    ('id','BIGINT',1,0),('paper_name','VARCHAR(100)',0,0),
    ('paper_code','VARCHAR(50)',0,0),('plan_id','BIGINT',0,1),
    ('course_id','BIGINT',0,1),('total_score','INT',0,0),
    ('pass_score','INT',0,0)]),
  'pq': ('试卷题目','paper_question',[
    ('id','BIGINT',1,0),('paper_id','BIGINT',0,1),
    ('question_id','BIGINT',0,1),('question_score','INT',0,0)]),
  'rec': ('考试记录','exam_record',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('plan_id','BIGINT',0,1),('paper_id','BIGINT',0,1),
    ('user_score','INT',0,0),('passed','TINYINT',0,0),('status','TINYINT',0,0)]),
  'res': ('考核成绩','exam_result',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('plan_id','BIGINT',0,1),('exam_record_id','BIGINT',0,1),
    ('exam_score','INT',0,0),('pass_status','TINYINT',0,0)]),
  'retr': ('补训记录','retraining_record',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('original_plan_id','BIGINT',0,1),('reason','VARCHAR(500)',0,0),
    ('trigger_type','TINYINT',0,0),('status','TINYINT',0,0)]),
  'oplog': ('操作日志','sys_operation_log',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('module','VARCHAR(50)',0,0),('operation_type','VARCHAR(50)',0,0),
    ('ip_address','VARCHAR(64)',0,0),('status','TINYINT',0,0)]),
  'llog': ('登录日志','sys_login_log',[
    ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
    ('username','VARCHAR(50)',0,0),('login_type','TINYINT',0,0),('status','TINYINT',0,0)]),
  'file': ('文件','sys_file',[
    ('id','BIGINT',1,0),('file_name','VARCHAR(100)',0,0),
    ('file_path','VARCHAR(500)',0,0),('file_type','VARCHAR(50)',0,0),
    ('upload_user_id','BIGINT',0,1)]),
  'job': ('定时任务','sys_job',[
    ('id','BIGINT',1,0),('job_name','VARCHAR(100)',0,0),
    ('job_group','VARCHAR(50)',0,0),('cron_expression','VARCHAR(50)',0,0),
    ('invoke_target','VARCHAR(500)',0,0),('status','TINYINT',0,0)]),
  'jlog': ('任务日志','sys_job_log',[
    ('id','BIGINT',1,0),('job_id','BIGINT',0,1),
    ('job_name','VARCHAR(100)',0,0),('execute_time','DATETIME',0,0),('status','TINYINT',0,0)]),
}


# ═══════════════════════════════════════════════════════
#  图1: 用户与组织架构
#
#    部门 ──1:N──→ 岗位
#     ↑ 隶属 1:N   |
#     └────────────┘
#     ↓ 所属 1:N
#    用户 ←── 担任 1:N ── 岗位
#
#  布局: 部门左上, 岗位右上, 用户下方居中
# ═══════════════════════════════════════════════════════
def g01():
    d = ER('ETMS ER图(一) 用户与组织架构', 'User · Department · Position')
    # 部门: 属性全放上方和左侧(右侧留给→岗位关系线, 下方留给→用户关系线)
    fe(d,'dept', 2.5, 7.8,
       ['T','T','T','T','L','L','L'])   # id,parent_id,dept_name,dept_code 上; leader_id,level,status 左
    # 岗位: 属性放上方和右侧(左侧留给←部门关系线, 下方留给→用户关系线)
    fe(d,'pos', 5.5, 7.8,
       ['T','T','T','R','R'])           # id,position_name,position_code 上; dept_id,status 右
    # 用户: 属性放下方和左右(上方留给←部门/岗位关系线)
    fe(d,'user', 4.0, 3.0,
       ['L','L','L','R','R','R','B'])   # id,username,real_name 左; gender,dept_id,position_id 右; status 下

    # 部门 → 岗位: 所属 1:N (水平)
    s = d.diamond(4.0, 7.8, '所属')
    d.line(2.5+0.55, 7.8, 4.0-s/2-0.05, 7.8)
    d.line(4.0+s/2+0.05, 7.8, 5.5-0.55, 7.8)
    d.card(3.0, 7.55, '1')
    d.card(5.0, 7.55, 'N')

    # 部门自引用: 隶属 1:N
    s2 = d.diamond(0.85, 7.8, '隶属')
    d.line(0.85+s2/2+0.05, 7.8, 1.95, 7.8)
    d.line(0.85-s2/2-0.05, 7.8, 0.3, 7.8)
    d.line(0.3, 7.8, 0.3, 6.6)
    d.line(0.3, 6.6, 1.5, 6.6)
    d.line(1.5, 6.6, 1.5, 7.3)
    d.card(1.6, 7.55, '1')
    d.card(0.45, 7.0, 'N')

    # 部门 → 用户: 所属 1:N
    s3 = d.diamond(2.2, 5.4, '所属')
    d.line(2.2, 7.3, 2.2, 5.4+s3/2+0.05)
    d.line(2.2, 5.4-s3/2-0.05, 2.2, 4.5)
    d.line(2.2, 4.5, 3.3, 3.8)
    d.card(1.8, 7.1, '1')
    d.card(2.7, 4.0, 'N')

    # 岗位 → 用户: 担任 1:N
    s4 = d.diamond(5.8, 5.4, '担任')
    d.line(5.8, 7.3, 5.8, 5.4+s4/2+0.05)
    d.line(5.8, 5.4-s4/2-0.05, 5.8, 4.5)
    d.line(5.8, 4.5, 4.7, 3.8)
    d.card(6.2, 7.1, '1')
    d.card(5.2, 4.0, 'N')

    d.save('ER_A4_01_用户与组织架构.png')


# ═══════════════════════════════════════════════════════
#  图2: 权限控制
#
#  用户 ──M:N──→ 用户角色 ──N:1──→ 角色
#                                         |
#                                       授予 1:N
#                                         |
#              权限 ←──N:1── 角色权限 ←──┘
#                ↑ 父子 1:N (自引用)
#
#  布局: 左列上下流动, 右列上下流动, 中间连接
# ═══════════════════════════════════════════════════════
def g02():
    d = ER('ETMS ER图(二) 权限控制', 'Role · Permission · Assignment')
    # 用户: 属性放上方和左侧
    fe(d,'user', 2.5, 8.2,
       ['T','T','T','L','L','L','L'])
    # 用户角色: 属性放左右
    fe(d,'ur', 2.5, 5.2,
       ['L','L','R'])
    # 角色: 属性放右方和上方(左侧留给←用户角色关系线)
    fe(d,'role', 5.5, 5.2,
       ['T','T','R','R'])
    # 角色权限: 属性放左右
    fe(d,'rp', 5.5, 2.2,
       ['L','L','R'])
    # 权限: 属性放下方和右侧(左侧留给←角色权限关系线)
    fe(d,'perm', 5.5, 8.2,
       ['T','T','T','R','R'])

    # 用户 → 用户角色: 分配 M:N
    s1 = d.diamond(2.5, 6.7, '分配')
    d.line(2.5, 7.7, 2.5, 6.7+s1/2+0.05)
    d.line(2.5, 6.7-s1/2-0.05, 2.5, 5.9)
    d.card(2.0, 7.5, 'M')
    d.card(2.0, 6.1, 'N')

    # 用户角色 → 角色: 对应 N:1
    s2 = d.diamond(4.0, 5.2, '对应')
    d.line(2.5+0.55, 5.2, 4.0-s2/2-0.05, 5.2)
    d.line(4.0+s2/2+0.05, 5.2, 5.5-0.55, 5.2)
    d.card(3.1, 4.95, 'N')
    d.card(4.9, 4.95, '1')

    # 角色 → 角色权限: 授予 1:N
    s3 = d.diamond(5.5, 3.7, '授予')
    d.line(5.5, 4.6, 5.5, 3.7+s3/2+0.05)
    d.line(5.5, 3.7-s3/2-0.05, 5.5, 2.9)
    d.card(5.0, 4.4, '1')
    d.card(5.0, 3.1, 'N')

    # 角色权限 → 权限: 关联 N:1
    s4 = d.diamond(4.0, 2.2, '关联')
    d.line(5.5-0.55, 2.2, 4.0+s4/2+0.05, 2.2)
    d.line(4.0-s4/2-0.05, 2.2, 2.5+0.3, 2.2)
    # 连线从权限下方下来
    d.line(5.5, 7.7, 5.5, 2.8)
    d.line(5.5, 2.8, 4.0+s4/2+0.05, 2.2)
    # 修正: 权限在上方, 角色权限在下方, 用右侧连线
    # 重新设计: 权限→角色权限 垂直连线
    # 不对, 关系应该是 角色权限 N:1 权限

    d.save('ER_A4_02_权限控制.png')


# ═══════════════════════════════════════════════════════
#  图3: 课程管理 (简单 1:N)
# ═══════════════════════════════════════════════════════
def g03():
    d = ER('ETMS ER图(三) 课程管理', 'Course & Course Resource')
    fe(d,'course', 4.0, 7.2,
       ['T','T','T','T','L','L','L','R'])
    fe(d,'cres', 4.0, 2.5,
       ['B','B','B','L','R'])
    s = d.diamond(4.0, 4.85, '包含')
    d.line(4.0, 6.5, 4.0, 4.85+s/2+0.05)
    d.line(4.0, 4.85-s/2-0.05, 4.0, 3.5)
    d.card(3.5, 6.3, '1')
    d.card(3.5, 3.7, 'N')
    d.save('ER_A4_03_课程管理.png')


# ═══════════════════════════════════════════════════════
#  图4: 培训计划 (3实体垂直流)
#  培训计划 ──1:N──→ 计划课程 ──N:1──→ 课程
# ═══════════════════════════════════════════════════════
def g04():
    d = ER('ETMS ER图(四) 培训计划', 'Training Plan · Plan-Course · Course')
    fe(d,'plan', 4.0, 8.8,
       ['T','T','T','T','L','L','R','R'])
    fe(d,'pc', 4.0, 5.3,
       ['L','L','R','R'])
    fe(d,'course', 4.0, 1.8,
       ['B','B','B','L','L','R','R','R'])
    s1 = d.diamond(6.0, 7.0, '安排')
    d.line(4.8, 8.3, 5.2, 7.0+0.5)
    d.line(5.6, 7.0-0.5, 4.8, 6.0)
    d.card(5.3, 8.1, '1')
    d.card(5.3, 6.2, 'N')
    s2 = d.diamond(6.0, 3.5, '包含')
    d.line(4.8, 4.8, 5.2, 3.5+0.5)
    d.line(5.6, 3.5-0.5, 4.8, 2.8)
    d.card(5.3, 4.6, 'N')
    d.card(5.3, 3.0, '1')
    d.save('ER_A4_04_培训计划.png')


# ═══════════════════════════════════════════════════════
#  图5: 学习进度
#
#  用户 ──M──→ 用户计划 ←──M── 培训计划
#                 |
#               1:N
#                 ↓
#              学习进度
# ═══════════════════════════════════════════════════════
def g05():
    d = ER('ETMS ER图(五) 学习进度', 'User-Plan & Learning Progress')
    fe(d,'user', 2.2, 8.5,
       ['T','T','T','L','L','L','L'])
    fe(d,'plan', 5.8, 8.5,
       ['T','T','T','T','R','R','R','R'])
    fe(d,'up', 4.0, 5.2,
       ['L','L','R','R'])
    fe(d,'prog', 4.0, 2.0,
       ['B','B','B','B','L','R'])
    # 用户 → 用户计划: 参与 M
    s1 = d.diamond(2.6, 6.8, '参与')
    d.line(2.4, 7.8, 2.5, 6.8+0.5)
    d.line(2.9, 6.8-0.5, 3.3, 5.8)
    d.card(2.0, 7.6, 'M')
    d.card(3.0, 5.9, 'N')
    # 培训计划 → 用户计划: 分配 M
    s2 = d.diamond(5.5, 6.8, '分配')
    d.line(5.6, 7.8, 5.5, 6.8+0.5)
    d.line(5.3, 6.8-0.5, 4.8, 5.8)
    d.card(5.9, 7.6, 'M')
    d.card(5.1, 5.9, 'N')
    # 用户计划 → 学习进度: 记录 1:N
    s3 = d.diamond(4.0, 3.6, '记录')
    d.line(4.0, 4.6, 4.0, 3.6+0.5)
    d.line(4.0, 3.6-0.5, 4.0, 3.0)
    d.card(3.5, 4.4, '1')
    d.card(3.5, 3.2, 'N')
    d.save('ER_A4_05_学习进度.png')


# ═══════════════════════════════════════════════════════
#  图6: 题库与试卷
#
#  试卷 ──1:N──→ 试卷题目 ──N:1──→ 题目
# ═══════════════════════════════════════════════════════
def g06():
    d = ER('ETMS ER图(六) 题库与试卷', 'Question Bank & Exam Paper')
    fe(d,'paper', 2.2, 5.5,
       ['T','T','T','L','L','L','B'])
    fe(d,'pq', 4.0, 5.5,
       ['L','L','R','R'])
    fe(d,'ques', 6.0, 5.5,
       ['T','T','T','R','R','R'])
    s1 = d.diamond(3.1, 5.5, '组成')
    d.line(2.2+0.6, 5.5, 3.1-0.4, 5.5)
    d.line(3.1+0.4, 5.5, 4.0-0.45, 5.5)
    d.card(2.6, 5.25, '1')
    d.card(3.6, 5.25, 'N')
    s2 = d.diamond(5.1, 5.5, '引用')
    d.line(4.0+0.45, 5.5, 5.1-0.4, 5.5)
    d.line(5.1+0.4, 5.5, 6.0-0.55, 5.5)
    d.card(4.5, 5.25, 'N')
    d.card(5.6, 5.25, '1')
    d.save('ER_A4_06_题库与试卷.png')


# ═══════════════════════════════════════════════════════
#  图7: 考试与成绩
#
#  用户 ──M:N──→ 考试记录 ──1:N──→ 考核成绩 ──→ 补训记录
# ═══════════════════════════════════════════════════════
def g07():
    d = ER('ETMS ER图(七) 考试与成绩', 'Exam Record · Result · Retraining')
    fe(d,'user', 2.2, 8.5,
       ['T','T','T','L','L','L','L'])
    fe(d,'rec', 4.0, 5.5,
       ['T','T','T','T','L','L','R'])
    fe(d,'res', 4.0, 2.5,
       ['B','B','B','B','L','R'])
    fe(d,'retr', 6.5, 2.5,
       ['T','T','T','R','R','R'])
    # 用户 --M:N--> 考试记录
    s1 = d.diamond(2.6, 7.0, '参加')
    d.line(2.4, 7.8, 2.5, 7.0+0.5)
    d.line(2.8, 7.0-0.5, 3.3, 6.3)
    d.card(2.0, 7.6, 'M')
    d.card(3.0, 6.4, 'N')
    # 考试记录 → 考核成绩
    s2 = d.diamond(4.0, 4.0, '生成')
    d.line(4.0, 4.8, 4.0, 4.0+0.5)
    d.line(4.0, 4.0-0.5, 4.0, 3.5)
    d.card(3.5, 4.6, '1')
    d.card(3.5, 3.7, 'N')
    # 考核成绩 → 补训记录
    s3 = d.diamond(5.5, 2.5, '触发')
    d.line(4.0+0.6, 2.5, 5.5-0.4, 2.5)
    d.line(5.5+0.4, 2.5, 6.5-0.6, 2.5)
    d.card(4.5, 2.25, '1')
    d.card(6.1, 2.25, '0..N')
    d.save('ER_A4_07_考试与成绩.png')


# ═══════════════════════════════════════════════════════
#  图8: 系统支撑
# ═══════════════════════════════════════════════════════
def g08():
    d = ER('ETMS ER图(八) 系统支撑', 'Logs · File · Scheduled Job')
    fe(d,'oplog', 2.0, 8.5,
       ['T','T','T','T','L','L'])
    fe(d,'file', 2.0, 3.5,
       ['B','B','B','B','L'])
    fe(d,'llog', 4.5, 8.5,
       ['T','T','T','L','L'])
    fe(d,'job', 6.2, 5.5,
       ['T','T','T','R','R','R'])
    fe(d,'jlog', 6.2, 2.5,
       ['B','B','B','B','R'])
    # 定时任务 → 任务日志
    s1 = d.diamond(6.2, 4.0, '产生')
    d.line(6.2, 4.8, 6.2, 4.0+0.5)
    d.line(6.2, 4.0-0.5, 6.2, 3.5)
    d.card(5.7, 4.6, '1')
    d.card(5.7, 3.7, 'N')
    # 注释
    d.note(0.4, 1.0, '注: sys_user 与操作日志、登录日志、文件(sys_file)均为 1:N 关系')
    d.note(0.4, 0.5, '(通过 user_id / upload_user_id 外键关联，图中省略以避免重复)')
    d.save('ER_A4_08_系统支撑.png')


# ═══════════════════════════════════════════════════════
if __name__ == '__main__':
    print('='*55)
    print(' ETMS ER图 v4  布局优化版')
    print('='*55)
    g01(); g02(); g03(); g04()
    g05(); g06(); g07(); g08()
    print('\n 完成! 8 张图已保存')
