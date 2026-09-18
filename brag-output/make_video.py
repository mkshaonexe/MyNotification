#!/usr/bin/env python3
"""
Social Sentry App - Promotional Video Generator v2
Uses Pillow for image composition + FFmpeg for video encoding.
"""

import subprocess
import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import textwrap

BASE_DIR = Path("/Users/mkshaon/playground/MyNotification-main")
SCREENSHOTS = BASE_DIR / "social_sentry_screenshots"
OUTPUT = BASE_DIR / "brag-output"
FRAMES = OUTPUT / "frames"
FRAMES.mkdir(parents=True, exist_ok=True)

# Colors
BG_DARK = (13, 17, 23)          # #0d1117
ACCENT_PINK = (255, 45, 126)    # Social Sentry brand pink
WHITE = (255, 255, 255)
GRAY = (170, 170, 170)
DARK_CARD = (22, 27, 34)

# Feature accent colors
COLORS = {
    "reels": (255, 107, 107),
    "adult": (78, 205, 196),
    "study": (81, 207, 102),
    "monk": (168, 85, 247),
    "insights": (255, 212, 75),
    "default": ACCENT_PINK
}


def get_font(size: int, bold: bool = False):
    """Get a macOS system font."""
    font_paths = [
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/HelveticaNeue.ttc",
        "/Library/Fonts/Arial.ttf",
    ]
    for fp in font_paths:
        try:
            return ImageFont.truetype(fp, size)
        except Exception:
            continue
    return ImageFont.load_default()


def draw_rounded_rect(draw, xy, radius, fill):
    """Draw a rounded rectangle."""
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle(xy, radius=radius, fill=fill)


