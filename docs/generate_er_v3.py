#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ETMS 标准ER图生成器 v3 — Chen标记法
白底黑字 / 几何边框充足 / A4打印适配

核心改进：
  - 用 draw() 初始化 renderer 后再测量文字，确保测量准确
  - 属性椭圆、实体矩形均加足 padding，文字绝不溢出
  - 属性之间留充足间距，互不干涉
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, Polygon
import matplotlib.font_manager as fm
import os, gc

# ── 字体 ──
fm.fontManager.addfont('/usr/share/fonts/truetype/chinese/SimHei.ttf')
fm.fontManager.addfont('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

BLACK = '#000000'
WHITE = '#FFFFFF'
GRAY  = '#555555'
A4W   = 7.87          # inch  (≈ 200 mm)
A4H   = 10.83         # inch  (≈ 275 mm)
DPI   = 200
OUT   = '/home/z/my-project/ETMS/docs/'


# ═══════════════════════════════════════════════════════
class ER:
    """ER 图画布"""

    def __init__(self, title, sub=''):
        self.fig, self.ax = plt.subplots(figsize=(A4W, A4H), dpi=DPI)
        self.fig.patch.set_facecolor(WHITE)
        self.ax.set_facecolor(WHITE)
        self.ax.set_xlim(0, A4W)
        self.ax.set_ylim(0, A4H)
        self.ax.set_aspect('equal')
        self.ax.axis('off')
        # ★ 先 draw 一次，让 renderer 就绪，后续 _tw 才准确
        self.fig.canvas.draw()
        gc.collect()
        self.ax.text(A4W/2, A4H-0.15, title,
                     ha='center', va='top', fontsize=12,
                     fontweight='bold', color=BLACK)
        if sub:
            self.ax.text(A4W/2, A4H-0.45, sub,
                         ha='center', va='top', fontsize=7, color=GRAY)

    # ── 文字测量（renderer 已就绪）──
    def _tw(self, text, fs):
        t = self.ax.text(0, 0, text, fontsize=fs, visible=False)
        bb = t.get_window_extent(renderer=self.fig.canvas.get_renderer())
        w = bb.width  / self.fig.dpi   # inch
        h = bb.height / self.fig.dpi
        t.remove()
        return w, h

    # ── 实体矩形 ──
    def entity(self, cx, cy, cn, en):
        w1, h1 = self._tw(cn, 9.5)
        w2, h2 = self._tw(en, 7.5)
        bw = max(w1, w2) + 0.65          # 宽 padding
        bh = h1 + h2 + 0.18 + 0.45      # 行距 + 上下 padding
        r = FancyBboxPatch((cx-bw/2, cy-bh/2), bw, bh,
                           boxstyle="round,pad=0.06",
                           lw=2.0, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(r)
        self.ax.text(cx, cy+h1/2+0.06, cn,
                     ha='center', va='center', fontsize=9.5,
                     fontweight='bold', color=BLACK, zorder=6)
        self.ax.text(cx, cy-h2/2-0.03, en,
                     ha='center', va='center', fontsize=7.5,
                     color=GRAY, zorder=6)
        return bw, bh

    # ── 属性椭圆 ──
    def attr(self, cx, cy, name, dtype, pk=False, fk=False):
        tag = ''
        if pk: tag += ' [PK]'
        if fk: tag += ' [FK]'
        label2 = dtype + tag
        w1, h1 = self._tw(name,   6.5)
        w2, h2 = self._tw(label2, 5.5)
        # 椭圆大小 = 文字宽高 + 充足 padding
        ew = max(w1, w2) + 0.55       # 左右各留 ~0.27"
        eh = h1 + h2 + 0.10 + 0.42    # 行距 + 上下各留 ~0.21"
        e = Ellipse((cx, cy), ew, eh,
                    lw=(1.8 if pk else 1.0),
                    ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(e)
        self.ax.text(cx, cy+h1/2+0.03, name,
                     ha='center', va='center', fontsize=6.5,
                     fontweight='bold' if pk else 'normal',
                     color=BLACK, zorder=6)
        self.ax.text(cx, cy-h2/2-0.02, label2,
                     ha='center', va='center', fontsize=5.5,
                     color=GRAY, zorder=6)
        if pk:
            self.ax.plot([cx-w1*0.8/2, cx+w1*0.8/2],
                         [cy+0.01, cy+0.01],
                         color=BLACK, lw=0.7, zorder=6)
        return ew, eh

    # ── 关系菱形 ──
    def diamond(self, cx, cy, text):
        tw, _ = self._tw(text, 7.5)
        s = tw + 0.65           # 每侧多留 0.3"
        hs = s/2
        d = Polygon([(cx, cy+hs),(cx+hs, cy),(cx, cy-hs),(cx-hs, cy)],
                    closed=True, lw=1.8, ec=BLACK, fc=WHITE, zorder=5)
        self.ax.add_patch(d)
        self.ax.text(cx, cy, text, ha='center', va='center',
                     fontsize=7.5, fontweight='bold', color=BLACK, zorder=6)
        return s

    # ── 连线 / 基数 ──
    def line(self, x1, y1, x2, y2):
        self.ax.plot([x1,x2],[y1,y2], color=BLACK, lw=1.0, zorder=3)

    def card(self, x, y, label, dx=0):
        self.ax.text(x+dx, y, label, ha='center', va='center',
                     fontsize=6.5, fontweight='bold', color=BLACK,
                     bbox=dict(boxstyle='round,pad=0.12', fc=WHITE,
                               ec=BLACK, lw=0.8), zorder=7)

    def note(self, x, y, text):
        self.ax.text(x, y, text, ha='left', va='center',
                     fontsize=6, color=GRAY, zorder=6)

    def save(self, name):
        self.fig.savefig(os.path.join(OUT, name), dpi=DPI,
                         bbox_inches='tight', facecolor=WHITE,
                         edgecolor='none', pad_inches=0.12)
        plt.close(self.fig)
        gc.collect()
        print(f'  OK {name}')


# ═══════════════════════════════════════════════════════
#  自动属性布局
# ═══════════════════════════════════════════════════════

def _attr_wh(d, a):
    """计算单个属性椭圆的 (宽, 高)"""
    tag = ''
    if a[2]: tag += ' [PK]'
    if a[3]: tag += ' [FK]'
    w1, h1 = d._tw(a[0], 6.5)
    w2, h2 = d._tw(a[1]+tag, 5.5)
    ew = max(w1, w2) + 0.55
    eh = h1 + h2 + 0.10 + 0.42
    return ew, eh


def layout_attrs(d, cx, cy, bw, bh, attrs, skip=None):
    """在实体四周自动排列属性"""
    n = len(attrs)
    if n == 0:
        return
    if skip is None:
        skip = set()
    avail = set(['top','bottom','left','right']) - skip
    if not avail:
        avail = {'top','bottom','left','right'}
    dirs = sorted(avail)
    nd = len(dirs)
    q, r = divmod(n, nd)
    per = {}
    k = 0
    for i, dr in enumerate(dirs):
        cnt = q + (1 if i < r else 0)
        per[dr] = list(range(k, k+cnt))
        k += cnt

    LINE = 0.42        # 连接线长
    GAP  = 0.10        # 属性之间最小间距

    for dr, ids in per.items():
        cnt = len(ids)
        if cnt == 0:
            continue
        sizes = [_attr_wh(d, attrs[i]) for i in ids]
        if dr in ('top','bottom'):
            total = sum(s[0] for s in sizes) + GAP*(cnt-1)
            sx = cx - total/2
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = sizes[j]
                ax = sx + cur + ew/2
                if dr == 'top':
                    ay = cy + bh/2 + LINE + eh/2
                    d.line(ax, ay-eh/2, ax, cy+bh/2)
                else:
                    ay = cy - bh/2 - LINE - eh/2
                    d.line(ax, ay+eh/2, ax, cy-bh/2)
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1],
                       attrs[idx][2], attrs[idx][3])
                cur += ew + GAP
        else:
            total = sum(s[1] for s in sizes) + GAP*(cnt-1)
            cur = 0
            for j, idx in enumerate(ids):
                ew, eh = sizes[j]
                if dr == 'left':
                    ax = cx - bw/2 - LINE - ew/2
                else:
                    ax = cx + bw/2 + LINE + ew/2
                ay = cy + total/2 - cur - eh/2
                if dr == 'left':
                    d.line(ax+ew/2, ay, cx-bw/2, ay)
                else:
                    d.line(ax-ew/2, ay, cx+bw/2, ay)
                d.attr(ax, ay, attrs[idx][0], attrs[idx][1],
                       attrs[idx][2], attrs[idx][3])
                cur += eh + GAP


def fe(d, key, cx, cy, skip=None):
    """绘制完整实体 = 矩形 + 属性"""
    cn, en, attrs = E[key]
    bw, bh = d.entity(cx, cy, cn, en)
    layout_attrs(d, cx, cy, bw, bh, attrs, skip)
    return bw, bh


# ═══════════════════════════════════════════════════════
#  实体数据
# ═══════════════════════════════════════════════════════

E = {
    'dept': ('部门','sys_dept',[
        ('id','BIGINT',1,0),('parent_id','BIGINT',0,1),
        ('dept_name','VARCHAR(50)',0,0),('dept_code','VARCHAR(50)',0,0),
        ('leader_id','BIGINT',0,1),('level','INT',0,0),
        ('status','TINYINT',0,0),('sort_order','INT',0,0),
        ('create_time','DATETIME',0,0)]),
    'position': ('岗位','sys_position',[
        ('id','BIGINT',1,0),('position_name','VARCHAR(50)',0,0),
        ('position_code','VARCHAR(50)',0,0),('position_level','VARCHAR(20)',0,0),
        ('dept_id','BIGINT',0,1),('status','TINYINT',0,0),
        ('sort_order','INT',0,0)]),
    'user': ('用户','sys_user',[
        ('id','BIGINT',1,0),('username','VARCHAR(50)',0,0),
        ('password','VARCHAR(100)',0,0),('real_name','VARCHAR(50)',0,0),
        ('gender','TINYINT',0,0),('email','VARCHAR(100)',0,0),
        ('phone','VARCHAR(20)',0,0),('dept_id','BIGINT',0,1),
        ('position_id','BIGINT',0,1),('status','TINYINT',0,0),
        ('login_time','DATETIME',0,0)]),
    'role': ('角色','sys_role',[
        ('id','BIGINT',1,0),('role_code','VARCHAR(50)',0,0),
        ('role_name','VARCHAR(50)',0,0),('role_desc','VARCHAR(200)',0,0),
        ('data_scope','TINYINT',0,0),('status','TINYINT',0,0),
        ('sort_order','INT',0,0)]),
    'perm': ('权限','sys_permission',[
        ('id','BIGINT',1,0),('perm_code','VARCHAR(100)',0,0),
        ('perm_name','VARCHAR(50)',0,0),('perm_type','TINYINT',0,0),
        ('parent_id','BIGINT',0,1),('path','VARCHAR(200)',0,0),
        ('sort_order','INT',0,0),('status','TINYINT',0,0)]),
    'user_role': ('用户角色','sys_user_role',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('role_id','BIGINT',0,1),('create_time','DATETIME',0,0)]),
    'role_perm': ('角色权限','sys_role_permission',[
        ('id','BIGINT',1,0),('role_id','BIGINT',0,1),
        ('permission_id','BIGINT',0,1),('create_time','DATETIME',0,0)]),
    'course': ('课程','training_course',[
        ('id','BIGINT',1,0),('course_name','VARCHAR(100)',0,0),
        ('course_code','VARCHAR(50)',0,0),('course_type','TINYINT',0,0),
        ('category_id','BIGINT',0,1),('duration','INT',0,0),
        ('credit','INT',0,0),('difficulty','TINYINT',0,0),
        ('status','TINYINT',0,0),('create_by','BIGINT',0,1)]),
    'course_res': ('课程资源','course_resource',[
        ('id','BIGINT',1,0),('course_id','BIGINT',0,1),
        ('resource_name','VARCHAR(100)',0,0),('resource_type','TINYINT',0,0),
        ('resource_url','VARCHAR(500)',0,0),('file_size','BIGINT',0,0),
        ('sort_order','INT',0,0)]),
    'plan': ('培训计划','training_plan',[
        ('id','BIGINT',1,0),('plan_name','VARCHAR(100)',0,0),
        ('plan_code','VARCHAR(50)',0,0),('plan_type','TINYINT',0,0),
        ('start_date','DATE',0,0),('end_date','DATE',0,0),
        ('course_id','BIGINT',0,1),('pass_score','INT',0,0),
        ('need_exam','TINYINT',0,0),('status','TINYINT',0,0),
        ('create_by','BIGINT',0,1)]),
    'plan_course': ('计划课程','etms_plan_course',[
        ('id','BIGINT',1,0),('plan_id','BIGINT',0,1),
        ('course_id','BIGINT',0,1),('sort_order','INT',0,0),
        ('required','TINYINT',0,0)]),
    'user_plan': ('用户计划','etms_user_plan',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('plan_id','BIGINT',0,1),('status','TINYINT',0,0)]),
    'progress': ('学习进度','learning_progress',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('plan_id','BIGINT',0,1),('course_id','BIGINT',0,1),
        ('progress','INT',0,0),('study_time','INT',0,0),
        ('status','TINYINT',0,0),('last_study_time','DATETIME',0,0)]),
    'question': ('题目','exam_question',[
        ('id','BIGINT',1,0),('question_code','VARCHAR(50)',0,0),
        ('question_type','TINYINT',0,0),('question_content','TEXT',0,0),
        ('answer','VARCHAR(500)',0,0),('difficulty','TINYINT',0,0),
        ('score','INT',0,0),('course_id','BIGINT',0,1),
        ('status','TINYINT',0,0),('create_by','BIGINT',0,1)]),
    'paper': ('试卷','exam_paper',[
        ('id','BIGINT',1,0),('paper_name','VARCHAR(100)',0,0),
        ('paper_code','VARCHAR(50)',0,0),('plan_id','BIGINT',0,1),
        ('course_id','BIGINT',0,1),('total_score','INT',0,0),
        ('pass_score','INT',0,0),('exam_duration','INT',0,0),
        ('paper_type','TINYINT',0,0),('status','TINYINT',0,0),
        ('create_by','BIGINT',0,1)]),
    'paper_q': ('试卷题目','paper_question',[
        ('id','BIGINT',1,0),('paper_id','BIGINT',0,1),
        ('question_id','BIGINT',0,1),('question_score','INT',0,0),
        ('sort_order','INT',0,0)]),
    'record': ('考试记录','exam_record',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('plan_id','BIGINT',0,1),('paper_id','BIGINT',0,1),
        ('total_score','INT',0,0),('user_score','INT',0,0),
        ('passed','TINYINT',0,0),('duration_used','INT',0,0),
        ('status','TINYINT',0,0)]),
    'result': ('考核成绩','exam_result',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('plan_id','BIGINT',0,1),('exam_record_id','BIGINT',0,1),
        ('exam_score','INT',0,0),('total_score','INT',0,0),
        ('pass_status','TINYINT',0,0),('retake_count','INT',0,0),
        ('exam_time','DATETIME',0,0)]),
    'retrain': ('补训记录','retraining_record',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('original_plan_id','BIGINT',0,1),('retrain_plan_id','BIGINT',0,1),
        ('reason','VARCHAR(500)',0,0),('trigger_type','TINYINT',0,0),
        ('status','TINYINT',0,0),('deadline','DATETIME',0,0)]),
    'oplog': ('操作日志','sys_operation_log',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('module','VARCHAR(50)',0,0),('operation_type','VARCHAR(50)',0,0),
        ('operation_desc','VARCHAR(500)',0,0),('request_url','VARCHAR(200)',0,0),
        ('ip_address','VARCHAR(64)',0,0),('status','TINYINT',0,0),
        ('cost_time','INT',0,0),('create_time','DATETIME',0,0)]),
    'loginlog': ('登录日志','sys_login_log',[
        ('id','BIGINT',1,0),('user_id','BIGINT',0,1),
        ('username','VARCHAR(50)',0,0),('login_type','TINYINT',0,0),
        ('status','TINYINT',0,0),('ip_address','VARCHAR(64)',0,0),
        ('browser','VARCHAR(100)',0,0),('create_time','DATETIME',0,0)]),
    'file': ('文件','sys_file',[
        ('id','BIGINT',1,0),('file_name','VARCHAR(100)',0,0),
        ('file_path','VARCHAR(500)',0,0),('file_url','VARCHAR(500)',0,0),
        ('file_size','BIGINT',0,0),('file_type','VARCHAR(50)',0,0),
        ('upload_user_id','BIGINT',0,1),('module','VARCHAR(50)',0,0),
        ('create_time','DATETIME',0,0)]),
    'job': ('定时任务','sys_job',[
        ('id','BIGINT',1,0),('job_name','VARCHAR(100)',0,0),
        ('job_group','VARCHAR(50)',0,0),('job_type','TINYINT',0,0),
        ('cron_expression','VARCHAR(50)',0,0),('invoke_target','VARCHAR(500)',0,0),
        ('status','TINYINT',0,0),('max_retry','INT',0,0)]),
    'joblog': ('任务日志','sys_job_log',[
        ('id','BIGINT',1,0),('job_id','BIGINT',0,1),
        ('job_name','VARCHAR(100)',0,0),('execute_time','DATETIME',0,0),
        ('duration','INT',0,0),('status','TINYINT',0,0),
        ('error_msg','TEXT',0,0)]),
}


