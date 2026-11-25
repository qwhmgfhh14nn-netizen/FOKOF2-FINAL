import json, random, math, sys, itertools, os, zlib
sys.path.insert(0, 'tools/catalog')
from subjects import SUBJECTS
from copy_ar import NOUN, CAT_SLUG, SUBLINES, KICK, CTA_CAT
from PIL import ImageFont

W = 360.0
FONT_B = ImageFont.truetype('/usr/share/fonts/truetype/noto/NotoSansArabic-Bold.ttf', 100)
FONT_R = ImageFont.truetype('/usr/share/fonts/truetype/noto/NotoSansArabic-Regular.ttf', 100)

def tw(text, fs, bold):
    return (FONT_B if bold else FONT_R).getlength(text) * fs / 100.0

def hx(c):
    c = c.lstrip('#'); return tuple(int(c[i:i+2], 16) for i in (0, 2, 4))
def tohex(t): return '#%02X%02X%02X' % tuple(max(0, min(255, int(round(v)))) for v in t)
def blend(a, b, t):
    A, B = hx(a), hx(b); return tohex([A[i]*(1-t)+B[i]*t for i in range(3)])
def lum(c):
    def f(v):
        v /= 255.0; return v/12.92 if v <= 0.03928 else ((v+0.055)/1.055)**2.4
    r, g, b = hx(c); return 0.2126*f(r)+0.7152*f(g)+0.0722*f(b)
def contrast(a, b):
    la, lb = lum(a), lum(b)
    if la < lb: la, lb = lb, la
    return (la+0.05)/(lb+0.05)

DARK_INK, LIGHT_INK = '#FFF8E7', '#1B1630'
# name, bg1, bg2, accent, accent2
PAL_DARK = [
 ('navy_gold', '#0B1530', '#1B2A55', '#D4AF37', '#F2D67C'), ('emerald', '#06382F', '#0F6B57', '#E6C35C', '#F6E3A1'),
 ('burgundy', '#4A0E1D', '#7A1F34', '#E8C27A', '#F7DFB0'), ('charcoal_gold', '#141414', '#2B2B2B', '#D4AF37', '#F0D98A'),
 ('royal_purple', '#2A1250', '#5B2A9E', '#F0C75E', '#FFE7A3'), ('teal', '#053B44', '#0E7C86', '#F6D87A', '#FFF0B8'),
 ('midnight_cyan', '#0B132B', '#1C2541', '#5BC0BE', '#A8E6E4'), ('forest', '#14281D', '#2F5D3A', '#E9D8A6', '#FFF1C8'),
 ('deep_red', '#5A0F12', '#A4262C', '#FFD27D', '#FFE9B8'), ('coffee', '#2B1B12', '#5C3B26', '#E3B778', '#F6D9A6'),
 ('plum', '#3A1030', '#7B2D6B', '#F5C7A9', '#FFE3D0'), ('slate', '#1F2933', '#3E4C59', '#F0B429', '#FFD97A'),
 ('ocean', '#023E58', '#1B7A9E', '#FFD166', '#FFE7A6'), ('olive', '#2F3B1B', '#66753A', '#F4E4A6', '#FFF4CF'),
 ('indigo', '#1A1B4B', '#3D3FA8', '#FFC857', '#FFE29A'), ('maroon_rose', '#4B1535', '#8E2F63', '#FFD9A0', '#FFEBCB'),
]
PAL_LIGHT = [
 ('ivory_gold', '#FFF8E7', '#F3E3B8', '#74530A', '#D4AF37'), ('cream_emerald', '#F6F1E4', '#DDEBD5', '#0F6B57', '#4FA38C'),
 ('blush', '#FFF0EE', '#F9D5D0', '#A4262C', '#E07A7F'), ('sky', '#EAF4FF', '#CFE3FA', '#1D4E89', '#5E9AD6'),
 ('mint', '#EAF8F2', '#C9EBDD', '#0B6B50', '#4FB894'), ('lavender', '#F3EEFF', '#DCCFF7', '#5B2A9E', '#A07AE0'),
 ('sand', '#F7EBD6', '#E8D2A6', '#7A4E0C', '#C79A3F'), ('peach', '#FFF1E6', '#FFD7B8', '#A33E14', '#EE9A6A'),
 ('lemon', '#FFFBE0', '#F7EDA1', '#6B5600', '#D6B72A'), ('pearl', '#F4F5F7', '#DDE1E6', '#243B53', '#7A93AC'),
 ('rose_gold', '#FDECEC', '#F2C6C2', '#8E3030', '#D98C88'), ('aqua', '#E7F8F8', '#BFE9EA', '#0B646D', '#4CB8C0'),
 ('terracotta', '#FBEADD', '#EBC3A4', '#8A3517', '#D98A5F'), ('sage', '#EEF3E8', '#CFDCC0', '#3A6335', '#86B07F'),
 ('saffron', '#FFF4D6', '#FFD983', '#7D4400', '#E39A1F'), ('coral', '#FFEDE8', '#FFC9BC', '#A8321F', '#F08A78'),
 ('ice', '#F0F7FA', '#CBE4EE', '#1A647F', '#62AFCB'), ('pistachio', '#F1F6DE', '#D8E6A8', '#455F0F', '#8FB02E'),
 ('champagne', '#FBF3E4', '#EBD7B0', '#6F5018', '#C4A25A'), ('lilac_rose', '#F9EEF6', '#EBC9E0', '#7E2859', '#C77BA8'),
]

