package com.example.data.ai

/**
 * Prompt engineering for the in-app image generator. Each UI style preset maps to an English
 * keyword bundle (lighting, composition, finish) so the free FLUX.2 klein models produce
 * professional-looking results. A cleanup suffix keeps text, letters, watermarks and logos out
 * of generated images; Arabic text is overlaid in the editor instead.
 */
object StylePrompts {

    private const val CLEANUP =
        ", no text, no letters, no words, no typography, no watermark, no logo, no signature"

    // Logo generation explicitly wants a logo mark, so only text/watermarks are banned there.
    const val LOGO_CLEANUP = ", no text, no letters, no words, no watermark, no signature"

    fun bundleFor(styleLabel: String): String {
        val s = styleLabel.lowercase()
        return when {
            s.contains("ذهب") || s.contains("أبعاد") || s.contains("luxury") || s.contains("3d") ->
                "luxurious 3D render, polished metallic gold (#D4AF37) accents on deep obsidian black, cinematic studio lighting with soft rim light, volumetric glow, ultra-detailed textures, premium brand campaign aesthetic, centered elegant composition"
            s.contains("خط") || s.contains("callig") ->
                "abstract calligraphy-inspired ink artwork, flowing expressive brush strokes, gold and black ink on textured ivory paper, dynamic movement, museum-grade fine art, abstract shapes only, no legible characters"
            s.contains("بسيط") || s.contains("مودرن") || s.contains("minimal") ->
                "minimalist premium design, generous negative space, single clear focal subject, soft diffused daylight, clean geometric composition, matte finish, editorial magazine quality"
            s.contains("واقع") || s.contains("photo") ->
                "photorealistic commercial photography, 85mm lens at f/1.8, shallow depth of field, three-point studio lighting, crisp product detail, high-end advertising shot, clean backdrop"
            s.contains("نيون") || s.contains("مستقبل") || s.contains("cyber") ->
                "futuristic neon aesthetic, deep blacks with electric gold and cyan rim lighting, sleek high-tech atmosphere, cinematic wide composition, ultra-sharp detailed render"
            s.contains("مائي") || s.contains("water") ->
                "delicate watercolor illustration, soft pigment bleeds, warm ivory paper texture, gentle golden highlights, airy elegant composition, hand-painted fine art feel"
            else ->
                "professional design, cinematic lighting, refined composition, ultra detailed"
        }
    }

    fun sizeFor(ratio: String): Pair<Int, Int> = when (ratio) {
        "16:9" -> 1216 to 704
        "9:16" -> 704 to 1216
        "4:5" -> 864 to 1088
        else -> 1024 to 1024
    }

    fun buildPrompt(userPrompt: String, styleLabel: String): String =
        "$userPrompt, ${bundleFor(styleLabel)}$CLEANUP"
}