# ═══════════════════════════════════════════════════════
#  各图生成
# ═══════════════════════════════════════════════════════

def g01():
    d = ER('ETMS ER图 (一) 用户与组织架构', 'User · Department · Position')
    fe(d,'dept',     3.2, 8.8, skip={'left'})
    fe(d,'position', 6.2, 8.8, skip={'left'})
    fe(d,'user',     3.5, 4.2, skip={'top'})
    # 部门自引用: 隶属 1:N
    s = d.diamond(1.1, 8.8, '隶属')
    d.line(1.1+s/2+.05, 8.8, 2.35, 8.8)
    d.line(1.1-s/2-.05, 8.8, 0.3, 8.8)
    d.line(0.3, 8.8, 0.3, 7.4)
    d.line(0.3, 7.4, 1.8, 7.4)
    d.line(1.8, 7.4, 1.8, 8.3)
    d.card(2.05, 8.8, '1', 0.12)
    d.card(0.45, 7.9, 'N', -0.18)
    # 部门 -> 岗位
    s2 = d.diamond(4.7, 8.8, '所属')
    d.line(3.2+0.65, 8.8, 4.7-s2/2-.05, 8.8)
    d.line(4.7+s2/2+.05, 8.8, 6.2-0.7, 8.8)
    d.card(3.65, 8.6, '1', 0.08)
    d.card(5.7, 8.6, 'N', -0.08)
    # 部门 -> 用户
    s3 = d.diamond(2.0, 6.5, '所属')
    d.line(2.0, 8.2, 2.0, 6.5+s3/2+.05)
    d.line(2.0, 6.5-s3/2-.05, 2.0, 5.5)
    d.line(2.0, 5.5, 2.9, 5.0)
    d.card(1.6, 7.8, '1', -0.25)
    d.card(2.6, 5.2, 'N', -0.25)
    # 岗位 -> 用户
    s4 = d.diamond(6.2, 6.5, '担任')
    d.line(6.2, 8.2, 6.2, 6.5+s4/2+.05)
    d.line(6.2, 6.5-s4/2-.05, 6.2, 5.5)
    d.line(6.2, 5.5, 4.9, 4.8)
    d.card(6.5, 7.8, '1', 0.25)
    d.card(5.2, 4.9, 'N', 0.25)
    d.save('ER_A4_01_用户与组织架构.png')