def make_palettes():
    out, dropped = [], []
    for dark, lst in ((True, PAL_DARK), (False, PAL_LIGHT)):
        for name, b1, b2, a1, a2 in lst:
            ink = DARK_INK if dark else LIGHT_INK
            panel = blend(b1, '#000000', 0.45) if dark else '#FFFFFF'
            # worst-case backgrounds under free text: both gradient ends with 30% art
            worst = [blend(b1, a1, .16), blend(b2, a1, .16), blend(b1, a2, .16), blend(b2, a2, .16), b1, b2]
            ok_ink = min(contrast(ink, w) for w in worst)
            ok_panel = contrast(ink, panel)
            kick = a2 if dark else a1
            ok_kick = min(contrast(kick, w) for w in worst + [panel])
            cta_txt = max((DARK_INK, LIGHT_INK), key=lambda c: contrast(c, a1))
            ok_cta = contrast(cta_txt, a1)
            if min(ok_ink, ok_panel, ok_cta) < 4.5 or ok_kick < 3.6:
                dropped.append((name, round(ok_ink, 1), round(ok_panel, 1), round(ok_kick, 1), round(ok_cta, 1))); continue
            out.append(dict(name=name, dark=dark, b1=b1, b2=b2, a1=a1, a2=a2, ink=ink, panel=panel, kick=kick, cta_txt=cta_txt))
    return out, dropped

# ---------- art generators (shapes behind the text) ----------
def sh(t, x, y, w, h, c, op, rot=0, tag=''):
    return dict(t='S', s=t, x=round(x, 1), y=round(y, 1), w=round(w, 1), h=round(h, 1), c=c, o=round(op, 2), r=round(rot, 1), tag=tag)

def free(x, y, r, avoid):
    if not avoid: return True
    ax0, ay0, ax1, ay1 = avoid
    return not (ax0 - r < x < ax1 + r and ay0 - r < y < ay1 + r)

def g_orbs(rng, P, h, avoid):
    out = []
    for i in range(rng.randint(4, 7)):
        r = rng.uniform(50, 130); x = rng.choice([rng.uniform(-20, 80), rng.uniform(280, 380)]) if rng.random() < .75 else rng.uniform(0, W)
        y = rng.uniform(-20, h * .35) if rng.random() < .5 else rng.uniform(h * .6, h + 20)
        c = rng.choice([P['a1'], P['a2'], blend(P['b2'], '#FFFFFF', .25)])
        op = rng.uniform(.14, .32) if not free(x, y, r, avoid) else rng.uniform(.18, .42)
        out.append(sh('CIRCLE', x, y, 2*r, 2*r, c, op))
    return out
def g_diamonds(rng, P, h, avoid):
    s = rng.choice([80, 100, 120]) * max(1.0, h / 360.0) ** .5; out = []
    ox, oy = rng.uniform(0, s), rng.uniform(0, s)
    row = 0; y = -s/2 + oy
    while y < h + s:
        x = -s/2 + ox + (s/2 if row % 2 else 0)
        while x < W + s:
            out.append(sh('DIAMOND', x, y, s*.8, s*.8, rng.choice([P['a1'], P['a2']]), rng.uniform(.07, .16)))
            x += s
        y += s/2; row += 1
    return out
def g_hex(rng, P, h, avoid):
    w = rng.choice([84, 100, 120]); hh = w * .87; out = []
    for r in range(int(h / (hh*.9)) + 3):
        for q in range(int(W / (w*.78)) + 3):
            x = q * w * .78 - 10; y = r * hh * .95 - 10 + (hh/2 if q % 2 else 0)
            out.append(sh('HEXAGON', x, y, w*.9, hh*.9, rng.choice([P['a1'], P['a2'], blend(P['b2'], '#FFFFFF', .3)]), rng.uniform(.06, .15)))
    return out
