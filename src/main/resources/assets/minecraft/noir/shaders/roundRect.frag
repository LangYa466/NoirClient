#version 120

uniform vec2 rectSize;
uniform float radius;
uniform vec4 color;
uniform float shadowBlur;
uniform vec4 shadowColor;
uniform vec2 shadowOffset;

// Versione della funzione SDF scritta in modo più esplicito
float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);

    // Decomponiamo il calcolo per una maggiore compatibilità
    vec2 max_q_zero = max(q, vec2(0.0));
    float cornerDistance = length(max_q_zero);
    float edgeDistance = min(max(q.x, q.y), 0.0);

    return cornerDistance + edgeDistance - r;
}

void main() {
    vec2 uv = gl_TexCoord[0].st * rectSize - (rectSize * 0.5);

    // Calcolo dell'ombra
    float shadowDistance = sdRoundRect(uv - shadowOffset, rectSize * 0.5, radius);
    float shadowAlpha = smoothstep(shadowBlur, 0.0, shadowDistance);
    vec4 finalShadowColor = vec4(shadowColor.rgb, shadowColor.a * shadowAlpha);

    // Calcolo del rettangolo
    float rectDistance = sdRoundRect(uv, rectSize * 0.5, radius);
    float rectAlpha = 1.0 - smoothstep(0.0, 1.0, rectDistance);
    vec4 finalRectColor = vec4(color.rgb, color.a * rectAlpha);

    // Fusione
    gl_FragColor = mix(finalShadowColor, finalRectColor, finalRectColor.a);
}