def g02():
    d = ER('ETMS ER图 (二) 权限控制', 'Role · Permission · Assignment')
    fe(d,'user',      3.5, 8.5, skip={'bottom'})
    fe(d,'user_role', 3.5, 4.8)
    fe(d,'role',      6.5, 4.8, skip={'left'})
    fe(d,'role_perm', 3.5, 1.2)
    fe(d,'perm',      6.5, 1.2, skip={'left'})
    # 用户 --M:N--> 用户角色
    s1 = d.diamond(3.5, 6.6, '分配')
    d.line(3.5, 7.9, 3.5, 6.6+s1/2+.05)
    d.line(3.5, 6.6-s1/2-.05, 3.5, 5.5)
    d.card(3.0, 7.7, 'M', -0.25)
    d.card(3.0, 5.7, 'N', -0.25)
    # 用户角色 N:1 角色
    s2 = d.diamond(5.0, 4.8, '对应')
    d.line(3.5+0.65, 4.8, 5.0-s2/2-.05, 4.8)
    d.line(5.0+s2/2+.05, 4.8, 6.5-0.65, 4.8)
    d.card(4.0, 4.6, 'N', 0)
    d.card(6.0, 4.6, '1', 0)
    # 角色 1:N 角色权限
    s3 = d.diamond(6.5, 3.0, '授予')
    d.line(6.5, 4.2, 6.5, 3.0+s3/2+.05)
    d.line(6.5, 3.0-s3/2-.05, 6.5, 2.0)
    d.card(6.9, 4.0, '1', 0.22)
    d.card(6.9, 2.2, 'N', 0.22)
    # 角色权限 N:1 权限
    s4 = d.diamond(5.0, 1.2, '关联')
    d.line(3.5+0.65, 1.2, 5.0-s4/2-.05, 1.2)
    d.line(5.0+s4/2+.05, 1.2, 6.5-0.7, 1.2)
    d.card(4.0, 1.0, 'N', 0)
    d.card(6.0, 1.0, '1', 0)
    # 权限自引用
    s5 = d.diamond(8.0, 1.2, '父子')
    d.line(6.5+0.7, 1.2, 8.0-s5/2-.05, 1.2)
    d.line(8.0+s5/2+.05, 1.2, 8.0+0.45, 1.2)
    d.line(8.0+0.45, 1.2, 8.0+0.45, 0.4)
    d.line(8.0+0.45, 0.4, 7.3, 0.4)
    d.line(7.3, 0.4, 7.3, 0.65)
    d.card(6.9, 1.0, '1', 0)
    d.card(7.45, 0.5, 'N', 0.22)
    d.save('ER_A4_02_权限控制.png')