def g_bands(rng, P, h, avoid):
    out = []; rot = rng.choice([-32, -24, 24, 32, 45, -45])
    for i in range(rng.randint(3, 5)):
        cx = rng.uniform(-40, W + 40); cy = rng.uniform(0, h)
        out.append(sh('RECTANGLE', cx, cy, rng.uniform(40, 120), h * 2.2, rng.choice([P['a1'], P['a2'], blend(P['b2'], '#FFFFFF', .3)]), rng.uniform(.1, .26), rot))
    return out
def g_rings(rng, P, h, avoid):
    cx = rng.choice([0, W, W/2, rng.uniform(40, 320)]); cy = rng.choice([0, h, h*.2, h*.8]); out = []
    r = rng.uniform(280, 380); step = rng.uniform(34, 52); i = 0
    while r > 20:
        out.append(sh('CIRCLE', cx, cy, 2*r, 2*r, P['a1'] if i % 2 == 0 else P['a2'], .10 if i % 2 == 0 else .05)); r -= step; i += 1
    return out
def g_stars(rng, P, h, avoid):
    out = []; n = rng.randint(14, 24); tries = 0
    while len(out) < n and tries < 400:
        tries += 1; x, y = rng.uniform(8, W-8), rng.uniform(8, h-8); s = rng.uniform(9, 26)
        if not free(x, y, s, avoid): continue
        out.append(sh('STAR', x, y, s, s, rng.choice([P['a1'], P['a2']]), rng.uniform(.35, .8), rng.uniform(0, 36)))
    return out
def g_arches(rng, P, h, avoid):
    out = []; n = rng.randint(4, 5); aw = W / n
    for i in range(n):
        ah = rng.uniform(.28, .5) * h
        out.append(sh('ROUNDED_RECT', aw*(i+.5), h - ah/2 + 10, aw*.86, ah, rng.choice([P['a1'], P['a2']]), rng.uniform(.10, .24)))
        out.append(sh('CIRCLE', aw*(i+.5), h - ah + 10, aw*.86, aw*.86, rng.choice([P['a1'], P['a2']]), rng.uniform(.10, .24)))
    return out
def g_corners(rng, P, h, avoid):
    out = []
    for (cx, cy) in ((0, 0), (W, 0), (0, h), (W, h)):
        sx = 1 if cx == 0 else -1; sy = 1 if cy == 0 else -1
        for k, (dx, dy, s, o) in enumerate(((30, 30, 46, .5), (66, 30, 24, .4), (30, 66, 24, .4), (52, 52, 14, .7), (96, 30, 12, .3), (30, 96, 12, .3))):
            out.append(sh('DIAMOND', cx + sx*dx, cy + sy*dy, s, s, P['a1'] if k % 2 == 0 else P['a2'], o))
    return out
ARTS = [g_orbs, g_diamonds, g_hex, g_bands, g_rings, g_stars, g_arches, g_corners]
ART_NAMES = ['orbs', 'diamonds', 'hex', 'bands', 'rings', 'stars', 'arches', 'corners']

# ---------- text + layouts ----------
def T(text, x, y, fs, bold, c, op=1.0):
    return dict(t='T', text=text, x=round(x, 1), y=round(y, 1), fs=round(fs, 1), b=bold, c=c, o=op)