def create_title_card(width: int, height: int, title: str, subtitle: str) -> Image.Image:
    """Create an animated-looking title card."""
    img = Image.new("RGB", (width, height), BG_DARK)
    draw = ImageDraw.Draw(img)
    
    # Add subtle gradient effect with a darker rectangle
    # Top gradient bar
    draw.rectangle([0, 0, width, 4], fill=ACCENT_PINK)
    
    # Brand icon / logo text
    font_brand = get_font(int(height * 0.10), bold=True)
    font_sub = get_font(int(height * 0.035))
    font_tagline = get_font(int(height * 0.025))
    
    # Draw main title
    bbox = draw.textbbox((0, 0), title, font=font_brand)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    
    # Pink "Social" + white "Sentry"
    if " " in title:
        parts = title.split(" ", 1)
        p1_bbox = draw.textbbox((0, 0), parts[0] + " ", font=font_brand)
        p1_w = p1_bbox[2] - p1_bbox[0]
        total_w = draw.textbbox((0, 0), title, font=font_brand)
        total_w = total_w[2] - total_w[0]
        
        x_start = (width - total_w) // 2
        y_title = int(height * 0.38)
        
        draw.text((x_start, y_title), parts[0] + " ", fill=ACCENT_PINK, font=font_brand)
        draw.text((x_start + p1_w, y_title), parts[1], fill=WHITE, font=font_brand)
    else:
        x_start = (width - tw) // 2
        y_title = int(height * 0.38)
        draw.text((x_start, y_title), title, fill=ACCENT_PINK, font=font_brand)
    
    # Draw subtitle
    sub_bbox = draw.textbbox((0, 0), subtitle, font=font_sub)
    sw = sub_bbox[2] - sub_bbox[0]
    sh = sub_bbox[3] - sub_bbox[1]
    x_sub = (width - sw) // 2
    y_sub = y_title + th + int(height * 0.06)
    draw.text((x_sub, y_sub), subtitle, fill=GRAY, font=font_sub)
    
    # Tagline bar
    tagline = '"Less Scroll. More Life."'
    tag_bbox = draw.textbbox((0, 0), tagline, font=font_tagline)
    tw2 = tag_bbox[2] - tag_bbox[0]
    draw.text(((width - tw2) // 2, int(height * 0.72)), tagline, 
              fill=(100, 100, 100), font=font_tagline)
    
    return img


def place_phone_screenshot(bg: Image.Image, screenshot_path: Path, 
                             x: int, y: int, target_h: int) -> Image.Image:
    """Place a scaled phone screenshot on the background."""
    phone = Image.open(screenshot_path).convert("RGB")
    pw, ph = phone.size
    
    # Scale to target height
    scale = target_h / ph
    new_w = int(pw * scale)
    new_h = target_h
    phone = phone.resize((new_w, new_h), Image.LANCZOS)
    
    # Add phone frame effect: rounded rectangle border
    frame_img = Image.new("RGB", (new_w + 8, new_h + 8), (40, 40, 40))
    frame_img.paste(phone, (4, 4))
    
    result = bg.copy()
    result.paste(frame_img, (x - 4, y - 4))
    return result


def create_feature_slide_landscape(
    screenshot_path: Path, width: int, height: int,
    feature_name: str, description: str, 
    accent: tuple = ACCENT_PINK
) -> Image.Image:
    """Create a landscape feature slide with phone on right, text on left."""
    
    img = Image.new("RGB", (width, height), BG_DARK)
    draw = ImageDraw.Draw(img)
    
    # Top accent bar
    draw.rectangle([0, 0, width, 4], fill=accent)
    
    # Place phone on right side
    phone_h = int(height * 0.88)
    phone_x = int(width * 0.53)
    phone_y = int((height - phone_h) / 2)
    
    img = place_phone_screenshot(img, screenshot_path, phone_x, phone_y, phone_h)
    draw = ImageDraw.Draw(img)
    
    # Left side: brand + feature text
    text_x = int(width * 0.06)
    
    # Small "Social Sentry" label
    font_brand_sm = get_font(int(height * 0.028))
    draw.text((text_x, int(height * 0.08)), "Social Sentry", fill=ACCENT_PINK, font=font_brand_sm)
    
    # Feature name (large)
    font_feature = get_font(int(height * 0.082), bold=True)
    font_desc = get_font(int(height * 0.030))
    
    y_feature = int(height * 0.30)
    
    # Accent color line
    draw.rectangle([text_x, y_feature - 8, text_x + 60, y_feature - 3], fill=accent)
    
    # Feature name (may need wrapping)
    words = feature_name.split()
    if len(words) <= 2:
        draw.text((text_x, y_feature), feature_name, fill=WHITE, font=font_feature)
        y_desc = y_feature + int(height * 0.12)
    else:
        line1 = " ".join(words[:2])
        line2 = " ".join(words[2:])
        draw.text((text_x, y_feature), line1, fill=WHITE, font=font_feature)
        draw.text((text_x, y_feature + int(height * 0.11)), line2, fill=WHITE, font=font_feature)
        y_desc = y_feature + int(height * 0.24)
    
    # Description (wrapped)
    for i, line in enumerate(description.split("\n")):
        draw.text((text_x, y_desc + i * int(height * 0.045)), 
                  line, fill=GRAY, font=font_desc)
    
    return img


def create_feature_slide_vertical(
    screenshot_path: Path, width: int, height: int,
    feature_name: str, description: str,
    accent: tuple = ACCENT_PINK
) -> Image.Image:
    """Create a vertical feature slide with phone centered, text above."""
    
    img = Image.new("RGB", (width, height), BG_DARK)
    draw = ImageDraw.Draw(img)
    
    # Top accent bar
    draw.rectangle([0, 0, width, 5], fill=accent)
    
    # Phone screenshot centered
    phone_h = int(height * 0.58)
    phone_w_approx = int(phone_h * 0.48)
    phone_x = (width - phone_w_approx) // 2
    phone_y = int(height * 0.38)
    
    img = place_phone_screenshot(img, screenshot_path, phone_x, phone_y, phone_h)
    draw = ImageDraw.Draw(img)
    
    # Text in upper portion
    text_x = int(width * 0.08)
    text_w = int(width * 0.84)
    
    # Brand label
    font_brand = get_font(int(height * 0.022))
    draw.text((text_x, int(height * 0.06)), "Social Sentry", fill=ACCENT_PINK, font=font_brand)
    
    # Feature name
    font_feature = get_font(int(height * 0.060), bold=True)
    font_desc = get_font(int(height * 0.024))
    
    y_feature = int(height * 0.12)
    draw.text((text_x, y_feature), feature_name, fill=WHITE, font=font_feature)
    
    # Underline
    feat_bbox = draw.textbbox((text_x, y_feature), feature_name, font=font_feature)
    feat_w = feat_bbox[2] - feat_bbox[0]
    
    y_desc = y_feature + int(height * 0.085)
    
    # Description
    for i, line in enumerate(description.split("\n")):
        draw.text((text_x, y_desc + i * int(height * 0.036)),
                  line, fill=GRAY, font=font_desc)
    
    return img


def create_cta_slide(width: int, height: int, landscape: bool = True) -> Image.Image:
    """Create a call-to-action slide."""
    img = Image.new("RGB", (width, height), BG_DARK)
    draw = ImageDraw.Draw(img)
    
    # Accent bar
    draw.rectangle([0, 0, width, 5], fill=ACCENT_PINK)
    
    font_large = get_font(int(height * 0.10), bold=True)
    font_mid = get_font(int(height * 0.040))
    font_small = get_font(int(height * 0.028))
    
    center_y = int(height * 0.35)
    
    # Main CTA text
    cta = "Get Started Today"
    bbox = draw.textbbox((0, 0), cta, font=font_large)
    cw = bbox[2] - bbox[0]
    draw.text(((width - cw) // 2, center_y), cta, fill=WHITE, font=font_large)
    
    # Sub text
    sub = "Download Social Sentry"
    s_bbox = draw.textbbox((0, 0), sub, font=font_mid)
    sw = s_bbox[2] - s_bbox[0]
    draw.text(((width - sw) // 2, center_y + int(height * 0.14)), 
              sub, fill=ACCENT_PINK, font=font_mid)
    
    # Tagline
    tag = '"Less Scroll. More Life."'
    t_bbox = draw.textbbox((0, 0), tag, font=font_small)
    tw = t_bbox[2] - t_bbox[0]
    draw.text(((width - tw) // 2, center_y + int(height * 0.24)),
              tag, fill=GRAY, font=font_small)
    
    # Store badges hint
    store = "Available on Google Play"
    st_bbox = draw.textbbox((0, 0), store, font=font_small)
    stw = st_bbox[2] - st_bbox[0]
    draw.text(((width - stw) // 2, int(height * 0.72)),
              store, fill=(80, 80, 80), font=font_small)
    
    return img


def save_frame(img: Image.Image, path: Path):
    img.save(str(path), "PNG", quality=95)
    print(f"  Saved: {path.name}")


def create_video_from_frames(frame_paths: list, output_path: Path, 
                              fps: int = 24, duration_per_frame: int = 4):
    """Use FFmpeg concat to create video from frames."""
    concat_file = OUTPUT / "concat.txt"
    with open(concat_file, "w") as f:
        for fp in frame_paths:
            f.write(f"file '{fp}'\n")
            f.write(f"duration {duration_per_frame}\n")
        # Repeat last frame (concat demuxer quirk)
        f.write(f"file '{frame_paths[-1]}'\n")
    
    cmd = [
        "ffmpeg", "-y",
        "-f", "concat", "-safe", "0",
        "-i", str(concat_file),
        "-vf", f"fps={fps},format=yuv420p",
        "-c:v", "libx264", "-preset", "medium", "-crf", "22",
        str(output_path)
    ]
    print(f"\nEncoding video: {output_path.name}...")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode == 0:
        size = output_path.stat().st_size / 1024 / 1024
        print(f"✅ Created: {output_path.name} ({size:.1f} MB)")
    else:
        print(f"❌ Error: {result.stderr[-500:]}")
    
    concat_file.unlink(missing_ok=True)


def main():
    print("=" * 50)
    print("Social Sentry Promotional Video Generator")
    print("=" * 50)
    
    ss = SCREENSHOTS
    
    # Screenshot mapping
    shots = {
        "dashboard": ss / "03_dashboard.png",
        "reels": ss / "04_reels_blocker.png",
        "adult": ss / "05_adult_blocker.png",
        "study": ss / "11_study_mode.png",
        "schedule": ss / "12_schedule_blocker.png",
        "monk_list": ss / "13_scroll.png",
        "insights": ss / "15_insights.png",
        "goals": ss / "16_goals.png",
    }
    
    # ─── YOUTUBE LANDSCAPE VIDEO (1920x1080) ─────────────────────────────────
    print("\n📹 Creating YouTube Landscape (1920x1080)...")
    W, H = 1920, 1080
    
    yt_frames = []
    
    # 1. Title card
    f = FRAMES / "yt_01_title.png"
    save_frame(create_title_card(W, H, "Social Sentry", 
                                  "Your AI-Powered Digital Wellbeing Companion"), f)
    yt_frames.append(f)
    
    # 2. Dashboard Overview
    f = FRAMES / "yt_02_dashboard.png"
    save_frame(create_feature_slide_landscape(
        shots["dashboard"], W, H,
        "Your Wellbeing Hub",
        "Track screen time.\nBlock digital distractions.\nAchieve your goals.",
        accent=ACCENT_PINK
    ), f)
    yt_frames.append(f)
    
    # 3. Reels Blocker
    f = FRAMES / "yt_03_reels.png"
    save_frame(create_feature_slide_landscape(
        shots["reels"], W, H,
        "Reels Blocker",
        "Stop endless scrolling.\nBlock reels on Instagram,\nFacebook & TikTok.",
        accent=COLORS["reels"]
    ), f)
    yt_frames.append(f)
    
    # 4. Adult Blocker
    f = FRAMES / "yt_04_adult.png"
    save_frame(create_feature_slide_landscape(
        shots["adult"], W, H,
        "Adult Content\nBlocker",
        "DNS Protection + AI NSFW Shield.\nBlocks adult sites in all browsers.\nKeep your browsing clean.",
        accent=COLORS["adult"]
    ), f)
    yt_frames.append(f)
    
    # 5. Monk Mode
    f = FRAMES / "yt_05_monk.png"
    save_frame(create_feature_slide_landscape(
        shots["monk_list"], W, H,
        "Monk Mode",
        "Total lockdown with only\nessential apps allowed.\nDeep focus. Zero distractions.",
        accent=COLORS["monk"]
    ), f)
    yt_frames.append(f)
    
    # 6. Study Mode
    f = FRAMES / "yt_06_study.png"
    save_frame(create_feature_slide_landscape(
        shots["study"], W, H,
        "Study Mode",
        "Maximum Focus sessions.\nBlock distracting apps.\nSilence notifications automatically.",
        accent=COLORS["study"]
    ), f)
    yt_frames.append(f)
    
    # 7. Goals & Insights
    f = FRAMES / "yt_07_insights.png"
    save_frame(create_feature_slide_landscape(
        shots["insights"], W, H,
        "Track Your Growth",
        "XP Goals, Skill Points & Activity.\nMeasure your progress every day.\nBecome the best version of yourself.",
        accent=COLORS["insights"]
    ), f)
    yt_frames.append(f)
    
    # 8. CTA
    f = FRAMES / "yt_08_cta.png"
    save_frame(create_cta_slide(W, H, landscape=True), f)
    yt_frames.append(f)
    
    yt_output = OUTPUT / "social_sentry_youtube.mp4"
    create_video_from_frames(yt_frames, yt_output, fps=24, duration_per_frame=5)
    
    # ─── INSTAGRAM/TIKTOK REELS (1080x1920) ──────────────────────────────────
    print("\n📱 Creating Instagram/TikTok Reels (1080x1920)...")
    VW, VH = 1080, 1920
    
    ig_frames = []
    
    # 1. Title
    f = FRAMES / "ig_01_title.png"
    save_frame(create_title_card(VW, VH, "Social Sentry",
                                  "AI-Powered Digital Wellbeing"), f)
    ig_frames.append(f)
    
    # 2. Dashboard
    f = FRAMES / "ig_02_dashboard.png"
    save_frame(create_feature_slide_vertical(
        shots["dashboard"], VW, VH,
        "Your Focus App",
        "Track screen time.\nBlock what distracts you.",
    ), f)
    ig_frames.append(f)
    
    # 3. Reels Blocker
    f = FRAMES / "ig_03_reels.png"
    save_frame(create_feature_slide_vertical(
        shots["reels"], VW, VH,
        "Reels Blocker",
        "Stop the infinite scroll loop.",
        accent=COLORS["reels"]
    ), f)
    ig_frames.append(f)
    
    # 4. Adult Blocker
    f = FRAMES / "ig_04_adult.png"
    save_frame(create_feature_slide_vertical(
        shots["adult"], VW, VH,
        "Adult Blocker",
        "DNS + AI-powered safe browsing.",
        accent=COLORS["adult"]
    ), f)
    ig_frames.append(f)
    
    # 5. Monk Mode
    f = FRAMES / "ig_05_monk.png"
    save_frame(create_feature_slide_vertical(
        shots["monk_list"], VW, VH,
        "Monk Mode",
        "Total focus. Zero distractions.",
        accent=COLORS["monk"]
    ), f)
    ig_frames.append(f)
    
    # 6. CTA
    f = FRAMES / "ig_06_cta.png"
    save_frame(create_cta_slide(VW, VH, landscape=False), f)
    ig_frames.append(f)
    
    ig_output = OUTPUT / "social_sentry_reels.mp4"
    create_video_from_frames(ig_frames, ig_output, fps=24, duration_per_frame=4)
    
    print("\n" + "=" * 50)
    print("✅ All videos created!")
    print(f"📁 Output: {OUTPUT}")
    for f in OUTPUT.glob("*.mp4"):
        size = f.stat().st_size / 1024 / 1024
        print(f"   {f.name}: {size:.1f} MB")


if __name__ == "__main__":
    main()