def g03():
    d = ER('ETMS ER图 (三) 课程管理', 'Course & Course Resource')
    fe(d,'course',    3.8, 7.0, skip={'bottom'})
    fe(d,'course_res',3.8, 2.2, skip={'top'})
    s = d.diamond(3.8, 4.6, '包含')
    d.line(3.8, 6.3, 3.8, 4.6+s/2+.05)
    d.line(3.8, 4.6-s/2-.05, 3.8, 3.2)
    d.card(3.3, 6.1, '1', -0.25)
    d.card(3.3, 3.4, 'N', -0.25)
    d.save('ER_A4_03_课程管理.png')


def g04():
    d = ER('ETMS ER图 (四) 培训计划', 'Training Plan & Plan-Course')
    fe(d,'plan',        3.8, 8.5, skip={'bottom','left'})
    fe(d,'plan_course', 3.8, 4.8, skip={'top'})
    fe(d,'course',      3.8, 1.3, skip={'top','left'})
    s1 = d.diamond(6.0, 6.6, '安排')
    d.line(4.9, 8.0, 5.2, 6.6+s1/2+.05)
    d.line(5.7, 6.6-s1/2-.05, 4.9, 5.5)
    d.card(5.4, 7.8, '1', 0.22)
    d.card(5.3, 5.7, 'N', 0.22)
    s2 = d.diamond(6.0, 3.0, '包含')
    d.line(4.9, 4.2, 5.2, 3.0+s2/2+.05)
    d.line(5.7, 3.0-s2/2-.05, 4.6, 2.2)
    d.card(5.4, 4.0, 'N', 0.22)
    d.card(5.1, 2.4, '1', 0.22)
    d.save('ER_A4_04_培训计划.png')