def build(P, layout, arr, head, sub, kick, cta, h, rng):
    """returns (text_shapes_and_texts, panel_shapes, avoid_rect, notes). arr: 0 kicker+head+sub+cta, 1 head+sub, 2 kicker+head+cta"""
    ink = P['ink']
    hfs = max(20.0, min(30.0, 300.0 / max(tw(head, 1, True), 1)))
    hfs = round(hfs, 1)
    parts = []  # (kind, height)
    if arr in (0, 2): parts.append(('kick', 18))
    parts.append(('head', hfs * 1.3))
    if arr in (0, 1): parts.append(('sub', 18))
    if arr in (0, 2): parts.append(('cta', 38))
    gap = 12
    bh = sum(p[1] for p in parts) + gap * (len(parts) - 1)
    panels = []
    cx = W / 2
    pad0 = {0: 28, 1: 26, 2: 26, 3: 28, 4: 28, 5: 46, 6: 28}[layout]
    pad = max(12.0, min(pad0, (h - bh) / 2 - 6))
    if layout == 0:   cy = h * (.5 + rng.uniform(-.06, .06)); txt_ink = ink
    elif layout == 1: cy = h - (bh + 2*pad)/2 - 0; panels.append(sh('RECTANGLE', cx, h - (bh + 2*pad)/2, W, bh + 2*pad, P['panel'], .93)); panels.append(sh('RECTANGLE', cx, h - (bh + 2*pad), W, 3, P['a1'], .95)); cy = h - (bh + 2*pad)/2
    elif layout == 2: cy = (bh + 2*pad)/2; panels.append(sh('RECTANGLE', cx, cy, W, bh + 2*pad, P['panel'], .93)); panels.append(sh('RECTANGLE', cx, bh + 2*pad, W, 3, P['a1'], .95))
    elif layout == 3: cy = h * .5; panels.append(sh('ROUNDED_RECT', cx, cy, min(W - 36, 324), bh + 2*pad, P['panel'], .94)); panels.append(sh('ROUNDED_RECT', cx, cy, min(W - 36, 324) - 14, bh + 2*pad - 14, P['a1'], .12))
    elif layout == 4: cy = h * .5; panels.append(sh('RECTANGLE', cx, cy, W, bh + 2*pad, P['panel'], .9)); panels.append(sh('RECTANGLE', cx, cy - (bh + 2*pad)/2, W, 2.5, P['a1'], .95)); panels.append(sh('RECTANGLE', cx, cy + (bh + 2*pad)/2, W, 2.5, P['a1'], .95))
    elif layout == 5: cy = h * .5; panels.append(sh('RECTANGLE', cx, cy, W * 1.35, bh + 2*pad, P['panel'], .92, rng.choice([-6, 6]))); panels.append(sh('RECTANGLE', cx, cy, W * 1.35, 3, P['a1'], .9, 0))
    else:             cy = h * .5; m = 14
                      
    if layout == 6:
        for (x, y, w_, h_) in ((cx, m, W - 2*m, 2.5), (cx, h - m, W - 2*m, 2.5), (m, h/2, 2.5, h - 2*m), (W - m, h/2, 2.5, h - 2*m)):
            panels.append(sh('RECTANGLE', x, y, w_, h_, P['a1'], .85))
        for (x, y) in ((m, m), (W - m, m), (m, h - m), (W - m, h - m)):
            panels.append(sh('DIAMOND', x, y, 14, 14, P['a2'], .95))
        panels.append(sh('ROUNDED_RECT', cx, cy, W - 70, bh + 2*pad, P['panel'], .72))
    cy = max(bh/2 + 10, min(h - bh/2 - 10, cy))
    y = cy - bh/2
    els = []; zone = (cx - 165, cy - bh/2 - pad, cx + 165, cy + bh/2 + pad)
    for kind, ph in parts:
        yc = y + ph/2
        if kind == 'kick': els.append(T(kick, cx, yc, 12, True, P['kick']))
        elif kind == 'head': els.append(T(head, cx, yc, hfs, True, ink))
        elif kind == 'sub': els.append(T(sub, cx, yc, 13, False, ink, .92))
        elif kind == 'cta':
            bw = max(110.0, tw(cta, 13, True) + 40)
            els.append(sh('ROUNDED_RECT', cx, yc, bw, 34, P['a1'], 1.0, 0, 'cta')); els.append(T(cta, cx, yc, 13, True, P['cta_txt']))
        y += ph + gap
    return els, panels, zone, dict(hfs=hfs, bh=bh)

def to_elements(items, tid):
    out = []
    for i, it in enumerate(items):
        eid = 'e%d' % i
        if it['t'] == 'T':
            out.append(dict(id=eid, type='TEXT', text=it['text'], x=it['x'], y=it['y'], fontSize=it['fs'], isBold=it['b'], colorHex=it['c'], opacity=it['o']))
        else:
            out.append(dict(id=eid, type='SHAPE', shapeType=it['s'], x=it['x'], y=it['y'], width=it['w'], height=it['h'], colorHex=it['c'], opacity=it['o'], rotation=it['r'], text=it['tag'] if it['tag'].startswith('grad') else ''))
    return out

LAYOUT_NAMES = ['center', 'bottompanel', 'toppanel', 'card', 'ribbon', 'diagband', 'frame']
ASPECTS = [('1:1', 360.0), ('4:5', 450.0), ('16:9', 202.5), ('9:16', 640.0)]
ASPECT_W = [0.34, 0.30, 0.22, 0.14]