def g05():
    d = ER('ETMS ER图 (五) 学习进度', 'User-Plan & Learning Progress')
    fe(d,'user',     1.8, 8.2, skip={'bottom','right'})
    fe(d,'plan',     6.0, 8.2, skip={'bottom','left'})
    fe(d,'user_plan',3.8, 4.8, skip={'top'})
    fe(d,'progress', 3.8, 1.3, skip={'top'})
    s1 = d.diamond(2.3, 6.5, '参与')
    d.line(2.0, 7.5, 2.0, 6.5+s1/2+.05)
    d.line(2.3, 6.5-s1/2-.05, 3.0, 5.5)
    d.card(1.6, 7.3, 'M', -0.25)
    d.card(2.8, 5.6, 'N', -0.25)
    s2 = d.diamond(5.5, 6.5, '分配')
    d.line(5.8, 7.5, 5.5, 6.5+s2/2+.05)
    d.line(5.3, 6.5-s2/2-.05, 4.8, 5.5)
    d.card(6.2, 7.3, 'M', 0.25)
    d.card(5.0, 5.6, 'N', 0.25)
    s3 = d.diamond(3.8, 3.05, '记录')
    d.line(3.8, 4.1, 3.8, 3.05+s3/2+.05)
    d.line(3.8, 3.05-s3/2-.05, 3.8, 2.4)
    d.card(3.3, 3.8, '1', -0.25)
    d.card(3.3, 2.6, 'N', -0.25)
    d.save('ER_A4_05_学习进度.png')


def g06():
    d = ER('ETMS ER图 (六) 题库与试卷', 'Question Bank & Exam Paper')
    fe(d,'paper',   1.8, 7.5, skip={'bottom','right'})
    fe(d,'paper_q', 3.8, 4.5, skip={'top'})
    fe(d,'question',6.2, 4.5, skip={'left'})
    s1 = d.diamond(2.3, 6.0, '组成')
    d.line(2.0, 6.7, 2.0, 6.0+s1/2+.05)
    d.line(2.3, 6.0-s1/2-.05, 3.0, 5.2)
    d.card(1.6, 6.5, '1', -0.25)
    d.card(2.8, 5.3, 'N', -0.25)
    s2 = d.diamond(5.2, 4.5, '引用')
    d.line(3.8+0.7, 4.5, 5.2-s2/2-.05, 4.5)
    d.line(5.2+s2/2+.05, 4.5, 6.2-0.75, 4.5)
    d.card(4.2, 4.3, 'N', 0)
    d.card(5.9, 4.3, '1', 0)
    d.save('ER_A4_06_题库与试卷.png')


def g07():
    d = ER('ETMS ER图 (七) 考试与成绩', 'Exam Record · Result · Retraining')
    fe(d,'user',   1.5, 8.2, skip={'bottom','right'})
    fe(d,'record', 5.5, 8.2, skip={'bottom','left'})
    fe(d,'result', 3.5, 4.2, skip={'top','bottom'})
    fe(d,'retrain',3.5, 1.2, skip={'top'})
    s1 = d.diamond(3.5, 8.2, '参加')
    d.line(1.5+0.7, 8.2, 3.5-s1/2-.05, 8.2)
    d.line(3.5+s1/2+.05, 8.2, 5.5-0.7, 8.2)
    d.card(2.0, 8.0, 'M', 0)
    d.card(5.0, 8.0, 'N', 0)
    s2 = d.diamond(5.5, 6.2, '生成')
    d.line(5.5, 7.4, 5.5, 6.2+s2/2+.05)
    d.line(5.5, 6.2-s2/2-.05, 4.7, 5.0)
    d.card(5.8, 7.2, '1', 0.25)
    d.card(5.0, 5.1, 'N', 0.25)
    s3 = d.diamond(3.5, 2.7, '触发')
    d.line(3.5, 3.3, 3.5, 2.7+s3/2+.05)
    d.line(3.5, 2.7-s3/2-.05, 3.5, 2.0)
    d.card(3.0, 3.1, '1', -0.3)
    d.card(3.0, 2.2, '0..N', -0.35)
    d.save('ER_A4_07_考试与成绩.png')