PATTERNS = ["أفضل {N} في مدينتك", "اكتشف عالم {N}", "عالم {N} بين يديك", "سحر {N}", "عرض خاص على {N}", "كل ما تحتاجه من {N}", "جديدنا في {N}", "مع {N} كل شيء أجمل", "استمتع مع {N}", "أحدث ما في {N}", "لمسة جديدة على {N}", "{N} في أجمل صورها", "كل يوم مع {N}", "هنا كل ما يخص {N}", "خصم خاص على {N}", "شغفنا هو {N}", "تجربة جديدة في {N}", "رحلة في عالم {N}"]
SOCIAL = ["اكتشف عالم {N}", "عالم {N} بين يديك", "سحر {N}", "مع {N} كل شيء أجمل", "استمتع مع {N}", "لمسة جديدة على {N}", "{N} في أجمل صورها", "كل يوم مع {N}", "رحلة في عالم {N}", "شغفنا هو {N}", "جمال {N}"]

def fdist(a, b):
    return sum(1 for x, y in zip(a, b) if x != y)

def generate(per_cat, existing=(), batch=1, min_dist=2):
    """Append-only: existing catalog entries are never changed; only new templates are produced."""
    pals, dropped = make_palettes()
    pal_idx = {p['name']: i for i, p in enumerate(pals)}
    taken = set(tuple(e['fp']) for e in existing)
    out = []
    for cat, qs in SUBJECTS.items():
        slug = CAT_SLUG[cat]; pats = SOCIAL if slug == 'social' else PATTERNS
        ex = [e for e in existing if e['cat'] == cat]
        used_heads = set(e['tAr'] for e in ex)
        chosen_fp = [tuple(e['fp']) for e in ex]
        rng0 = random.Random(zlib.crc32(cat.encode()) + batch * 7919)
        heads = [(q, p.format(N=NOUN[q])) for q in qs for p in pats]
        heads = [(q, hd) for q, hd in heads if len(hd) <= 28 and hd not in used_heads]
        rng0.shuffle(heads)
        n = len(ex); added = 0
        for q, head in heads:
            if added >= per_cat: break
            asp = rng0.choices(ASPECTS, ASPECT_W)[0]; h = asp[1]
            for attempt in range(80):
                fp = (rng0.randrange(len(LAYOUT_NAMES)), rng0.randrange(len(ARTS)), rng0.randrange(len(pals)), rng0.randrange(3))
                if h < 260 and fp[3] == 0: fp = (fp[0], fp[1], fp[2], rng0.choice([1, 2]))
                if fp in taken: continue
                if any(fdist(fp, o) < min_dist for o in chosen_fp): continue
                break
            else:
                continue
            chosen_fp.append(fp); taken.add(fp)
            P = pals[fp[2]]
            trng = random.Random(zlib.crc32(repr((cat, head, fp)).encode()))
            sub = rng0.choice(SUBLINES[cat]); kick = rng0.choice(KICK); cta = rng0.choice(CTA_CAT[cat])
            n += 1; added += 1
            tid = 'cg_%s_%03d' % (slug, n)
            els, panels, zone, note = build(P, fp[0], fp[3], head, sub, kick, cta, h, trng)
            art = ARTS[fp[1]](trng, P, h, zone)
            def cap(it):
                if it['t'] == 'S' and it['tag'] != 'cta':
                    r = max(it['w'], it['h']) / 2
                    if (zone[0] - r < it['x'] < zone[2] + r) and (zone[1] - r < it['y'] < zone[3] + r): it['o'] = min(it['o'], .16)
                return it
            art = [cap(a) for a in art]
            out.append(dict(id=tid, cat=cat, q=q, asp=asp[0], h=h, head=head, sub=sub, kick=kick, cta=cta, fp=fp,
                            layout=LAYOUT_NAMES[fp[0]], art=ART_NAMES[fp[1]], pal=P['name'], arr=fp[3],
                            bs=P['b1'], be=P['b2'], items=art + panels + els, zone=zone, hfs=note['hfs'], P=P))
    return out, dropped

def enc(t):
    els = []
    for it in t['items']:
        if it['t'] == 'T': els.append(['T', it['text'], it['x'], it['y'], it['fs'], 1 if it['b'] else 0, it['c'], it['o']])
        else: els.append(['S', it['s'], it['x'], it['y'], it['w'], it['h'], it['c'], it['o'], it['r'], it['tag']])
    return dict(id=t['id'], cat=t['cat'], asp=t['asp'], tAr=t['head'], tEn=t['q'].title() + ' ' + t['layout'].title(), bs=t['bs'], be=t['be'], fp=list(t['fp']), e=els)