def g08():
    d = ER('ETMS ER图 (八) 系统支撑', 'Logs · File · Scheduled Job')
    fe(d,'oplog', 1.8, 8.2, skip={'bottom'})
    fe(d,'loginlog',6.2, 8.2, skip={'bottom'})
    fe(d,'file',  1.8, 3.8, skip={'top'})
    fe(d,'job',   6.2, 5.2, skip={'bottom'})
    fe(d,'joblog',6.2, 1.8, skip={'top'})
    s1 = d.diamond(6.2, 3.5, '产生')
    d.line(6.2, 4.5, 6.2, 3.5+s1/2+.05)
    d.line(6.2, 3.5-s1/2-.05, 6.2, 2.5)
    d.card(5.7, 4.3, '1', -0.25)
    d.card(5.7, 2.7, 'N', -0.25)
    d.note(0.4, 1.0, '注: sys_user 与操作日志、登录日志、文件均为 1:N 关系')
    d.note(0.4, 0.5, '(通过 user_id / upload_user_id 外键关联，图中省略以避免重复)')
    d.save('ER_A4_08_系统支撑.png')


# ═══════════════════════════════════════════════════════
if __name__ == '__main__':
    print('='*55)
    print(' ETMS ER图 v3  Chen标记法 · 白底黑字 · A4适配')
    print('='*55)
    g01(); g02(); g03(); g04()
    g05(); g06(); g07(); g08()
    print('\n 全部完成! 8 张 ER 图已保存到 docs/')
